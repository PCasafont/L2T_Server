package l2server.gameserver.model.entity;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntProcedure;
import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2DoorTemplate;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

/**
 * @author evill33t, GodKratos
 */
public class Instance
{
	private int _id;
	private String _name;

	private TIntHashSet _players = new TIntHashSet();
	private final EjectPlayerProcedure _ejectProc;

	private CopyOnWriteArrayList<L2Npc> _npcs = new CopyOnWriteArrayList<>();
	private ArrayList<L2DoorInstance> _doors = null;
	private int[] _spawnLoc = new int[3];
	private boolean _allowSummon = true;
	private long _emptyDestroyTime = -1;
	private long _lastLeft = -1;
	private long _instanceStartTime = -1;
	private long _instanceEndTime = -1;
	private boolean _isPvPInstance = false;
	private boolean _isPeaceInstance = false;
	private boolean _showTimer = false;
	private boolean _isTimerIncrease = true;
	private String _timerText = "";

	protected ScheduledFuture<?> _CheckTimeUpTask = null;

	public Instance(int id)
	{
		_id = id;
		_ejectProc = new EjectPlayerProcedure();
		_instanceStartTime = System.currentTimeMillis();
	}

	/**
	 * Returns the ID of this instance.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Returns the name of this instance
	 */
	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Returns whether summon friend type skills are allowed for this instance
	 */
	public boolean isSummonAllowed()
	{
		return _allowSummon;
	}

	/**
	 * Sets the status for the instance for summon friend type skills
	 */
	public void setAllowSummon(boolean b)
	{
		_allowSummon = b;
	}

	/*
	 * Returns true if entire instance is PvP zone
	 */
	public boolean isPvPInstance()
	{
		return _isPvPInstance;
	}

	/*
	 * Sets PvP zone status of the instance
	 */
	public void setPvPInstance(boolean b)
	{
		_isPvPInstance = b;
	}

	public void setPeaceInstance(boolean b)
	{
		_isPeaceInstance = b;
	}

	public boolean isPeaceInstance()
	{
		return _isPeaceInstance;
	}

	/**
	 * Set the instance duration task
	 *
	 * @param duration in milliseconds
	 */
	public void setDuration(int duration)
	{
		if (_CheckTimeUpTask != null)
		{
			_CheckTimeUpTask.cancel(true);
		}

		_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(duration), 500);
		_instanceEndTime = System.currentTimeMillis() + duration + 500;
	}

	/**
	 * Set time before empty instance will be removed
	 *
	 * @param time in milliseconds
	 */
	public void setEmptyDestroyTime(long time)
	{
		_emptyDestroyTime = time;
	}

	/**
	 * Checks if the player exists within this instance
	 *
	 * @param objectId
	 * @return true if player exists in instance
	 */
	public boolean containsPlayer(int objectId)
	{
		return _players.contains(objectId);
	}

	/**
	 * Adds the specified player to the instance
	 *
	 * @param objectId Players object ID
	 */
	public void addPlayer(int objectId)
	{
		synchronized (_players)
		{
			_players.add(objectId);
		}
	}

	/**
	 * Removes the specified player from the instance list
	 *
	 * @param objectId Players object ID
	 */
	public void removePlayer(int objectId)
	{
		synchronized (_players)
		{
			_players.remove(objectId);
		}

		if (_players.isEmpty() && _emptyDestroyTime >= 0)
		{
			_lastLeft = System.currentTimeMillis();
			setDuration((int) (_instanceEndTime - System.currentTimeMillis() - 500));
		}
	}

	/**
	 * Removes the player from the instance by setting InstanceId to 0 and teleporting to nearest town.
	 *
	 * @param objectId
	 */
	public void ejectPlayer(int objectId)
	{
		L2PcInstance player = L2World.getInstance().getPlayer(objectId);
		if (player != null && player.getInstanceId() == getId())
		{
			player.setInstanceId(0);
			player.sendMessage("You were removed from the instance");
			if (getSpawnLoc()[0] != 0 && getSpawnLoc()[1] != 0 && getSpawnLoc()[2] != 0)
			{
				player.teleToLocation(getSpawnLoc()[0], getSpawnLoc()[1], getSpawnLoc()[2]);
			}
			else
			{
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
	}

	public void addNpc(L2Npc npc)
	{
		_npcs.add(npc);
	}

	public void removeNpc(L2Npc npc)
	{
		if (npc.getSpawn() != null)
		{
			npc.getSpawn().stopRespawn();
		}
		//npc.deleteMe();
		_npcs.remove(npc);
	}

	/**
	 * Adds a door into the instance
	 *
	 * @param doorId - from doors.csv
	 */
	public void addDoor(int doorId, StatsSet set)
	{
		if (_doors == null)
		{
			_doors = new ArrayList<>(2);
		}

		for (L2DoorInstance door : _doors)
		{
			if (door.getDoorId() == doorId)
			{
				Log.warning("Door ID " + doorId + " already exists in instance " + getId());
				return;
			}
		}

		L2DoorTemplate temp = DoorTable.getInstance().getDoorTemplate(doorId);
		L2DoorInstance newdoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), temp, set);
		newdoor.setInstanceId(getId());
		newdoor.setCurrentHp(newdoor.getMaxHp());
		newdoor.spawnMe(temp.posX, temp.posY, temp.posZ);
		_doors.add(newdoor);
	}

	public TIntHashSet getPlayers()
	{
		return _players;
	}

	public CopyOnWriteArrayList<L2Npc> getNpcs()
	{
		return _npcs;
	}

	public ArrayList<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public L2DoorInstance getDoor(int id)
	{
		for (L2DoorInstance temp : getDoors())
		{
			if (temp.getDoorId() == id)
			{
				return temp;
			}
		}
		return null;
	}

	public long getInstanceEndTime()
	{
		return _instanceEndTime;
	}

	public long getInstanceStartTime()
	{
		return _instanceStartTime;
	}

	public boolean isShowTimer()
	{
		return _showTimer;
	}

	public boolean isTimerIncrease()
	{
		return _isTimerIncrease;
	}

	public String getTimerText()
	{
		return _timerText;
	}

	/**
	 * Returns the spawn location for this instance to be used when leaving the instance
	 *
	 * @return int[3]
	 */
	public int[] getSpawnLoc()
	{
		return _spawnLoc;
	}

	/**
	 * Sets the spawn location for this instance to be used when leaving the instance
	 */
	public void setSpawnLoc(int[] loc)
	{
		if (loc == null || loc.length < 3)
		{
			return;
		}
		System.arraycopy(loc, 0, _spawnLoc, 0, 3);
	}

	public void removePlayers()
	{
		_players.forEach(_ejectProc);

		synchronized (_players)
		{
			_players.clear();
		}
	}

	public void removeNpcs()
	{
		for (L2Npc mob : _npcs)
		{
			if (mob != null)
			{
				if (mob.getSpawn() != null)
				{
					mob.getSpawn().stopRespawn();
				}
				mob.deleteMe();
				mob.setInstanceId(0);
			}
		}
		_npcs.clear();
	}

	public void removeDoors()
	{
		if (_doors == null)
		{
			return;
		}

		for (L2DoorInstance door : _doors)
		{
			if (door != null)
			{
				L2WorldRegion region = door.getWorldRegion();
				door.decayMe();

				if (region != null)
				{
					region.removeVisibleObject(door);
				}

				door.getKnownList().removeAllKnownObjects();
				L2World.getInstance().removeObject(door);
			}
		}
		_doors.clear();
		_doors = null;
	}

	public void loadInstanceTemplate(String filename)
	{
		File xml = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "instances/" + filename);

		try
		{
			XmlDocument doc = new XmlDocument(xml);
			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("instance"))
				{
					parseInstance(n);
				}
			}
		}
		catch (IOException e)
		{
			Log.log(Level.WARNING, "Instance: can not find " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Instance: error while loading " + xml.getAbsolutePath() + " ! " + e.getMessage(),
					e);
		}
	}

	private void parseInstance(XmlNode node) throws Exception
	{
		L2Spawn spawnDat;
		L2NpcTemplate npcTemplate;
		String name = node.getString("name");
		setName(name);

		for (XmlNode n : node.getChildren())
		{
			if (n.getName().equalsIgnoreCase("activityTime"))
			{
				if (n.hasAttribute("val"))
				{
					_CheckTimeUpTask = ThreadPoolManager.getInstance()
							.scheduleGeneral(new CheckTimeUp(n.getInt("val") * 60000), 15000);
					_instanceEndTime = System.currentTimeMillis() + n.getLong("val") * 60000 + 15000;
				}
			}
			/*			else if (n.getName().equalsIgnoreCase("timeDelay"))
                        {
							a = n.getString("val");
							if (a != null)
								instance.setTimeDelay(Integer.parseInt(a);
						}*/
			else if (n.getName().equalsIgnoreCase("allowSummon"))
			{
				if (n.hasAttribute("val"))
				{
					setAllowSummon(n.getBool("val"));
				}
			}
			else if (n.getName().equalsIgnoreCase("emptyDestroyTime"))
			{
				if (n.hasAttribute("val"))
				{
					_emptyDestroyTime = n.getLong("val") * 1000;
				}
			}
			else if (n.getName().equalsIgnoreCase("showTimer"))
			{
				if (n.hasAttribute("val"))
				{
					_showTimer = n.getBool("val");
				}
				if (n.hasAttribute("increase"))
				{
					_isTimerIncrease = n.getBool("increase");
				}
				if (n.hasAttribute("text"))
				{
					_timerText = n.getString("text");
				}
			}
			else if (n.getName().equalsIgnoreCase("PvPInstance"))
			{
				if (n.hasAttribute("val"))
				{
					setPvPInstance(n.getBool("val"));
				}
			}
			else if (n.getName().equalsIgnoreCase("doorlist"))
			{
				for (XmlNode d : n.getChildren())
				{
					int doorId = 0;
					if (d.getName().equalsIgnoreCase("door"))
					{
						doorId = d.getInt("doorId");
						StatsSet set = new StatsSet();
						for (XmlNode bean : d.getChildren())
						{
							if (bean.getName().equalsIgnoreCase("set"))
							{
								String setname = d.getString("name");
								String value = d.getString("val");
								set.set(setname, value);
							}
						}
						addDoor(doorId, set);
					}
				}
			}
			else if (n.getName().equalsIgnoreCase("spawnlist"))
			{
				for (XmlNode d : n.getChildren())
				{
					int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0, delay = -1;

					if (d.getName().equalsIgnoreCase("spawn"))
					{

						npcId = d.getInt("npcId");
						x = d.getInt("x");
						y = d.getInt("y");
						z = d.getInt("z");
						heading = d.getInt("heading");
						respawn = d.getInt("respawn");
						if (d.hasAttribute("onKillDelay"))
						{
							delay = d.getInt("onKillDelay");
						}

						npcTemplate = NpcTable.getInstance().getTemplate(npcId);
						if (npcTemplate != null)
						{
							spawnDat = new L2Spawn(npcTemplate);
							spawnDat.setX(x);
							spawnDat.setY(y);
							spawnDat.setZ(z);
							spawnDat.setHeading(heading);
							spawnDat.setRespawnDelay(respawn);
							if (respawn == 0)
							{
								spawnDat.stopRespawn();
							}
							else
							{
								spawnDat.startRespawn();
							}
							spawnDat.setInstanceId(getId());
							L2Npc spawned = spawnDat.getNpc();
							spawnDat.doSpawn();
							if (delay >= 0 && spawned instanceof L2Attackable)
							{
								((L2Attackable) spawned).setOnKillDelay(delay);
							}
						}
						else
						{
							Log.warning(
									"Instance: Data missing in NPC table for ID: " + npcId + " in Instance " + getId());
						}
					}
				}
			}
			else if (n.getName().equalsIgnoreCase("spawnpoint"))
			{
				try
				{
					_spawnLoc[0] = n.getInt("spawnX");
					_spawnLoc[1] = n.getInt("spawnY");
					_spawnLoc[2] = n.getInt("spawnZ");
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Error parsing instance xml: " + e.getMessage(), e);
					_spawnLoc = new int[3];
				}
			}
		}
		if (Config.DEBUG)
		{
			Log.info(name + " Instance Template for Instance " + getId() + " loaded");
		}
	}

	protected void doCheckTimeUp(int remaining)
	{
		CreatureSay cs = null;
		int timeLeft;
		int interval;

		if (_players.isEmpty() && _emptyDestroyTime == 0)
		{
			remaining = 0;
			interval = 500;
		}
		else if (_players.isEmpty() && _emptyDestroyTime > 0)
		{

			Long emptyTimeLeft = _lastLeft + _emptyDestroyTime - System.currentTimeMillis();
			if (emptyTimeLeft <= 0)
			{
				interval = 0;
				remaining = 0;
			}
			else if (remaining > 300000 && emptyTimeLeft > 300000)
			{
				interval = 300000;
				remaining = remaining - 300000;
			}
			else if (remaining > 60000 && emptyTimeLeft > 60000)
			{
				interval = 60000;
				remaining = remaining - 60000;
			}
			else if (remaining > 30000 && emptyTimeLeft > 30000)
			{
				interval = 30000;
				remaining = remaining - 30000;
			}
			else
			{
				interval = 10000;
				remaining = remaining - 10000;
			}
		}
		else if (remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 300000;
		}
		else if (remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 60000;
		}
		else if (remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new CreatureSay(0, Say2.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 10000;
		}
		if (cs != null)
		{
			_players.forEach(new SendPacketToPlayerProcedure(cs));
		}

		cancelTimer();
		//System.out.println(_id + " (" + getName() + "): " + remaining + " " + interval);
		if (remaining >= 10000)
		{
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		}
		else
		{
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
		}
	}

	public void cancelTimer()
	{
		if (_CheckTimeUpTask != null)
		{
			_CheckTimeUpTask.cancel(true);
		}
	}

	public class CheckTimeUp implements Runnable
	{
		private int _remaining;

		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}

		@Override
		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}

	public class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			InstanceManager.getInstance().destroyInstance(getId());
		}
	}

	private final class EjectPlayerProcedure implements TIntProcedure
	{
		EjectPlayerProcedure()
		{

		}

		@Override
		public final boolean execute(final int objId)
		{
			ejectPlayer(objId);
			return true;
		}
	}

	private final class SendPacketToPlayerProcedure implements TIntProcedure
	{
		private final L2GameServerPacket _packet;

		SendPacketToPlayerProcedure(final L2GameServerPacket packet)
		{
			_packet = packet;
		}

		@Override
		public final boolean execute(final int objId)
		{
			L2PcInstance player = L2World.getInstance().getPlayer(objId);

			if (player.getInstanceId() == getId())
			{
				player.sendPacket(_packet);
			}
			return true;
		}
	}
}
