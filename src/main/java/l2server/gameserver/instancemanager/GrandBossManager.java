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

import gnu.trove.TIntIntHashMap;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.GrandBossInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.zone.type.BossZone;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author DaRkRaGe
 * Revised by Emperorc
 */
public class GrandBossManager {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(GrandBossManager.class.getName());


	/**
	 * DELETE FROM grandboss_list
	 */
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
	
	/**
	 * INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)
	 */
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	
	/**
	 * UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?
	 */
	private static final String UPDATE_GRAND_BOSS_DATA =
			"UPDATE grandboss_data SET loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? WHERE boss_id = ?";
	
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data SET status = ? WHERE boss_id = ?";
	
	protected static Map<Integer, GrandBossInstance> bosses;
	
	protected static Map<Integer, StatsSet> storedInfo;
	
	private TIntIntHashMap bossStatus;
	
	private ArrayList<BossZone> zones;
	
	public final int ALIVE = 0;
	public final int WAITING = 1;
	public final int FIGHTING = 2;
	public final int DEAD = 3;
	
	public static GrandBossManager getInstance() {
		return SingletonHolder.instance;
	}
	
	private GrandBossManager() {
	}
	
	@Load(dependencies = {NpcTable.class})
	public void load() {
		log.info("Initializing GrandBossManager");
		zones = new ArrayList<>();
		
		bosses = new HashMap<>();
		storedInfo = new HashMap<>();
		bossStatus = new TIntIntHashMap();
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT * FROM grandboss_data ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next()) {
				//Read all info from DB, and store it for AI to read and decide what to do
				//faster than accessing DB in real time
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				NpcTemplate boss = NpcTable.getInstance().getTemplate(bossId);
				if (boss == null) {
					log.warn("Trying to create a grand boss which has no template! Boss id: " + bossId);
					continue;
				}
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP"); //jython doesn't recognize doubles
				int true_HP = (int) HP; //so use java's ability to type cast
				info.set("currentHP", true_HP); //to convert double to int
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				int status = rset.getInt("status");
				bossStatus.put(bossId, status);
				storedInfo.put(bossId, info);
				log.debug("GrandBossManager: " + boss.getName() + " (" + bossId + ") status is " + status + ".");
				if (status > 0) {
					log.debug("GrandBossManager: Next spawn date of " + boss.getName() + " is " + new Date(info.getLong("respawn_time")) + ".");
				}
				
				info = null;
			}
			
			log.info("GrandBossManager: Loaded " + storedInfo.size() + " Instances");
			
			rset.close();
			statement.close();
		} catch (SQLException e) {
			log.warn("GrandBossManager: Could not load grandboss_data table: " + e.getMessage(), e);
		} catch (Exception e) {
			log.warn("Error while initializing GrandBossManager: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
		
		initZones();
	}
	
	/*
	 * Zone Functions
	 */
	private void initZones() {
		Connection con = null;
		
		HashMap<Integer, ArrayList<Integer>> zones = new HashMap<>();
		
		for (BossZone zone : this.zones) {
			if (zone == null) {
				continue;
			}
			zones.put(zone.getId(), new ArrayList<>());
		}
		
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT * FROM grandboss_list ORDER BY player_id");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next()) {
				int id = rset.getInt("player_id");
				int zone_id = rset.getInt("zone");
				zones.get(zone_id).add(id);
			}
			
			rset.close();
			statement.close();
			
			log.info("GrandBossManager: Initialized " + zones.size() + " Grand Boss Zones");
		} catch (SQLException e) {
			log.warn("GrandBossManager: Could not load grandboss_list table: " + e.getMessage(), e);
		} catch (Exception e) {
			log.warn("Error while initializing GrandBoss zones: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
		
		for (BossZone zone : this.zones) {
			if (zone == null) {
				continue;
			}
			
			zone.setAllowedPlayers(zones.get(zone.getId()));
		}
		
		zones.clear();
	}
	
	public void addZone(BossZone zone) {
		if (zones != null) {
			zones.add(zone);
		}
	}
	
	public final BossZone getZone(Creature character) {
		if (zones != null) {
			for (BossZone temp : zones) {
				if (temp.isCharacterInZone(character)) {
					return temp;
				}
			}
		}
		return null;
	}
	
	public final BossZone getZone(int x, int y, int z) {
		if (zones != null) {
			for (BossZone temp : zones) {
				if (temp.isInsideZone(x, y, z)) {
					return temp;
				}
			}
		}
		return null;
	}
	
	public boolean checkIfInZone(String zoneType, WorldObject obj) {
		BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if (temp == null) {
			return false;
		}
		
		return temp.getName().equalsIgnoreCase(zoneType);
	}
	
	public boolean checkIfInZone(Player player) {
		if (player == null) {
			return false;
		}
		BossZone temp = getZone(player.getX(), player.getY(), player.getZ());
		return temp != null;
	}
	
	/*
	 * The rest
	 */
	public int getBossStatus(int bossId) {
		return bossStatus.get(bossId);
	}
	
	public void setBossStatus(int bossId, int status) {
		bossStatus.put(bossId, status);
		log.info(getClass().getSimpleName() + ": Updated " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" + bossId + ") status to " +
				status);
		updateDb(bossId, true);
	}
	
	/*
	 * Adds a GrandBossInstance to the list of bosses.
	 */
	public void addBoss(GrandBossInstance boss) {
		if (boss != null) {
			bosses.put(boss.getNpcId(), boss);
		}
	}
	
	public GrandBossInstance getBoss(int bossId) {
		return bosses.get(bossId);
	}
	
	public StatsSet getStatsSet(int bossId) {
		return storedInfo.get(bossId);
	}
	
	public void setStatsSet(int bossId, StatsSet info) {
		storedInfo.put(bossId, info);
		updateDb(bossId, false);
	}
	
	private void storeToDb() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement deleteStatement = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			deleteStatement.executeUpdate();
			deleteStatement.close();
			
			PreparedStatement insertStatement = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
			for (BossZone zone : zones) {
				if (zone == null) {
					continue;
				}
				Integer id = zone.getId();
				Set<Integer> list = zone.getAllowedPlayers();
				if (list == null || list.isEmpty()) {
					continue;
				}
				for (Integer player : list) {
					insertStatement.setInt(1, player);
					insertStatement.setInt(2, id);
					insertStatement.executeUpdate();
					insertStatement.clearParameters();
				}
			}
			insertStatement.close();
			
			PreparedStatement updateStatement1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
			PreparedStatement updateStatement2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
			for (Integer bossId : storedInfo.keySet()) {
				GrandBossInstance boss = bosses.get(bossId);
				StatsSet info = storedInfo.get(bossId);
				if (boss == null || info == null) {
					updateStatement1.setInt(1, bossStatus.get(bossId));
					updateStatement1.setInt(2, bossId);
					updateStatement1.executeUpdate();
					updateStatement1.clearParameters();
				} else {
					updateStatement2.setInt(1, boss.getX());
					updateStatement2.setInt(2, boss.getY());
					updateStatement2.setInt(3, boss.getZ());
					updateStatement2.setInt(4, boss.getHeading());
					updateStatement2.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead()) {
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					updateStatement2.setDouble(6, hp);
					updateStatement2.setDouble(7, mp);
					updateStatement2.setInt(8, bossStatus.get(bossId));
					updateStatement2.setInt(9, bossId);
					updateStatement2.executeUpdate();
					updateStatement2.clearParameters();
				}
			}
			updateStatement1.close();
			updateStatement2.close();
		} catch (SQLException e) {
			log.warn("GrandBossManager: Couldn't store grandbosses to database:" + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	private void updateDb(int bossId, boolean statusOnly) {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			GrandBossInstance boss = bosses.get(bossId);
			StatsSet info = storedInfo.get(bossId);
			
			if (statusOnly || boss == null || info == null) {
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
				statement.setInt(1, bossStatus.get(bossId));
				statement.setInt(2, bossId);
			} else {
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
				statement.setInt(1, boss.getX());
				statement.setInt(2, boss.getY());
				statement.setInt(3, boss.getZ());
				statement.setInt(4, boss.getHeading());
				statement.setLong(5, info.getLong("respawn_time"));
				double hp = boss.getCurrentHp();
				double mp = boss.getCurrentMp();
				if (boss.isDead()) {
					hp = boss.getMaxHp();
					mp = boss.getMaxMp();
				}
				statement.setDouble(6, hp);
				statement.setDouble(7, mp);
				statement.setInt(8, bossStatus.get(bossId));
				statement.setInt(9, bossId);
			}
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			log.warn("GrandBossManager: Couldn't update grandbosses to database:" + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
	
	/**
	 * Saves all Grand Boss info and then clears all info from memory,
	 * including all schedules.
	 */
	public void cleanUp() {
		storeToDb();
		
		bosses.clear();
		storedInfo.clear();
		bossStatus.clear();
		zones.clear();
	}
	
	public ArrayList<BossZone> getZones() {
		return zones;
	}
	
	//LasTravel
	
	/**
	 * @param npcId
	 * @return
	 */
	public int getRespawnTime(int npcId) {
		switch (npcId) {
			case 29001:
				return Config.QUEENANT_INTERVAL_SPAWN * 3600000;
			case 29006:
				return Config.CORE_INTERVAL_SPAWN * 3600000;
			case 29014:
			case 29022: // Zaken
				return Config.ORFEN_INTERVAL_SPAWN * 3600000;
			case 29068:
				return Config.ANTHARAS_INTERVAL_SPAWN * 3600000;
			case 29020:
				return Config.BAIUM_INTERVAL_SPAWN * 3600000;
			case 29028:
				return Config.VALAKAS_INTERVAL_SPAWN * 3600000;
			case 29240:
				return Config.LINDVIOR_INTERVAL_SPAWN * 3600000;
			case 26124:
				return Config.KELBIM_INTERVAL_SPAWN * 3600000;
			case 29303:
				return Config.HELIOS_INTERVAL_SPAWN * 360000;
			case 25286: //Anakim
				return (int) calcReuseFromDays(0, 21, Calendar.TUESDAY, 0, 16, Calendar.SATURDAY);
			case 25283: //Lilith
				return (int) calcReuseFromDays(0, 21, Calendar.THURSDAY, 0, 14, Calendar.SATURDAY);
		}
		return 0;
	}
	
	/**
	 * Used for reuses like: Tuesday (21:00) and Saturday (16:00) or Thursday (21:00) and Saturday (14:00)
	 *
	 * @param day1Minute
	 * @param day1Hour
	 * @param day1Day
	 * @param day2Minute
	 * @param day2Hour
	 * @param day2Day
	 * @return
	 */
	private long calcReuseFromDays(int day1Minute, int day1Hour, int day1Day, int day2Minute, int day2Hour, int day2Day) {
		Calendar now = Calendar.getInstance();
		Calendar day1 = (Calendar) now.clone();
		day1.set(Calendar.MINUTE, day1Minute);
		day1.set(Calendar.HOUR_OF_DAY, day1Hour);
		day1.set(Calendar.DAY_OF_WEEK, day1Day);
		
		Calendar day2 = (Calendar) day1.clone();
		day2.set(Calendar.MINUTE, day2Minute);
		day2.set(Calendar.HOUR_OF_DAY, day2Hour);
		day2.set(Calendar.DAY_OF_WEEK, day2Day);
		
		if (now.after(day1)) {
			day1.add(Calendar.WEEK_OF_MONTH, 1);
		}
		if (now.after(day2)) {
			day2.add(Calendar.WEEK_OF_MONTH, 1);
		}
		
		Calendar reenter = day1;
		if (day2.before(day1)) {
			reenter = day2;
		}
		
		return reenter.getTimeInMillis() - System.currentTimeMillis();
	}
	
	/**
	 * @param npcId
	 * @return
	 */
	public int getRandomRespawnTime(int npcId) {
		switch (npcId) {
			case 29001:
				return Config.QUEENANT_RANDOM_SPAWN * 3600000;
			case 29006:
				return Config.CORE_RANDOM_SPAWN * 3600000;
			case 29014:
			case 29022: // Zaken
				return Config.ORFEN_RANDOM_SPAWN * 3600000;
			case 29068:
				return Config.ANTHARAS_RANDOM_SPAWN * 3600000;
			case 29020:
				return Config.BAIUM_RANDOM_SPAWN * 3600000;
			case 29028:
				return Config.VALAKAS_RANDOM_SPAWN * 3600000;
			case 29303:
				return Config.HELIOS_RANDOM_SPAWN * 360000;
			case 29240:
				return Config.LINDVIOR_RANDOM_SPAWN * 3600000;
			case 26124:
				return Config.KELBIM_RANDOM_SPAWN * 3600000;
		}
		return 0;
	}
	
	/**
	 * @param bossId
	 * @return
	 */
	public long getUnlockTime(int bossId) {
		long respawnTime = 0;
		int bossStatus = getBossStatus(bossId);
		if (bossStatus != ALIVE) {
			StatsSet info = getStatsSet(bossId);
			respawnTime = info.getLong("respawn_time");
			
			if (bossStatus == DEAD) {
				if (respawnTime <= System.currentTimeMillis()) {
					return 1;
				} else {
					return respawnTime - System.currentTimeMillis();
				}
			} else {
				return 1000L;
			}
		}
		
		return respawnTime;
	}
	
	/**
	 * @param bossId
	 */
	public void notifyBossKilled(int bossId) {
		setBossStatus(bossId, DEAD);
		
		long respawnTime = (long) getRespawnTime(bossId) + Rnd.get(getRandomRespawnTime(bossId));
		
		StatsSet info = getStatsSet(bossId);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		setStatsSet(bossId, info);
	}
	
	/**
	 * @param bossId
	 * @param lastAction
	 * @return
	 */
	public boolean isActive(int bossId, long lastAction) {
		Long temp = System.currentTimeMillis() - lastAction;
		
		return temp <= 900000;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final GrandBossManager instance = new GrandBossManager();
	}
}
