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
import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2GrandBossInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.type.L2BossZone;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author DaRkRaGe
 *         Revised by Emperorc
 */
public class GrandBossManager
{
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
			"UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";

	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";

	protected static Logger _log = Logger.getLogger(GrandBossManager.class.getName());

	protected static Map<Integer, L2GrandBossInstance> _bosses;

	protected static TIntObjectHashMap<StatsSet> _storedInfo;

	private TIntIntHashMap _bossStatus;

	private ArrayList<L2BossZone> _zones;

	public final int ALIVE = 0;
	public final int WAITING = 1;
	public final int FIGHTING = 2;
	public final int DEAD = 3;

	public static GrandBossManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private GrandBossManager()
	{
		Log.info("Initializing GrandBossManager");
		init();
	}

	private void init()
	{
		_zones = new ArrayList<>();

		_bosses = new HashMap<>();
		_storedInfo = new TIntObjectHashMap<>();
		_bossStatus = new TIntIntHashMap();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				//Read all info from DB, and store it for AI to read and decide what to do
				//faster than accessing DB in real time
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				L2NpcTemplate boss = NpcTable.getInstance().getTemplate(bossId);
				if (boss == null)
				{
					Log.warning("Trying to create a grand boss which has no template! Boss id: " + bossId);
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
				_bossStatus.put(bossId, status);
				_storedInfo.put(bossId, info);
				Log.fine("GrandBossManager: " + boss.getName() + " (" + bossId + ") status is " + status + ".");
				if (status > 0)
				{
					Log.fine("GrandBossManager: Next spawn date of " + boss.getName() + " is " +
							new Date(info.getLong("respawn_time")) + ".");
				}

				info = null;
			}

			Log.info("GrandBossManager: Loaded " + _storedInfo.size() + " Instances");

			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "GrandBossManager: Could not load grandboss_data table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while initializing GrandBossManager: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/*
	 * Zone Functions
	 */
	public void initZones()
	{
		Connection con = null;

		HashMap<Integer, ArrayList<Integer>> zones = new HashMap<>();

		if (_zones == null)
		{
			Log.warning("GrandBossManager: Could not read Grand Boss zone data");
			return;
		}

		for (L2BossZone zone : _zones)
		{
			if (zone == null)
			{
				continue;
			}
			zones.put(zone.getId(), new ArrayList<>());
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT * from grandboss_list ORDER BY player_id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int id = rset.getInt("player_id");
				int zone_id = rset.getInt("zone");
				zones.get(zone_id).add(id);
			}

			rset.close();
			statement.close();

			Log.info("GrandBossManager: Initialized " + _zones.size() + " Grand Boss Zones");
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "GrandBossManager: Could not load grandboss_list table: " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while initializing GrandBoss zones: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		for (L2BossZone zone : _zones)
		{
			if (zone == null)
			{
				continue;
			}

			zone.setAllowedPlayers(zones.get(zone.getId()));
		}

		zones.clear();
	}

	public void addZone(L2BossZone zone)
	{
		if (_zones != null)
		{
			_zones.add(zone);
		}
	}

	public final L2BossZone getZone(L2Character character)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isCharacterInZone(character))
				{
					return temp;
				}
			}
		}
		return null;
	}

	public final L2BossZone getZone(int x, int y, int z)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isInsideZone(x, y, z))
				{
					return temp;
				}
			}
		}
		return null;
	}

	public boolean checkIfInZone(String zoneType, L2Object obj)
	{
		L2BossZone temp = getZone(obj.getX(), obj.getY(), obj.getZ());
		if (temp == null)
		{
			return false;
		}

		return temp.getName().equalsIgnoreCase(zoneType);
	}

	public boolean checkIfInZone(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}
		L2BossZone temp = getZone(player.getX(), player.getY(), player.getZ());
		return temp != null;

	}

	/*
	 * The rest
	 */
	public int getBossStatus(int bossId)
	{
		return _bossStatus.get(bossId);
	}

	public void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		Log.info(
				getClass().getSimpleName() + ": Updated " + NpcTable.getInstance().getTemplate(bossId).getName() + "(" +
						bossId + ") status to " + status);
		updateDb(bossId, true);
	}

	/*
	 * Adds a L2GrandBossInstance to the list of bosses.
	 */
	public void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
		{
			_bosses.put(boss.getNpcId(), boss);
		}
	}

	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}

	public StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}

	public void setStatsSet(int bossId, StatsSet info)
	{
		_storedInfo.put(bossId, info);
		updateDb(bossId, false);
	}

	private void storeToDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement deleteStatement = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			deleteStatement.executeUpdate();
			deleteStatement.close();

			PreparedStatement insertStatement = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
			for (L2BossZone zone : _zones)
			{
				if (zone == null)
				{
					continue;
				}
				Integer id = zone.getId();
				Set<Integer> list = zone.getAllowedPlayers();
				if (list == null || list.isEmpty())
				{
					continue;
				}
				for (Integer player : list)
				{
					insertStatement.setInt(1, player);
					insertStatement.setInt(2, id);
					insertStatement.executeUpdate();
					insertStatement.clearParameters();
				}
			}
			insertStatement.close();

			PreparedStatement updateStatement1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
			PreparedStatement updateStatement2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
			for (Integer bossId : _storedInfo.keys())
			{
				L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if (boss == null || info == null)
				{
					updateStatement1.setInt(1, _bossStatus.get(bossId));
					updateStatement1.setInt(2, bossId);
					updateStatement1.executeUpdate();
					updateStatement1.clearParameters();
				}
				else
				{
					updateStatement2.setInt(1, boss.getX());
					updateStatement2.setInt(2, boss.getY());
					updateStatement2.setInt(3, boss.getZ());
					updateStatement2.setInt(4, boss.getHeading());
					updateStatement2.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					updateStatement2.setDouble(6, hp);
					updateStatement2.setDouble(7, mp);
					updateStatement2.setInt(8, _bossStatus.get(bossId));
					updateStatement2.setInt(9, bossId);
					updateStatement2.executeUpdate();
					updateStatement2.clearParameters();
				}
			}
			updateStatement1.close();
			updateStatement2.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "GrandBossManager: Couldn't store grandbosses to database:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void updateDb(int bossId, boolean statusOnly)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			L2GrandBossInstance boss = _bosses.get(bossId);
			StatsSet info = _storedInfo.get(bossId);

			if (statusOnly || boss == null || info == null)
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
				statement.setInt(1, _bossStatus.get(bossId));
				statement.setInt(2, bossId);
			}
			else
			{
				statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
				statement.setInt(1, boss.getX());
				statement.setInt(2, boss.getY());
				statement.setInt(3, boss.getZ());
				statement.setInt(4, boss.getHeading());
				statement.setLong(5, info.getLong("respawn_time"));
				double hp = boss.getCurrentHp();
				double mp = boss.getCurrentMp();
				if (boss.isDead())
				{
					hp = boss.getMaxHp();
					mp = boss.getMaxMp();
				}
				statement.setDouble(6, hp);
				statement.setDouble(7, mp);
				statement.setInt(8, _bossStatus.get(bossId));
				statement.setInt(9, bossId);
			}
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "GrandBossManager: Couldn't update grandbosses to database:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Saves all Grand Boss info and then clears all info from memory,
	 * including all schedules.
	 */
	public void cleanUp()
	{
		storeToDb();

		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
	}

	public ArrayList<L2BossZone> getZones()
	{
		return _zones;
	}

	//LasTravel

	/**
	 * @param npcId
	 * @return
	 */
	public int getRespawnTime(int npcId)
	{
		switch (npcId)
		{
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
	private long calcReuseFromDays(int day1Minute, int day1Hour, int day1Day, int day2Minute, int day2Hour, int day2Day)
	{
		Calendar now = Calendar.getInstance();
		Calendar day1 = (Calendar) now.clone();
		day1.set(Calendar.MINUTE, day1Minute);
		day1.set(Calendar.HOUR_OF_DAY, day1Hour);
		day1.set(Calendar.DAY_OF_WEEK, day1Day);

		Calendar day2 = (Calendar) day1.clone();
		day2.set(Calendar.MINUTE, day2Minute);
		day2.set(Calendar.HOUR_OF_DAY, day2Hour);
		day2.set(Calendar.DAY_OF_WEEK, day2Day);

		if (now.after(day1))
		{
			day1.add(Calendar.WEEK_OF_MONTH, 1);
		}
		if (now.after(day2))
		{
			day2.add(Calendar.WEEK_OF_MONTH, 1);
		}

		Calendar reenter = day1;
		if (day2.before(day1))
		{
			reenter = day2;
		}

		return reenter.getTimeInMillis() - System.currentTimeMillis();
	}

	/**
	 * @param npcId
	 * @return
	 */
	public int getRandomRespawnTime(int npcId)
	{
		switch (npcId)
		{
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
	public long getUnlockTime(int bossId)
	{
		long respawnTime = 0;
		int bossStatus = getBossStatus(bossId);
		if (bossStatus != ALIVE)
		{
			StatsSet info = getStatsSet(bossId);
			respawnTime = info.getLong("respawn_time");

			if (bossStatus == DEAD)
			{
				if (respawnTime <= System.currentTimeMillis())
				{
					return 1;
				}
				else
				{
					return respawnTime - System.currentTimeMillis();
				}
			}
			else
			{
				return 1000L;
			}
		}

		return respawnTime;
	}

	/**
	 * @param bossId
	 */
	public void notifyBossKilled(int bossId)
	{
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
	public boolean isActive(int bossId, long lastAction)
	{
		Long temp = System.currentTimeMillis() - lastAction;

		return temp <= 900000;

	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GrandBossManager _instance = new GrandBossManager();
	}
}
