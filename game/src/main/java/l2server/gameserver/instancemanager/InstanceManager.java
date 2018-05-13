/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.DatabasePool;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.ScenePlayerDataTable;
import l2server.gameserver.model.L2CommandChannel;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExStartScenePlayer;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.gameserver.util.Util;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author evill33t, GodKratos
 */
public class InstanceManager {
	
	private static Logger log = LoggerFactory.getLogger(InstanceManager.class.getName());
	
	private ConcurrentHashMap<Integer, Instance> instanceList = new ConcurrentHashMap<>();
	private HashMap<Integer, InstanceWorld> instanceWorlds = new HashMap<>();
	private int dynamic = 300000;
	
	// InstanceId Names
	private static final Map<Integer, String> instanceIdNames = new HashMap<>();
	private Map<Integer, Map<Integer, Long>> playerInstanceTimes = new HashMap<>();
	
	private static final String ADD_INSTANCE_TIME =
			"INSERT INTO character_instance_time (charId,instanceId,time) VALUES (?,?,?) ON DUPLICATE KEY UPDATE time=?";
	private static final String RESTORE_INSTANCE_TIMES = "SELECT instanceId,time FROM character_instance_time WHERE charId=?";
	private static final String DELETE_INSTANCE_TIME = "DELETE FROM character_instance_time WHERE charId=? AND instanceId=?";
	
	public long getInstanceTime(int playerObjId, int id) {
		if (!playerInstanceTimes.containsKey(playerObjId)) {
			restoreInstanceTimes(playerObjId);
		}
		if (playerInstanceTimes.get(playerObjId).containsKey(id)) {
			return playerInstanceTimes.get(playerObjId).get(id);
		}
		return -1;
	}
	
	public Map<Integer, Long> getAllInstanceTimes(int playerObjId) {
		if (!playerInstanceTimes.containsKey(playerObjId)) {
			restoreInstanceTimes(playerObjId);
		}
		return playerInstanceTimes.get(playerObjId);
	}
	
	public void setInstanceTime(int playerObjId, int id, long time) {
		if (!playerInstanceTimes.containsKey(playerObjId)) {
			restoreInstanceTimes(playerObjId);
		}
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = null;
			statement = con.prepareStatement(ADD_INSTANCE_TIME);
			statement.setInt(1, playerObjId);
			statement.setInt(2, id);
			statement.setLong(3, time);
			statement.setLong(4, time);
			statement.execute();
			statement.close();
			playerInstanceTimes.get(playerObjId).put(id, time);
		} catch (Exception e) {
			log.warn("Could not insert character instance time data: " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	public void deleteInstanceTime(int playerObjId, int id) {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = null;
			statement = con.prepareStatement(DELETE_INSTANCE_TIME);
			statement.setInt(1, playerObjId);
			statement.setInt(2, id);
			statement.execute();
			statement.close();
			playerInstanceTimes.get(playerObjId).remove(id);
		} catch (Exception e) {
			log.warn("Could not delete character instance time data: " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	public void restoreInstanceTimes(int playerObjId) {
		if (playerInstanceTimes.containsKey(playerObjId)) {
			return; // already restored
		}
		playerInstanceTimes.put(playerObjId, new HashMap<>());
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_INSTANCE_TIMES);
			statement.setInt(1, playerObjId);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next()) {
				int id = rset.getInt("instanceId");
				long time = rset.getLong("time");
				if (time < System.currentTimeMillis()) {
					deleteInstanceTime(playerObjId, id);
				} else {
					playerInstanceTimes.get(playerObjId).put(id, time);
				}
			}
			
			rset.close();
			statement.close();
		} catch (Exception e) {
			log.warn("Could not delete character instance time data: " + e.getMessage(), e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	public String getInstanceIdName(int id) {
		if (instanceIdNames.containsKey(id)) {
			return instanceIdNames.get(id);
		}
		return "UnknownInstance";
	}
	
	private void loadInstanceNames() {
		try {
			XmlDocument doc = new XmlDocument(new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "instancenames.xml"));
			for (XmlNode node : doc.getChildren()) {
				if (node.getName().equals("instance")) {
					int id = node.getInt("id");
					String name = node.getString("name");
					instanceIdNames.put(id, name);
				}
			}
		} catch (Exception e) {
			log.warn("Error while loading instance names: " + e.getMessage(), e);
		}
	}
	
	public static class InstanceWorld {
		public int instanceId;
		public int templateId = -1;
		public ArrayList<Integer> allowed = new ArrayList<>();
		public volatile int status;
	}
	
	public void addWorld(InstanceWorld world) {
		instanceWorlds.put(world.instanceId, world);
	}
	
	public InstanceWorld getWorld(int instanceId) {
		return instanceWorlds.get(instanceId);
	}
	
	public InstanceWorld getPlayerWorld(Player player) {
		for (InstanceWorld temp : instanceWorlds.values()) {
			if (temp == null) {
				continue;
			}
			// check if the player have a World Instance where he/she is allowed to enter
			if (temp.allowed.contains(player.getObjectId())) {
				return temp;
			}
		}
		
		return null;
	}
	
	private InstanceManager() {
	}
	
	@Load
	private void load() {
		log.debug("Initializing InstanceManager");
		loadInstanceNames();
		log.info("Loaded " + instanceIdNames.size() + " instance names");
		createWorld();
	}
	
	public static InstanceManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private void createWorld() {
		Instance themultiverse = new Instance(-1);
		themultiverse.setName("multiverse");
		instanceList.put(-1, themultiverse);
		log.info("Multiverse Instance created");
		
		Instance universe = new Instance(0);
		universe.setName("universe");
		instanceList.put(0, universe);
		log.info("Universe Instance created");
	}
	
	public void destroyInstance(int instanceid) {
		if (instanceid <= 0) {
			return;
		}
		Instance temp = instanceList.get(instanceid);
		if (temp != null) {
			temp.removeNpcs();
			temp.removePlayers();
			temp.removeDoors();
			temp.cancelTimer();
			instanceList.remove(instanceid);
			if (instanceWorlds.containsKey(instanceid)) {
				instanceWorlds.remove(instanceid);
			}
		}
	}
	
	public Instance getInstance(int instanceid) {
		return instanceList.get(instanceid);
	}
	
	public ConcurrentHashMap<Integer, Instance> getInstances() {
		return instanceList;
	}
	
	public int getPlayerInstance(int objectId) {
		for (Instance temp : instanceList.values()) {
			if (temp == null) {
				continue;
			}
			// check if the player is in any active instance
			if (temp.containsPlayer(objectId)) {
				return temp.getId();
			}
		}
		// 0 is default instance aka the world
		return 0;
	}
	
	public boolean createInstance(int id) {
		if (getInstance(id) != null) {
			return false;
		}
		
		Instance instance = new Instance(id);
		instanceList.put(id, instance);
		return true;
	}
	
	public boolean createInstanceFromTemplate(int id, String template) throws FileNotFoundException {
		if (getInstance(id) != null) {
			return false;
		}
		
		Instance instance = new Instance(id);
		instanceList.put(id, instance);
		instance.loadInstanceTemplate(template);
		return true;
	}
	
	/**
	 * Create a new instance with a dynamic instance id based on a template (or null)
	 *
	 * @param template xml file
	 */
	public int createDynamicInstance(String template) {
		
		while (getInstance(dynamic) != null) {
			dynamic++;
			if (dynamic == Integer.MAX_VALUE) {
				log.warn("InstanceManager: More then " + (Integer.MAX_VALUE - 300000) + " instances created");
				dynamic = 300000;
			}
		}
		Instance instance = new Instance(dynamic);
		instanceList.put(dynamic, instance);
		if (template != null) {
			try {
				instance.loadInstanceTemplate(template);
			} catch (Exception e) {
				log.warn("InstanceManager: Failed creating instance from template " + template + ", " + e.getMessage(), e);
			}
		}
		return dynamic;
	}
	
	public void showVidToInstance(int vidId, final int instId) {
		stopWholeInstance(instId);
		broadcastMovie(vidId, instId);
		
		ThreadPoolManager.getInstance()
				.scheduleGeneral(() -> startWholeInstance(instId), ScenePlayerDataTable.getInstance().getVideoDuration(vidId) + 1000);
	}
	
	public void stopWholeInstance(int instId) {
		for (Npc mobs : getInstance(instId).getNpcs()) {
			if (mobs == null || !(mobs instanceof Attackable)) {
				continue;
			}
			
			mobs.setTarget(null);
			mobs.abortAttack();
			mobs.abortCast();
			mobs.disableAllSkills();
			mobs.stopMove(null);
			mobs.setIsInvul(true);
			mobs.setIsImmobilized(true);
		}
		
		for (Player pl : World.getInstance().getAllPlayers().values()) {
			if (pl != null && pl.getInstanceId() == instId && !pl.isGM()) {
				pl.setIsImmobilized(true);
				pl.setTarget(null);
				pl.disableAllSkills();
				pl.setIsInvul(true);
				pl.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
		}
	}
	
	public void broadcastMovie(int vidId, int instId) {
		for (Player pl : World.getInstance().getAllPlayers().values()) {
			if (pl != null && pl.getInstanceId() == instId) {
				pl.setMovieId(vidId);
				pl.sendPacket(new ExStartScenePlayer(vidId));
			}
		}
	}
	
	public void startWholeInstance(int instId) {
		Instance inst = getInstance(instId);
		if (inst == null) {
			return;
		}
		for (Npc mobs : inst.getNpcs()) {
			if (mobs == null || !(mobs instanceof Attackable)) {
				continue;
			}
			
			mobs.setIsInvul(false);
			mobs.enableAllSkills();
			mobs.setIsImmobilized(false);
		}
		
		for (Player pl : World.getInstance().getAllPlayers().values()) {
			if (pl != null && pl.getInstanceId() == instId && !pl.isGM()) {
				pl.enableAllSkills();
				pl.setIsInvul(false);
				pl.setIsImmobilized(false);
			}
		}
	}
	
	public void sendPacket(int instanceId, L2GameServerPacket packet) {
		for (Player player : World.getInstance().getAllPlayersArray()) {
			if (player != null && player.isOnline() && player.getInstanceId() == instanceId) {
				player.sendPacket(packet);
			}
		}
	}
	
	public void sendDelayedPacketToInstance(final int instanceId, final int delaySec, final L2GameServerPacket packet) {
		ThreadPoolManager.getInstance().scheduleGeneral(() -> sendPacket(instanceId, packet), delaySec * 1000);
	}
	
	public void sendDelayedPacketToPlayer(final Player player, int delaySec, final int instanceId, final L2GameServerPacket packet) {
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if (player != null && player.getInstanceId() == instanceId) {
				player.sendPacket(packet);
			}
		}, delaySec * 1000);
	}
	
	public List<Player> getPlayers(int instanceId) {
		List<Player> instancePlayers = new ArrayList<>();
		for (Player player : World.getInstance().getAllPlayersArray()) {
			if (player != null && player.getInstanceId() == instanceId) {
				instancePlayers.add(player);
			}
		}
		return instancePlayers;
	}
	
	private long calcInstanceReuse(boolean isHard) {
		Calendar now = Calendar.getInstance();
		Calendar reenterPointWed = (Calendar) now.clone();
		reenterPointWed.set(Calendar.MINUTE, 30);
		reenterPointWed.set(Calendar.HOUR_OF_DAY, 6);
		reenterPointWed.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		
		Calendar reenterPointSat = (Calendar) reenterPointWed.clone();
		reenterPointSat.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		
		if (now.after(reenterPointWed)) {
			reenterPointWed.add(Calendar.WEEK_OF_MONTH, 1);
		}
		if (now.after(reenterPointSat)) {
			reenterPointSat.add(Calendar.WEEK_OF_MONTH, 1);
		}
		
		Calendar reenter = reenterPointWed;
		if (!isHard && reenterPointSat.before(reenterPointWed)) {
			reenter = reenterPointSat;
		}
		
		return reenter.getTimeInMillis();
	}
	
	public void despawnAll(int instId) {
		if (getInstance(instId) == null || getInstance(instId).getNpcs() == null) {
			return;
		}
		
		for (Npc npc : getInstance(instId).getNpcs()) {
			if (npc == null) {
				continue;
			}
			
			L2Spawn spawn = npc.getSpawn();
			if (spawn != null && spawn.getNpc() != null) {
				spawn.stopRespawn();
			}
			
			npc.deleteMe();
		}
	}
	
	public void finishInstance(int instId, boolean despawnAll) {
		if (despawnAll) {
			despawnAll(instId);
		}
		
		Instance inst = getInstance(instId);
		if (inst != null) {
			inst.setDuration(300000);
		}
	}
	
	/**
	 * @param isHard     (true: every Wednesday at 6.30 AM, otherwise: every Wednesday and Saturday at 6.30 AM)
	 */
	public void setInstanceReuse(int instanceId, int templateId, boolean isHard) {
		InstanceWorld instance = instanceWorlds.get(instanceId);
		if (instance != null) {
			for (int playerId : instance.allowed) {
				long instanceReuse = calcInstanceReuse(isHard);
				setInstanceTime(playerId, templateId, instanceReuse);
			}
		}
	}
	
	/**
	 * @param reuseTime  in minutes
	 */
	public void setInstanceReuse(int instanceId, int templateId, int reuseTime) {
		InstanceWorld instance = instanceWorlds.get(instanceId);
		if (instance != null) {
			for (int playerId : instance.allowed) {
				setInstanceTime(playerId, templateId, System.currentTimeMillis() + reuseTime * 60000);
			}
		}
	}
	
	public void setInstanceReuse(int instanceId, int templateId, int hour, int minute) {
		InstanceWorld instance = instanceWorlds.get(instanceId);
		if (instance != null) {
			Calendar reenter = Calendar.getInstance();
			reenter.set(Calendar.MINUTE, minute);
			if (reenter.get(Calendar.HOUR_OF_DAY) >= hour) {
				reenter.add(Calendar.DATE, 1);
			}
			reenter.set(Calendar.HOUR_OF_DAY, hour);
			
			for (int playerId : instance.allowed) {
				setInstanceTime(playerId, templateId, reenter.getTimeInMillis());
			}
		}
	}
	
	/**
	 * Under test
	 *
	 */
	public boolean canGetUniqueReward(Player player, ArrayList<Player> rewardedPlayers) {
		if (player == null) {
			return false;
		}
		
		for (Player players : rewardedPlayers) {
			if (players == null) {
				continue;
			}
			
			if (players == player) {
				return false;
			}
			
			if (players.getExternalIP().equalsIgnoreCase(player.getExternalIP()) &&
					players.getInternalIP().equalsIgnoreCase(player.getInternalIP())) {
				return false;
			}
			
			//	if (players.getHWID().equalsIgnoreCase(player.getHWID()) && players.getInternalIP().equalsIgnoreCase(player.getInternalIP()))
			//	return false;
		}
		return true;
	}
	
	public boolean checkInstanceConditions(Player player, int templateId, int minPlayers, int maxPlayers, int minLevel, int maxLevel) {
		if (player == null) {
			return false;
		}
		
		List<Player> allPlayers = new ArrayList<>();
		L2Party party = null;
		L2CommandChannel cChannel = null;
		
		if (minPlayers == 1 && maxPlayers == 1) {
			allPlayers.add(player);
		} else if (minPlayers > 1) {
			party = player.getParty();
			if (party == null) {
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
				return false;
			}
			
			cChannel = player.getParty().getCommandChannel();
			if (cChannel == null) {
				if (minPlayers <= Config.MAX_MEMBERS_IN_PARTY) {
					if (party.getLeader() != player) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
						return false;
					}
					
					if (party.getMemberCount() < minPlayers) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER)
								.addNumber(minPlayers));
						return false;
					} else {
						allPlayers.addAll(party.getPartyMembers());
					}
				}
			}
			
			if (minPlayers > Config.MAX_MEMBERS_IN_PARTY || cChannel != null) {
				if (minPlayers > Config.MAX_MEMBERS_IN_PARTY) //Need command channel yes or yes
				{
					if (cChannel == null) {
						player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
						return false;
					}
				}
				if (cChannel != null) {
					if (cChannel.getChannelLeader() != player) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
						return false;
					}
					if (cChannel.getMemberCount() < minPlayers) {
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_HAVE_MINIMUM_OF_S1_PEOPLE_TO_ENTER)
								.addNumber(minPlayers));
						return false;
					}
					if (cChannel.getMemberCount() > maxPlayers) {
						player.sendMessage("You exceeded the number of allowed participants.");
						return false;
					}
				}
				allPlayers.addAll(cChannel.getMembers());
			}
		}
		
		//List checks
		for (Player enterPlayer : allPlayers) {
			if (enterPlayer == null) {
				continue;
			}
			
			if (enterPlayer.isInDuel()) {
				return false;
			}
			
			if (enterPlayer.getLevel() < minLevel || enterPlayer.getLevel() > maxLevel) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(enterPlayer);
				
				if (party != null) {
					party.broadcastToPartyMembers(sm);
				} else {
					enterPlayer.sendPacket(sm);
				}
				
				return false;
			}
			
			if (!Util.checkIfInRange(1000, player, enterPlayer, true)) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(enterPlayer);
				
				if (party != null) {
					party.broadcastToPartyMembers(sm);
				} else {
					enterPlayer.sendPacket(sm);
				}
				
				return false;
			}
			
			Long reentertime = getInstanceTime(enterPlayer.getObjectId(), templateId);
			if (System.currentTimeMillis() < reentertime) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(enterPlayer);
				
				if (party != null) {
					party.broadcastToPartyMembers(sm);
				} else {
					enterPlayer.sendPacket(sm);
				}
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE d MMMMMMM kk:mm:ss");
				
				player.sendMessage("Right now, it is " + dateFormat.format(System.currentTimeMillis()));
				player.sendMessage("You will be able to re-enter at " + dateFormat.format(reentertime));
				
				return false;
			}
		}
		
		Broadcast.toGameMasters(player.getName() + " is entering Instance[" + templateId + "] with:");
		
		for (Player enterPlayer : allPlayers) {
			Broadcast.toGameMasters("- " + enterPlayer.getName());
		}
		
		return true;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final InstanceManager instance = new InstanceManager();
	}
}
