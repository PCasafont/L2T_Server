package l2server.gameserver.model.entity;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntProcedure;
import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.Server;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.DoorTemplate;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

/**
 * @author evill33t, GodKratos
 */
public class Instance {
	private static Logger log = LoggerFactory.getLogger(Server.class.getName());
	
	private int id;
	private String name;

	private TIntHashSet players = new TIntHashSet();
	private final EjectPlayerProcedure ejectProc;

	private CopyOnWriteArrayList<Npc> npcs = new CopyOnWriteArrayList<>();
	private ArrayList<DoorInstance> doors = null;
	private int[] spawnLoc = new int[3];
	private boolean allowSummon = true;
	private long emptyDestroyTime = -1;
	private long lastLeft = -1;
	private long instanceStartTime = -1;
	private long instanceEndTime = -1;
	private boolean isPvPInstance = false;
	private boolean isPeaceInstance = false;
	private boolean showTimer = false;
	private boolean isTimerIncrease = true;
	private String timerText = "";

	protected ScheduledFuture<?> CheckTimeUpTask = null;

	public Instance(int id) {
		this.id = id;
		ejectProc = new EjectPlayerProcedure();
		instanceStartTime = System.currentTimeMillis();
	}

	/**
	 * Returns the ID of this instance.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns the name of this instance
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns whether summon friend type skills are allowed for this instance
	 */
	public boolean isSummonAllowed() {
		return allowSummon;
	}

	/**
	 * Sets the status for the instance for summon friend type skills
	 */
	public void setAllowSummon(boolean b) {
		allowSummon = b;
	}

	/*
	 * Returns true if entire instance is PvP zone
	 */
	public boolean isPvPInstance() {
		return isPvPInstance;
	}

	/*
	 * Sets PvP zone status of the instance
	 */
	public void setPvPInstance(boolean b) {
		isPvPInstance = b;
	}

	public void setPeaceInstance(boolean b) {
		isPeaceInstance = b;
	}

	public boolean isPeaceInstance() {
		return isPeaceInstance;
	}

	/**
	 * Set the instance duration task
	 *
	 * @param duration in milliseconds
	 */
	public void setDuration(int duration) {
		if (CheckTimeUpTask != null) {
			CheckTimeUpTask.cancel(true);
		}

		CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(duration), 500);
		instanceEndTime = System.currentTimeMillis() + duration + 500;
	}

	/**
	 * Set time before empty instance will be removed
	 *
	 * @param time in milliseconds
	 */
	public void setEmptyDestroyTime(long time) {
		emptyDestroyTime = time;
	}

	/**
	 * Checks if the player exists within this instance
	 *
	 * @param objectId
	 * @return true if player exists in instance
	 */
	public boolean containsPlayer(int objectId) {
		return players.contains(objectId);
	}

	/**
	 * Adds the specified player to the instance
	 *
	 * @param objectId Players object ID
	 */
	public void addPlayer(int objectId) {
		synchronized (players) {
			players.add(objectId);
		}
	}

	/**
	 * Removes the specified player from the instance list
	 *
	 * @param objectId Players object ID
	 */
	public void removePlayer(int objectId) {
		synchronized (players) {
			players.remove(objectId);
		}

		if (players.isEmpty() && emptyDestroyTime >= 0) {
			lastLeft = System.currentTimeMillis();
			setDuration((int) (instanceEndTime - System.currentTimeMillis() - 500));
		}
	}

	/**
	 * Removes the player from the instance by setting InstanceId to 0 and teleporting to nearest town.
	 *
	 * @param objectId
	 */
	public void ejectPlayer(int objectId) {
		Player player = World.getInstance().getPlayer(objectId);
		if (player != null && player.getInstanceId() == getId()) {
			player.setInstanceId(0);
			player.sendMessage("You were removed from the instance");
			if (getSpawnLoc()[0] != 0 && getSpawnLoc()[1] != 0 && getSpawnLoc()[2] != 0) {
				player.teleToLocation(getSpawnLoc()[0], getSpawnLoc()[1], getSpawnLoc()[2]);
			} else {
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
	}

	public void addNpc(Npc npc) {
		npcs.add(npc);
	}

	public void removeNpc(Npc npc) {
		if (npc.getSpawn() != null) {
			npc.getSpawn().stopRespawn();
		}
		//npc.deleteMe();
		npcs.remove(npc);
	}

	/**
	 * Adds a door into the instance
	 *
	 * @param doorId - from doors.csv
	 */
	public void addDoor(int doorId, StatsSet set) {
		if (doors == null) {
			doors = new ArrayList<>(2);
		}

		for (DoorInstance door : doors) {
			if (door.getDoorId() == doorId) {
				log.warn("Door ID " + doorId + " already exists in instance " + getId());
				return;
			}
		}

		DoorTemplate temp = DoorTable.getInstance().getDoorTemplate(doorId);
		DoorInstance newdoor = new DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
		newdoor.setInstanceId(getId());
		newdoor.setCurrentHp(newdoor.getMaxHp());
		newdoor.spawnMe(temp.posX, temp.posY, temp.posZ);
		doors.add(newdoor);
	}

	public TIntHashSet getPlayers() {
		return players;
	}

	public CopyOnWriteArrayList<Npc> getNpcs() {
		return npcs;
	}

	public ArrayList<DoorInstance> getDoors() {
		return doors;
	}

	public DoorInstance getDoor(int id) {
		for (DoorInstance temp : getDoors()) {
			if (temp.getDoorId() == id) {
				return temp;
			}
		}
		return null;
	}

	public long getInstanceEndTime() {
		return instanceEndTime;
	}

	public long getInstanceStartTime() {
		return instanceStartTime;
	}

	public boolean isShowTimer() {
		return showTimer;
	}

	public boolean isTimerIncrease() {
		return isTimerIncrease;
	}

	public String getTimerText() {
		return timerText;
	}

	/**
	 * Returns the spawn location for this instance to be used when leaving the instance
	 *
	 * @return int[3]
	 */
	public int[] getSpawnLoc() {
		return spawnLoc;
	}

	/**
	 * Sets the spawn location for this instance to be used when leaving the instance
	 */
	public void setSpawnLoc(int[] loc) {
		if (loc == null || loc.length < 3) {
			return;
		}
		System.arraycopy(loc, 0, spawnLoc, 0, 3);
	}

	public void removePlayers() {
		players.forEach(ejectProc);

		synchronized (players) {
			players.clear();
		}
	}

	public void removeNpcs() {
		for (Npc mob : npcs) {
			if (mob != null) {
				if (mob.getSpawn() != null) {
					mob.getSpawn().stopRespawn();
				}
				mob.deleteMe();
				mob.setInstanceId(0);
			}
		}
		npcs.clear();
	}

	public void removeDoors() {
		if (doors == null) {
			return;
		}

		for (DoorInstance door : doors) {
			if (door != null) {
				WorldRegion region = door.getWorldRegion();
				door.decayMe();

				if (region != null) {
					region.removeVisibleObject(door);
				}

				door.getKnownList().removeAllKnownObjects();
				World.getInstance().removeObject(door);
			}
		}
		doors.clear();
		doors = null;
	}

	public void loadInstanceTemplate(String filename) {
		File xml = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "instances/" + filename);

		try {
			XmlDocument doc = new XmlDocument(xml);
			for (XmlNode n : doc.getChildren()) {
				if (n.getName().equalsIgnoreCase("instance")) {
					parseInstance(n);
				}
			}
		} catch (IOException e) {
			log.warn("Instance: can not find " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		} catch (Exception e) {
			log.warn("Instance: error while loading " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
	}

	private void parseInstance(XmlNode node) throws Exception {
		L2Spawn spawnDat;
		NpcTemplate npcTemplate;
		String name = node.getString("name");
		setName(name);

		for (XmlNode n : node.getChildren()) {
			if (n.getName().equalsIgnoreCase("activityTime")) {
				if (n.hasAttribute("val")) {
					CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(n.getInt("val") * 60000), 15000);
					instanceEndTime = System.currentTimeMillis() + n.getLong("val") * 60000 + 15000;
				}
			}
			/*			else if (n.getName().equalsIgnoreCase("timeDelay"))
                        {
							a = n.getString("val");
							if (a != null)
								instance.setTimeDelay(Integer.parseInt(a);
						}*/
			else if (n.getName().equalsIgnoreCase("allowSummon")) {
				if (n.hasAttribute("val")) {
					setAllowSummon(n.getBool("val"));
				}
			} else if (n.getName().equalsIgnoreCase("emptyDestroyTime")) {
				if (n.hasAttribute("val")) {
					emptyDestroyTime = n.getLong("val") * 1000;
				}
			} else if (n.getName().equalsIgnoreCase("showTimer")) {
				if (n.hasAttribute("val")) {
					showTimer = n.getBool("val");
				}
				if (n.hasAttribute("increase")) {
					isTimerIncrease = n.getBool("increase");
				}
				if (n.hasAttribute("text")) {
					timerText = n.getString("text");
				}
			} else if (n.getName().equalsIgnoreCase("PvPInstance")) {
				if (n.hasAttribute("val")) {
					setPvPInstance(n.getBool("val"));
				}
			} else if (n.getName().equalsIgnoreCase("doorlist")) {
				for (XmlNode d : n.getChildren()) {
					int doorId = 0;
					if (d.getName().equalsIgnoreCase("door")) {
						doorId = d.getInt("doorId");
						StatsSet set = new StatsSet();
						for (XmlNode bean : d.getChildren()) {
							if (bean.getName().equalsIgnoreCase("set")) {
								String setname = d.getString("name");
								String value = d.getString("val");
								set.set(setname, value);
							}
						}
						addDoor(doorId, set);
					}
				}
			} else if (n.getName().equalsIgnoreCase("spawnlist")) {
				for (XmlNode d : n.getChildren()) {
					int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0, delay = -1;

					if (d.getName().equalsIgnoreCase("spawn")) {

						npcId = d.getInt("npcId");
						x = d.getInt("x");
						y = d.getInt("y");
						z = d.getInt("z");
						heading = d.getInt("heading");
						respawn = d.getInt("respawn");
						if (d.hasAttribute("onKillDelay")) {
							delay = d.getInt("onKillDelay");
						}

						npcTemplate = NpcTable.getInstance().getTemplate(npcId);
						if (npcTemplate != null) {
							spawnDat = new L2Spawn(npcTemplate);
							spawnDat.setX(x);
							spawnDat.setY(y);
							spawnDat.setZ(z);
							spawnDat.setHeading(heading);
							spawnDat.setRespawnDelay(respawn);
							if (respawn == 0) {
								spawnDat.stopRespawn();
							} else {
								spawnDat.startRespawn();
							}
							spawnDat.setInstanceId(getId());
							Npc spawned = spawnDat.getNpc();
							spawnDat.doSpawn();
							if (delay >= 0 && spawned instanceof Attackable) {
								((Attackable) spawned).setOnKillDelay(delay);
							}
						} else {
							log.warn("Instance: Data missing in NPC table for ID: " + npcId + " in Instance " + getId());
						}
					}
				}
			} else if (n.getName().equalsIgnoreCase("spawnpoint")) {
				try {
					spawnLoc[0] = n.getInt("spawnX");
					spawnLoc[1] = n.getInt("spawnY");
					spawnLoc[2] = n.getInt("spawnZ");
				} catch (Exception e) {
					log.warn("Error parsing instance xml: " + e.getMessage(), e);
					spawnLoc = new int[3];
				}
			}
		}
		if (Config.DEBUG) {
			log.info(name + " Instance Template for Instance " + getId() + " loaded");
		}
	}

	protected void doCheckTimeUp(int remaining) {
		CreatureSay cs = null;
		int timeLeft;
		int interval;

		if (players.isEmpty() && emptyDestroyTime == 0) {
			remaining = 0;
			interval = 500;
		} else if (players.isEmpty() && emptyDestroyTime > 0) {

			Long emptyTimeLeft = lastLeft + emptyDestroyTime - System.currentTimeMillis();
			if (emptyTimeLeft <= 0) {
				interval = 0;
				remaining = 0;
			} else if (remaining > 300000 && emptyTimeLeft > 300000) {
				interval = 300000;
				remaining = remaining - 300000;
			} else if (remaining > 60000 && emptyTimeLeft > 60000) {
				interval = 60000;
				remaining = remaining - 60000;
			} else if (remaining > 30000 && emptyTimeLeft > 30000) {
				interval = 30000;
				remaining = remaining - 30000;
			} else {
				interval = 10000;
				remaining = remaining - 10000;
			}
		} else if (remaining > 300000) {
			timeLeft = remaining / 60000;
			interval = 300000;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 300000;
		} else if (remaining > 60000) {
			timeLeft = remaining / 60000;
			interval = 60000;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 60000;
		} else if (remaining > 30000) {
			timeLeft = remaining / 1000;
			interval = 30000;
			cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 30000;
		} else {
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 10000;
		}
		if (cs != null) {
			players.forEach(new SendPacketToPlayerProcedure(cs));
		}

		cancelTimer();
		//System.out.println(id + " (" + getName() + "): " + remaining + " " + interval);
		if (remaining >= 10000) {
			CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		} else {
			CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
		}
	}

	public void cancelTimer() {
		if (CheckTimeUpTask != null) {
			CheckTimeUpTask.cancel(true);
		}
	}

	public class CheckTimeUp implements Runnable {
		private int remaining;

		public CheckTimeUp(int remaining) {
			this.remaining = remaining;
		}

		@Override
		public void run() {
			doCheckTimeUp(remaining);
		}
	}

	public class TimeUp implements Runnable {
		@Override
		public void run() {
			InstanceManager.getInstance().destroyInstance(getId());
		}
	}

	private final class EjectPlayerProcedure implements TIntProcedure {
		EjectPlayerProcedure() {

		}

		@Override
		public final boolean execute(final int objId) {
			ejectPlayer(objId);
			return true;
		}
	}

	private final class SendPacketToPlayerProcedure implements TIntProcedure {
		private final L2GameServerPacket packet;

		SendPacketToPlayerProcedure(final L2GameServerPacket packet) {
			this.packet = packet;
		}

		@Override
		public final boolean execute(final int objId) {
			Player player = World.getInstance().getPlayer(objId);

			if (player.getInstanceId() == getId()) {
				player.sendPacket(packet);
			}
			return true;
		}
	}
}
