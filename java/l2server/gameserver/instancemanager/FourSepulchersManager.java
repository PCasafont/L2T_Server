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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SepulcherMonsterInstance;
import l2server.gameserver.model.actor.instance.L2SepulcherNpcInstance;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.util.Util;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

/**
 * @author sandman
 */
public class FourSepulchersManager
{

	private static final String QUEST_ID = "620_FourGoblets";

	private static final int ENTRANCE_PASS = 7075;
	private static final int USED_PASS = 7261;
	private static final int CHAPEL_KEY = 7260;
	private static final int ANTIQUE_BROOCH = 7262;

	protected boolean firstTimeRun;
	protected boolean inEntryTime = false;
	protected boolean inWarmUpTime = false;
	protected boolean inAttackTime = false;
	protected boolean inCoolDownTime = false;

	protected ScheduledFuture<?> changeCoolDownTimeTask = null;
	protected ScheduledFuture<?> changeEntryTimeTask = null;
	protected ScheduledFuture<?> changeWarmUpTimeTask = null;
	protected ScheduledFuture<?> changeAttackTimeTask = null;
	protected ScheduledFuture<?> onPartyAnnihilatedTask = null;

	private int[][] startHallSpawn =
			{{181632, -85587, -7218}, {179963, -88978, -7218}, {173217, -86132, -7218}, {175608, -82296, -7218}};

	private int[][][] shadowSpawnLoc = {
			{
					{25339, 191231, -85574, -7216, 33380},
					{25349, 189534, -88969, -7216, 32768},
					{25346, 173195, -76560, -7215, 49277},
					{25342, 175591, -72744, -7215, 49317}
			}, {
					{25342, 191231, -85574, -7216, 33380},
					{25339, 189534, -88969, -7216, 32768},
					{25349, 173195, -76560, -7215, 49277},
					{25346, 175591, -72744, -7215, 49317}
			}, {
					{25346, 191231, -85574, -7216, 33380},
					{25342, 189534, -88969, -7216, 32768},
					{25339, 173195, -76560, -7215, 49277},
					{25349, 175591, -72744, -7215, 49317}
			}, {
					{25349, 191231, -85574, -7216, 33380},
					{25346, 189534, -88969, -7216, 32768},
					{25342, 173195, -76560, -7215, 49277},
					{25339, 175591, -72744, -7215, 49317}
			},
			};
	protected HashMap<Integer, Boolean> archonSpawned = new HashMap<>();
	protected HashMap<Integer, Boolean> hallInUse = new HashMap<>();
	protected HashMap<Integer, L2PcInstance> challengers = new HashMap<>();
	protected TIntObjectHashMap<int[]> startHallSpawns = new TIntObjectHashMap<>();
	protected TIntIntHashMap hallGateKeepers = new TIntIntHashMap();
	protected TIntIntHashMap keyBoxNpc = new TIntIntHashMap();
	protected TIntIntHashMap victim = new TIntIntHashMap();
	protected TIntObjectHashMap<L2Spawn> executionerSpawns = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<L2Spawn> keyBoxSpawns = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<L2Spawn> mysteriousBoxSpawns = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<L2Spawn> shadowSpawns = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<ArrayList<L2Spawn>> dukeFinalMobs = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<ArrayList<L2SepulcherMonsterInstance>> dukeMobs = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<ArrayList<L2Spawn>> emperorsGraveNpcs = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<ArrayList<L2Spawn>> magicalMonsters = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<ArrayList<L2Spawn>> physicalMonsters = new TIntObjectHashMap<>();
	protected TIntObjectHashMap<ArrayList<L2SepulcherMonsterInstance>> viscountMobs = new TIntObjectHashMap<>();

	protected ArrayList<L2Spawn> physicalSpawns;
	protected ArrayList<L2Spawn> magicalSpawns;
	protected ArrayList<L2Spawn> managers;
	protected ArrayList<L2Spawn> dukeFinalSpawns;
	protected ArrayList<L2Spawn> emperorsGraveSpawns;
	protected ArrayList<L2Npc> allMobs = new ArrayList<>();

	protected long attackTimeEnd = 0;
	protected long coolDownTimeEnd = 0;
	protected long entryTimeEnd = 0;
	protected long warmUpTimeEnd = 0;

	protected byte newCycleMin = 55;

	private FourSepulchersManager()
	{
	}

	public static FourSepulchersManager getInstance()
	{
		return SingletonHolder.instance;
	}

	public void init()
	{
		if (Config.IS_CLASSIC)
		{
			return;
		}

		if (changeCoolDownTimeTask != null)
		{
			changeCoolDownTimeTask.cancel(true);
		}
		if (changeEntryTimeTask != null)
		{
			changeEntryTimeTask.cancel(true);
		}
		if (changeWarmUpTimeTask != null)
		{
			changeWarmUpTimeTask.cancel(true);
		}
		if (changeAttackTimeTask != null)
		{
			changeAttackTimeTask.cancel(true);
		}

		changeCoolDownTimeTask = null;
		changeEntryTimeTask = null;
		changeWarmUpTimeTask = null;
		changeAttackTimeTask = null;

		inEntryTime = false;
		inWarmUpTime = false;
		inAttackTime = false;
		inCoolDownTime = false;

		firstTimeRun = true;
		initFixedInfo();
		loadMysteriousBox();
		initKeyBoxSpawns();
		loadPhysicalMonsters();
		loadMagicalMonsters();
		initLocationShadowSpawns();
		initExecutionerSpawns();
		loadDukeMonsters();
		loadEmperorsGraveMonsters();
		spawnManagers();
		timeSelector();
	}

	// phase select on server launch
	protected void timeSelector()
	{
		timeCalculator();
		long currentTime = Calendar.getInstance().getTimeInMillis();
		// if current time >= time of entry beginning and if current time < time
		// of entry beginning + time of entry end
		if (currentTime >= coolDownTimeEnd && currentTime < entryTimeEnd) // entry
		// time
		// check
		{
			clean();
			changeEntryTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeEntryTime(), 0);
			Log.info("FourSepulchersManager: Beginning in Entry time");
		}
		else if (currentTime >= entryTimeEnd && currentTime < warmUpTimeEnd) // warmup
		// time
		// check
		{
			clean();
			changeWarmUpTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeWarmUpTime(), 0);
			Log.info("FourSepulchersManager: Beginning in WarmUp time");
		}
		else if (currentTime >= warmUpTimeEnd && currentTime < attackTimeEnd) // attack
		// time
		// check
		{
			clean();
			changeAttackTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(), 0);
			Log.info("FourSepulchersManager: Beginning in Attack time");
		}
		else
		// else cooldown time and without cleanup because it's already
		// implemented
		{
			changeCoolDownTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(), 0);
			Log.info("FourSepulchersManager: Beginning in Cooldown time");
		}
	}

	// phase end times calculator
	protected void timeCalculator()
	{
		Calendar tmp = Calendar.getInstance();
		if (tmp.get(Calendar.MINUTE) < newCycleMin)
		{
			tmp.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) - 1);
		}
		tmp.set(Calendar.MINUTE, newCycleMin);
		coolDownTimeEnd = tmp.getTimeInMillis();
		entryTimeEnd = coolDownTimeEnd + Config.FS_TIME_ENTRY * 60000L;
		warmUpTimeEnd = entryTimeEnd + Config.FS_TIME_WARMUP * 60000L;
		attackTimeEnd = warmUpTimeEnd + Config.FS_TIME_ATTACK * 60000L;
	}

	public void clean()
	{
		for (int i = 31921; i < 31925; i++)
		{
			int[] Location = startHallSpawns.get(i);
			GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).oustAllPlayers();
		}

		deleteAllMobs();

		closeAllDoors();

		hallInUse.clear();
		hallInUse.put(31921, false);
		hallInUse.put(31922, false);
		hallInUse.put(31923, false);
		hallInUse.put(31924, false);

		if (archonSpawned.size() != 0)
		{
			Set<Integer> npcIdSet = archonSpawned.keySet();
			for (int npcId : npcIdSet)
			{
				archonSpawned.put(npcId, false);
			}
		}
	}

	protected void spawnManagers()
	{
		managers = new ArrayList<>();
		// L2Spawn spawnDat;

		int i = 31921;
		for (L2Spawn spawnDat; i <= 31924; i++)
		{
			if (i < 31921 || i > 31924)
			{
				continue;
			}
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(i);
			if (template1 == null)
			{
				continue;
			}
			try
			{
				spawnDat = new L2Spawn(template1);

				spawnDat.setRespawnDelay(60);
				switch (i)
				{
					case 31921: // conquerors
						spawnDat.setX(181061);
						spawnDat.setY(-85595);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-32584);
						break;
					case 31922: // emperors
						spawnDat.setX(179292);
						spawnDat.setY(-88981);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-33272);
						break;
					case 31923: // sages
						spawnDat.setX(173202);
						spawnDat.setY(-87004);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-16248);
						break;
					case 31924: // judges
						spawnDat.setX(175606);
						spawnDat.setY(-82853);
						spawnDat.setZ(-7200);
						spawnDat.setHeading(-16248);
						break;
				}
				managers.add(spawnDat);
				SpawnTable.getInstance().addNewSpawn(spawnDat, false);
				spawnDat.doSpawn();
				spawnDat.startRespawn();
				Log.info("FourSepulchersManager: spawned " + spawnDat.getTemplate().getName());
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Error while spawning managers: " + e.getMessage(), e);
			}
		}
	}

	protected void initFixedInfo()
	{
		startHallSpawns.put(31921, startHallSpawn[0]);
		startHallSpawns.put(31922, startHallSpawn[1]);
		startHallSpawns.put(31923, startHallSpawn[2]);
		startHallSpawns.put(31924, startHallSpawn[3]);

		hallInUse.put(31921, false);
		hallInUse.put(31922, false);
		hallInUse.put(31923, false);
		hallInUse.put(31924, false);

		hallGateKeepers.put(31925, 25150012);
		hallGateKeepers.put(31926, 25150013);
		hallGateKeepers.put(31927, 25150014);
		hallGateKeepers.put(31928, 25150015);
		hallGateKeepers.put(31929, 25150016);
		hallGateKeepers.put(31930, 25150002);
		hallGateKeepers.put(31931, 25150003);
		hallGateKeepers.put(31932, 25150004);
		hallGateKeepers.put(31933, 25150005);
		hallGateKeepers.put(31934, 25150006);
		hallGateKeepers.put(31935, 25150032);
		hallGateKeepers.put(31936, 25150033);
		hallGateKeepers.put(31937, 25150034);
		hallGateKeepers.put(31938, 25150035);
		hallGateKeepers.put(31939, 25150036);
		hallGateKeepers.put(31940, 25150022);
		hallGateKeepers.put(31941, 25150023);
		hallGateKeepers.put(31942, 25150024);
		hallGateKeepers.put(31943, 25150025);
		hallGateKeepers.put(31944, 25150026);

		keyBoxNpc.put(18120, 31455);
		keyBoxNpc.put(18121, 31455);
		keyBoxNpc.put(18122, 31455);
		keyBoxNpc.put(18123, 31455);
		keyBoxNpc.put(18124, 31456);
		keyBoxNpc.put(18125, 31456);
		keyBoxNpc.put(18126, 31456);
		keyBoxNpc.put(18127, 31456);
		keyBoxNpc.put(18128, 31457);
		keyBoxNpc.put(18129, 31457);
		keyBoxNpc.put(18130, 31457);
		keyBoxNpc.put(18131, 31457);
		keyBoxNpc.put(18149, 31458);
		keyBoxNpc.put(18150, 31459);
		keyBoxNpc.put(18151, 31459);
		keyBoxNpc.put(18152, 31459);
		keyBoxNpc.put(18153, 31459);
		keyBoxNpc.put(18154, 31460);
		keyBoxNpc.put(18155, 31460);
		keyBoxNpc.put(18156, 31460);
		keyBoxNpc.put(18157, 31460);
		keyBoxNpc.put(18158, 31461);
		keyBoxNpc.put(18159, 31461);
		keyBoxNpc.put(18160, 31461);
		keyBoxNpc.put(18161, 31461);
		keyBoxNpc.put(18162, 31462);
		keyBoxNpc.put(18163, 31462);
		keyBoxNpc.put(18164, 31462);
		keyBoxNpc.put(18165, 31462);
		keyBoxNpc.put(18183, 31463);
		keyBoxNpc.put(18184, 31464);
		keyBoxNpc.put(18212, 31465);
		keyBoxNpc.put(18213, 31465);
		keyBoxNpc.put(18214, 31465);
		keyBoxNpc.put(18215, 31465);
		keyBoxNpc.put(18216, 31466);
		keyBoxNpc.put(18217, 31466);
		keyBoxNpc.put(18218, 31466);
		keyBoxNpc.put(18219, 31466);

		victim.put(18150, 18158);
		victim.put(18151, 18159);
		victim.put(18152, 18160);
		victim.put(18153, 18161);
		victim.put(18154, 18162);
		victim.put(18155, 18163);
		victim.put(18156, 18164);
		victim.put(18157, 18165);
	}

	private void loadMysteriousBox()
	{
		Connection con = null;

		mysteriousBoxSpawns.clear();

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY id");
			statement.setInt(1, 0);
			ResultSet rset = statement.executeQuery();

			L2Spawn spawnDat;
			L2NpcTemplate template1;

			while (rset.next())
			{
				template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					spawnDat = new L2Spawn(template1);
					spawnDat.setX(rset.getInt("locx"));
					spawnDat.setY(rset.getInt("locy"));
					spawnDat.setZ(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					int keyNpcId = rset.getInt("key_npc_id");
					mysteriousBoxSpawns.put(keyNpcId, spawnDat);
				}
				else
				{
					Log.warning("FourSepulchersManager.LoadMysteriousBox: Data missing in NPC table for ID: " +
							rset.getInt("npc_templateid") + ".");
				}
			}

			rset.close();
			statement.close();
			Log.fine("FourSepulchersManager: loaded " + mysteriousBoxSpawns.size() + " Mysterious-Box spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			Log.log(Level.WARNING,
					"FourSepulchersManager.LoadMysteriousBox: Spawn could not be initialized: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void initKeyBoxSpawns()
	{
		L2Spawn spawnDat;
		L2NpcTemplate template;

		for (int keyNpcId : keyBoxNpc.keys())
		{
			try
			{
				template = NpcTable.getInstance().getTemplate(keyBoxNpc.get(keyNpcId));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setX(0);
					spawnDat.setY(0);
					spawnDat.setZ(0);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(3600);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					keyBoxSpawns.put(keyNpcId, spawnDat);
				}
				else
				{
					Log.warning("FourSepulchersManager.InitKeyBoxSpawns: Data missing in NPC table for ID: " +
							keyBoxNpc.get(keyNpcId) + ".");
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING,
						"FourSepulchersManager.InitKeyBoxSpawns: Spawn could not be initialized: " + e.getMessage(), e);
			}
		}
	}

	private void loadPhysicalMonsters()
	{
		physicalMonsters.clear();

		int loaded = 0;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement1 = con.prepareStatement(
					"SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 1);
			ResultSet rset1 = statement1.executeQuery();

			PreparedStatement statement2 = con.prepareStatement(
					"SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist Where key_npc_id = ? and spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");

				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 1);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();

				L2Spawn spawnDat;
				L2NpcTemplate template1;

				physicalSpawns = new ArrayList<>();

				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						physicalSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						Log.warning("FourSepulchersManager.LoadPhysicalMonsters: Data missing in NPC table for ID: " +
								rset2.getInt("npc_templateid") + ".");
					}
				}

				rset2.close();
				physicalMonsters.put(keyNpcId, physicalSpawns);
			}

			rset1.close();
			statement1.close();
			statement2.close();

			Log.fine("FourSepulchersManager: loaded " + loaded + " Physical type monsters spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			Log.log(Level.WARNING,
					"FourSepulchersManager.LoadPhysicalMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void loadMagicalMonsters()
	{

		magicalMonsters.clear();

		int loaded = 0;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement1 = con.prepareStatement(
					"SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 2);
			ResultSet rset1 = statement1.executeQuery();

			PreparedStatement statement2 = con.prepareStatement(
					"SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");

				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 2);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();

				L2Spawn spawnDat;
				L2NpcTemplate template1;

				magicalSpawns = new ArrayList<>();

				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						magicalSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						Log.warning("FourSepulchersManager.LoadMagicalMonsters: Data missing in NPC table for ID: " +
								rset2.getInt("npc_templateid") + ".");
					}
				}

				rset2.close();
				magicalMonsters.put(keyNpcId, magicalSpawns);
			}

			rset1.close();
			statement1.close();
			statement2.close();

			Log.fine("FourSepulchersManager: loaded " + loaded + " Magical type monsters spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			Log.log(Level.WARNING,
					"FourSepulchersManager.LoadMagicalMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void loadDukeMonsters()
	{
		dukeFinalMobs.clear();
		archonSpawned.clear();

		int loaded = 0;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement1 = con.prepareStatement(
					"SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 5);
			ResultSet rset1 = statement1.executeQuery();

			PreparedStatement statement2 = con.prepareStatement(
					"SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");

				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 5);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();

				L2Spawn spawnDat;
				L2NpcTemplate template1;

				dukeFinalSpawns = new ArrayList<>();

				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						dukeFinalSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						Log.warning("FourSepulchersManager.LoadDukeMonsters: Data missing in NPC table for ID: " +
								rset2.getInt("npc_templateid") + ".");
					}
				}

				rset2.close();
				dukeFinalMobs.put(keyNpcId, dukeFinalSpawns);
				archonSpawned.put(keyNpcId, false);
			}

			rset1.close();
			statement1.close();
			statement2.close();

			Log.fine("FourSepulchersManager: loaded " + loaded + " Church of duke monsters spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			Log.log(Level.WARNING,
					"FourSepulchersManager.LoadDukeMonsters: Spawn could not be initialized: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void loadEmperorsGraveMonsters()
	{

		emperorsGraveNpcs.clear();

		int loaded = 0;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement1 = con.prepareStatement(
					"SELECT Distinct key_npc_id FROM four_sepulchers_spawnlist Where spawntype = ? ORDER BY key_npc_id");
			statement1.setInt(1, 6);
			ResultSet rset1 = statement1.executeQuery();

			PreparedStatement statement2 = con.prepareStatement(
					"SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM four_sepulchers_spawnlist WHERE key_npc_id = ? and spawntype = ? ORDER BY id");
			while (rset1.next())
			{
				int keyNpcId = rset1.getInt("key_npc_id");

				statement2.setInt(1, keyNpcId);
				statement2.setInt(2, 6);
				ResultSet rset2 = statement2.executeQuery();
				statement2.clearParameters();

				L2Spawn spawnDat;
				L2NpcTemplate template1;

				emperorsGraveSpawns = new ArrayList<>();

				while (rset2.next())
				{
					template1 = NpcTable.getInstance().getTemplate(rset2.getInt("npc_templateid"));
					if (template1 != null)
					{
						spawnDat = new L2Spawn(template1);
						spawnDat.setX(rset2.getInt("locx"));
						spawnDat.setY(rset2.getInt("locy"));
						spawnDat.setZ(rset2.getInt("locz"));
						spawnDat.setHeading(rset2.getInt("heading"));
						spawnDat.setRespawnDelay(rset2.getInt("respawn_delay"));
						SpawnTable.getInstance().addNewSpawn(spawnDat, false);
						emperorsGraveSpawns.add(spawnDat);
						loaded++;
					}
					else
					{
						Log.warning(
								"FourSepulchersManager.LoadEmperorsGraveMonsters: Data missing in NPC table for ID: " +
										rset2.getInt("npc_templateid") + ".");
					}
				}

				rset2.close();
				emperorsGraveNpcs.put(keyNpcId, emperorsGraveSpawns);
			}

			rset1.close();
			statement1.close();
			statement2.close();

			Log.fine("FourSepulchersManager: loaded " + loaded + " Emperor's grave NPC spawns.");
		}
		catch (Exception e)
		{
			// problem with initializing spawn, go to next one
			Log.log(Level.WARNING, "FourSepulchersManager.LoadEmperorsGraveMonsters: Spawn could not be initialized: " +
					e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	protected void initLocationShadowSpawns()
	{
		int locNo = Rnd.get(4);
		final int[] gateKeeper = {31929, 31934, 31939, 31944};

		L2Spawn spawnDat;
		L2NpcTemplate template;

		shadowSpawns.clear();

		for (int i = 0; i <= 3; i++)
		{
			template = NpcTable.getInstance().getTemplate(shadowSpawnLoc[locNo][i][0]);
			if (template != null)
			{
				try
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setX(shadowSpawnLoc[locNo][i][1]);
					spawnDat.setY(shadowSpawnLoc[locNo][i][2]);
					spawnDat.setZ(shadowSpawnLoc[locNo][i][3]);
					spawnDat.setHeading(shadowSpawnLoc[locNo][i][4]);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					int keyNpcId = gateKeeper[i];
					shadowSpawns.put(keyNpcId, spawnDat);
				}
				catch (Exception e)
				{
					Log.log(Level.SEVERE, "Error on InitLocationShadowSpawns", e);
				}
			}
			else
			{
				Log.warning("FourSepulchersManager.InitLocationShadowSpawns: Data missing in NPC table for ID: " +
						shadowSpawnLoc[locNo][i][0] + ".");
			}
		}
	}

	protected void initExecutionerSpawns()
	{
		L2Spawn spawnDat;
		L2NpcTemplate template;

		for (int keyNpcId : victim.keys())
		{
			try
			{
				template = NpcTable.getInstance().getTemplate(victim.get(keyNpcId));
				if (template != null)
				{
					spawnDat = new L2Spawn(template);
					spawnDat.setX(0);
					spawnDat.setY(0);
					spawnDat.setZ(0);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(3600);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					executionerSpawns.put(keyNpcId, spawnDat);
				}
				else
				{
					Log.warning("FourSepulchersManager.InitExecutionerSpawns: Data missing in NPC table for ID: " +
							victim.get(keyNpcId) + ".");
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "FourSepulchersManager.InitExecutionerSpawns: Spawn could not be initialized: " +
						e.getMessage(), e);
			}
		}
	}

	public boolean isEntryTime()
	{
		return inEntryTime;
	}

	public boolean isAttackTime()
	{
		return inAttackTime;
	}

	public synchronized void tryEntry(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		switch (npcId)
		{
			// ID ok
			case 31921:
			case 31922:
			case 31923:
			case 31924:
				break;
			// ID not ok
			default:
				if (!player.isGM())
				{
					Log.warning("Player " + player.getName() + " (" + player.getObjectId() +
							") tried to cheat in four sepulchers.");
					Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() +
							" tried to enter four sepulchers with invalid npc id.", Config.DEFAULT_PUNISH);
				}
				return;
		}

		if (hallInUse.get(npcId))
		{
			showHtmlFile(player, npcId + "-FULL.htm", npc, null);
			return;
		}

		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			if (!player.isInParty() || player.getParty().getMemberCount() < Config.FS_PARTY_MEMBER_COUNT)
			{
				showHtmlFile(player, npcId + "-SP.htm", npc, null);
				return;
			}

			if (!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId + "-NL.htm", npc, null);
				return;
			}

			for (L2PcInstance mem : player.getParty().getPartyMembers())
			{
				QuestState qs = mem.getQuestState(QUEST_ID);
				if (qs == null || !qs.isStarted() && !qs.isCompleted())
				{
					showHtmlFile(player, npcId + "-NS.htm", npc, mem);
					return;
				}
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId + "-SE.htm", npc, mem);
					return;
				}

				if (player.getWeightPenalty() >= 3)
				{
					mem.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
			}
		}
		else if (Config.FS_PARTY_MEMBER_COUNT <= 1 && player.isInParty())
		{
			if (!player.getParty().isLeader(player))
			{
				showHtmlFile(player, npcId + "-NL.htm", npc, null);
				return;
			}
			for (L2PcInstance mem : player.getParty().getPartyMembers())
			{
				QuestState qs = mem.getQuestState(QUEST_ID);
				if (qs == null || !qs.isStarted() && !qs.isCompleted())
				{
					showHtmlFile(player, npcId + "-NS.htm", npc, mem);
					return;
				}
				if (mem.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
				{
					showHtmlFile(player, npcId + "-SE.htm", npc, mem);
					return;
				}

				if (player.getWeightPenalty() >= 3)
				{
					mem.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
					return;
				}
			}
		}
		else
		{
			QuestState qs = player.getQuestState(QUEST_ID);
			if (qs == null || !qs.isStarted() && !qs.isCompleted())
			{
				showHtmlFile(player, npcId + "-NS.htm", npc, player);
				return;
			}
			if (player.getInventory().getItemByItemId(ENTRANCE_PASS) == null)
			{
				showHtmlFile(player, npcId + "-SE.htm", npc, player);
				return;
			}

			if (player.getWeightPenalty() >= 3)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT));
				return;
			}
		}

		if (!isEntryTime())
		{
			showHtmlFile(player, npcId + "-NE.htm", npc, null);
			return;
		}

		showHtmlFile(player, npcId + "-OK.htm", npc, null);

		entry(npcId, player);
	}

	private void entry(int npcId, L2PcInstance player)
	{
		int[] Location = startHallSpawns.get(npcId);
		int driftx;
		int drifty;

		if (Config.FS_PARTY_MEMBER_COUNT > 1)
		{
			List<L2PcInstance> members = new ArrayList<>();
			for (L2PcInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}

			for (L2PcInstance mem : members)
			{
				GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2])
						.allowPlayerEntry(mem, 7200);
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem, true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}

				L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if (hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}

			challengers.remove(npcId);
			challengers.put(npcId, player);

			hallInUse.remove(npcId);
			hallInUse.put(npcId, true);
		}
		if (Config.FS_PARTY_MEMBER_COUNT <= 1 && player.isInParty())
		{
			List<L2PcInstance> members = new ArrayList<>();
			for (L2PcInstance mem : player.getParty().getPartyMembers())
			{
				if (!mem.isDead() && Util.checkIfInRange(700, player, mem, true))
				{
					members.add(mem);
				}
			}

			for (L2PcInstance mem : members)
			{
				GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(mem, 30);
				driftx = Rnd.get(-80, 80);
				drifty = Rnd.get(-80, 80);
				mem.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
				mem.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, mem, true);
				if (mem.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
				{
					mem.addItem("Quest", USED_PASS, 1, mem, true);
				}

				L2ItemInstance hallsKey = mem.getInventory().getItemByItemId(CHAPEL_KEY);
				if (hallsKey != null)
				{
					mem.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), mem, true);
				}
			}

			challengers.remove(npcId);
			challengers.put(npcId, player);

			hallInUse.remove(npcId);
			hallInUse.put(npcId, true);
		}
		else
		{
			GrandBossManager.getInstance().getZone(Location[0], Location[1], Location[2]).allowPlayerEntry(player, 30);
			driftx = Rnd.get(-80, 80);
			drifty = Rnd.get(-80, 80);
			player.teleToLocation(Location[0] + driftx, Location[1] + drifty, Location[2]);
			player.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, player, true);
			if (player.getInventory().getItemByItemId(ANTIQUE_BROOCH) == null)
			{
				player.addItem("Quest", USED_PASS, 1, player, true);
			}

			L2ItemInstance hallsKey = player.getInventory().getItemByItemId(CHAPEL_KEY);
			if (hallsKey != null)
			{
				player.destroyItemByItemId("Quest", CHAPEL_KEY, hallsKey.getCount(), player, true);
			}

			challengers.remove(npcId);
			challengers.put(npcId, player);

			hallInUse.remove(npcId);
			hallInUse.put(npcId, true);
		}
	}

	public void spawnMysteriousBox(int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}

		L2Spawn spawnDat = mysteriousBoxSpawns.get(npcId);
		if (spawnDat != null)
		{
			allMobs.add(spawnDat.getNpc());
			spawnDat.doSpawn();
			spawnDat.stopRespawn();
		}
	}

	public void spawnMonster(int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}

		ArrayList<L2Spawn> monsterList;
		ArrayList<L2SepulcherMonsterInstance> mobs = new ArrayList<>();
		L2Spawn keyBoxMobSpawn;

		if (Rnd.get(2) == 0)
		{
			monsterList = physicalMonsters.get(npcId);
		}
		else
		{
			monsterList = magicalMonsters.get(npcId);
		}

		if (monsterList != null)
		{
			boolean spawnKeyBoxMob = false;
			boolean spawnedKeyBoxMob = false;

			for (L2Spawn spawnDat : monsterList)
			{
				if (spawnedKeyBoxMob)
				{
					spawnKeyBoxMob = false;
				}
				else
				{
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
							if (Rnd.get(48) == 0)
							{
								spawnKeyBoxMob = true;
								// Logozo.info("FourSepulchersManager.SpawnMonster:
								// Set to spawn Church of Viscount Key Mob.");
							}
							break;
						default:
							spawnKeyBoxMob = false;
					}
				}

				L2SepulcherMonsterInstance mob = null;

				if (spawnKeyBoxMob)
				{
					try
					{
						L2NpcTemplate template = NpcTable.getInstance().getTemplate(18149);
						if (template != null)
						{
							keyBoxMobSpawn = new L2Spawn(template);
							keyBoxMobSpawn.setX(spawnDat.getX());
							keyBoxMobSpawn.setY(spawnDat.getY());
							keyBoxMobSpawn.setZ(spawnDat.getZ());
							keyBoxMobSpawn.setHeading(spawnDat.getHeading());
							keyBoxMobSpawn.setRespawnDelay(3600);
							SpawnTable.getInstance().addNewSpawn(keyBoxMobSpawn, false);
							mob = (L2SepulcherMonsterInstance) keyBoxMobSpawn.getNpc();
							keyBoxMobSpawn.doSpawn();
							keyBoxMobSpawn.stopRespawn();
						}
						else
						{
							Log.warning("FourSepulchersManager.SpawnMonster: Data missing in NPC table for ID: 18149");
						}
					}
					catch (Exception e)
					{
						Log.log(Level.WARNING,
								"FourSepulchersManager.SpawnMonster: Spawn could not be initialized: " + e.getMessage(),
								e);
					}

					spawnedKeyBoxMob = true;
				}
				else
				{
					mob = (L2SepulcherMonsterInstance) spawnDat.getNpc();
					spawnDat.doSpawn();
					spawnDat.stopRespawn();
				}

				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					switch (npcId)
					{
						case 31469:
						case 31474:
						case 31479:
						case 31484:
						case 31472:
						case 31477:
						case 31482:
						case 31487:
							mobs.add(mob);
					}
					allMobs.add(mob);
				}
			}

			switch (npcId)
			{
				case 31469:
				case 31474:
				case 31479:
				case 31484:
					viscountMobs.put(npcId, mobs);
					break;

				case 31472:
				case 31477:
				case 31482:
				case 31487:
					dukeMobs.put(npcId, mobs);
					break;
			}
		}
	}

	public synchronized boolean isViscountMobsAnnihilated(int npcId)
	{
		ArrayList<L2SepulcherMonsterInstance> mobs = viscountMobs.get(npcId);

		if (mobs == null)
		{
			return true;
		}

		for (L2SepulcherMonsterInstance mob : mobs)
		{
			if (!mob.isDead())
			{
				return false;
			}
		}

		return true;
	}

	public synchronized boolean isDukeMobsAnnihilated(int npcId)
	{
		ArrayList<L2SepulcherMonsterInstance> mobs = dukeMobs.get(npcId);

		if (mobs == null)
		{
			return true;
		}

		for (L2SepulcherMonsterInstance mob : mobs)
		{
			if (!mob.isDead())
			{
				return false;
			}
		}

		return true;
	}

	public void spawnKeyBox(L2Npc activeChar)
	{
		if (!isAttackTime())
		{
			return;
		}

		L2Spawn spawnDat = keyBoxSpawns.get(activeChar.getNpcId());

		if (spawnDat != null)
		{
			spawnDat.setX(activeChar.getX());
			spawnDat.setY(activeChar.getY());
			spawnDat.setZ(activeChar.getZ());
			spawnDat.setHeading(activeChar.getHeading());
			spawnDat.setRespawnDelay(3600);
			allMobs.add(spawnDat.getNpc());
			spawnDat.doSpawn();
			spawnDat.stopRespawn();
		}
	}

	public void spawnExecutionerOfHalisha(L2Npc activeChar)
	{
		if (!isAttackTime())
		{
			return;
		}

		L2Spawn spawnDat = executionerSpawns.get(activeChar.getNpcId());

		if (spawnDat != null)
		{
			spawnDat.setX(activeChar.getX());
			spawnDat.setY(activeChar.getY());
			spawnDat.setZ(activeChar.getZ());
			spawnDat.setHeading(activeChar.getHeading());
			spawnDat.setRespawnDelay(3600);
			allMobs.add(spawnDat.getNpc());
			spawnDat.doSpawn();
			spawnDat.stopRespawn();
		}
	}

	public void spawnArchonOfHalisha(int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}

		if (archonSpawned.get(npcId))
		{
			return;
		}

		ArrayList<L2Spawn> monsterList = dukeFinalMobs.get(npcId);

		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance) spawnDat.getNpc();
				spawnDat.doSpawn();
				spawnDat.stopRespawn();

				if (mob != null)
				{
					mob.mysteriousBoxId = npcId;
					allMobs.add(mob);
				}
			}
			archonSpawned.put(npcId, true);
		}
	}

	public void spawnEmperorsGraveNpc(int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}

		ArrayList<L2Spawn> monsterList = emperorsGraveNpcs.get(npcId);

		if (monsterList != null)
		{
			for (L2Spawn spawnDat : monsterList)
			{
				allMobs.add(spawnDat.getNpc());
				spawnDat.doSpawn();
				spawnDat.stopRespawn();
			}
		}
	}

	protected void locationShadowSpawns()
	{
		int locNo = Rnd.get(4);
		// Logozo.info("FourSepulchersManager.LocationShadowSpawns: Location index
		// is " + locNo + ".");
		final int[] gateKeeper = {31929, 31934, 31939, 31944};

		L2Spawn spawnDat;

		for (int i = 0; i <= 3; i++)
		{
			int keyNpcId = gateKeeper[i];
			spawnDat = shadowSpawns.get(keyNpcId);
			spawnDat.setX(shadowSpawnLoc[locNo][i][1]);
			spawnDat.setY(shadowSpawnLoc[locNo][i][2]);
			spawnDat.setZ(shadowSpawnLoc[locNo][i][3]);
			spawnDat.setHeading(shadowSpawnLoc[locNo][i][4]);
			shadowSpawns.put(keyNpcId, spawnDat);
		}
	}

	public void spawnShadow(int npcId)
	{
		if (!isAttackTime())
		{
			return;
		}

		L2Spawn spawnDat = shadowSpawns.get(npcId);
		if (spawnDat != null)
		{
			L2SepulcherMonsterInstance mob = (L2SepulcherMonsterInstance) spawnDat.getNpc();
			spawnDat.doSpawn();
			spawnDat.stopRespawn();

			if (mob != null)
			{
				mob.mysteriousBoxId = npcId;
				allMobs.add(mob);
			}
		}
	}

	public void deleteAllMobs()
	{
		for (L2Npc mob : allMobs)
		{
			if (mob == null)
			{
				continue;
			}

			try
			{
				if (mob.getSpawn() != null)
				{
					mob.getSpawn().stopRespawn();
				}
				mob.deleteMe();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "FourSepulchersManager: Failed deleting mob.", e);
			}
		}
		allMobs.clear();
	}

	protected void closeAllDoors()
	{
		for (int doorId : hallGateKeepers.getValues())
		{
			try
			{
				L2DoorInstance door = DoorTable.getInstance().getDoor(doorId);
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					Log.warning("FourSepulchersManager: Attempted to close undefined door. doorId: " + doorId);
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "FourSepulchersManager: Failed closing door", e);
			}
		}
	}

	protected byte minuteSelect(byte min)
	{
		if ((double) min % 5 != 0)// if doesn't divides on 5 fully
		{
			// mad table for selecting proper minutes...
			// may be there is a better way to do this
			switch (min)
			{
				case 6:
				case 7:
					min = 5;
					break;
				case 8:
				case 9:
				case 11:
				case 12:
					min = 10;
					break;
				case 13:
				case 14:
				case 16:
				case 17:
					min = 15;
					break;
				case 18:
				case 19:
				case 21:
				case 22:
					min = 20;
					break;
				case 23:
				case 24:
				case 26:
				case 27:
					min = 25;
					break;
				case 28:
				case 29:
				case 31:
				case 32:
					min = 30;
					break;
				case 33:
				case 34:
				case 36:
				case 37:
					min = 35;
					break;
				case 38:
				case 39:
				case 41:
				case 42:
					min = 40;
					break;
				case 43:
				case 44:
				case 46:
				case 47:
					min = 45;
					break;
				case 48:
				case 49:
				case 51:
				case 52:
					min = 50;
					break;
				case 53:
				case 54:
				case 56:
				case 57:
					min = 55;
					break;
			}
		}
		return min;
	}

	public void managerSay(byte min)
	{
		// for attack phase, sending message every 5 minutes
		if (inAttackTime)
		{
			if (min < 5)
			{
				return; // do not shout when < 5 minutes
			}

			min = minuteSelect(min);

			String msg = min + " minute(s) have passed."; // now this is a
			// proper message^^

			if (min == 90)
			{
				msg = "Game over. The teleport will appear momentarily";
			}

			for (L2Spawn temp : managers)
			{
				if (temp == null)
				{
					Log.warning("FourSepulchersManager: managerSay(): manager is null");
					continue;
				}
				if (!(temp.getNpc() instanceof L2SepulcherNpcInstance))
				{
					Log.warning("FourSepulchersManager: managerSay(): manager is not Sepulcher instance");
					continue;
				}
				// hall not used right now, so its manager will not tell you
				// anything :)
				// if you don't need this - delete next two lines.
				if (!hallInUse.get(temp.getNpcId()))
				{
					continue;
				}

				((L2SepulcherNpcInstance) temp.getNpc()).sayInShout(msg);
			}
		}

		else if (inEntryTime)
		{
			String msg1 = "You may now enter the Sepulcher";
			String msg2 = "If you place your hand on the stone statue in front of each sepulcher," +
					" you will be able to enter";
			for (L2Spawn temp : managers)
			{
				if (temp == null)
				{
					Log.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				if (!(temp.getNpc() instanceof L2SepulcherNpcInstance))
				{
					Log.warning("FourSepulchersManager: Something goes wrong in managerSay()...");
					continue;
				}
				((L2SepulcherNpcInstance) temp.getNpc()).sayInShout(msg1);
				((L2SepulcherNpcInstance) temp.getNpc()).sayInShout(msg2);
			}
		}
	}

	protected class ManagerSay implements Runnable
	{
		@Override
		public void run()
		{
			if (inAttackTime)
			{
				Calendar tmp = Calendar.getInstance();
				tmp.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - warmUpTimeEnd);
				if (tmp.get(Calendar.MINUTE) + 5 < Config.FS_TIME_ATTACK)
				{
					managerSay((byte) tmp.get(Calendar.MINUTE)); // byte
					// because
					// minute
					// cannot be
					// more than
					// 59
					ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), 5 * 60000);
				}
				// attack time ending chat
				else if (tmp.get(Calendar.MINUTE) + 5 >= Config.FS_TIME_ATTACK)
				{
					managerSay((byte) 90); // sending a unique id :D
				}
			}
			else if (inEntryTime)
			{
				managerSay((byte) 0);
			}
		}
	}

	protected class ChangeEntryTime implements Runnable
	{
		@Override
		public void run()
		{
			// Logozo.info("FourSepulchersManager:In Entry Time");
			inEntryTime = true;
			inWarmUpTime = false;
			inAttackTime = false;
			inCoolDownTime = false;

			long interval = 0;
			// if this is first launch - search time when entry time will be
			// ended:
			// counting difference between time when entry time ends and current
			// time
			// and then launching change time task
			if (firstTimeRun)
			{
				interval = entryTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_ENTRY * 60000L; // else use stupid
			}
			// method

			// launching saying process...
			ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), 0);
			changeWarmUpTimeTask = ThreadPoolManager.getInstance().scheduleEffect(new ChangeWarmUpTime(), interval);
			if (changeEntryTimeTask != null)
			{
				changeEntryTimeTask.cancel(true);
				changeEntryTimeTask = null;
			}
		}
	}

	protected class ChangeWarmUpTime implements Runnable
	{
		@Override
		public void run()
		{
			// Logozo.info("FourSepulchersManager:In Warm-Up Time");
			inEntryTime = true;
			inWarmUpTime = false;
			inAttackTime = false;
			inCoolDownTime = false;

			long interval = 0;
			// searching time when warmup time will be ended:
			// counting difference between time when warmup time ends and
			// current time
			// and then launching change time task
			if (firstTimeRun)
			{
				interval = warmUpTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_WARMUP * 60000L;
			}
			changeAttackTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeAttackTime(), interval);

			if (changeWarmUpTimeTask != null)
			{
				changeWarmUpTimeTask.cancel(true);
				changeWarmUpTimeTask = null;
			}
		}
	}

	protected class ChangeAttackTime implements Runnable
	{
		@Override
		public void run()
		{
			// Logozo.info("FourSepulchersManager:In Attack Time");
			inEntryTime = false;
			inWarmUpTime = false;
			inAttackTime = true;
			inCoolDownTime = false;

			locationShadowSpawns();

			spawnMysteriousBox(31921);
			spawnMysteriousBox(31922);
			spawnMysteriousBox(31923);
			spawnMysteriousBox(31924);

			if (!firstTimeRun)
			{
				warmUpTimeEnd = Calendar.getInstance().getTimeInMillis();
			}

			long interval = 0;
			// say task
			if (firstTimeRun)
			{
				for (double min = Calendar.getInstance().get(Calendar.MINUTE); min < newCycleMin; min++)
				{
					// looking for next shout time....
					if (min % 5 == 0)// check if min can be divided by 5
					{
						Log.info(Calendar.getInstance().getTime() + " Atk announce scheduled to " + min +
								" minute of this hour.");
						Calendar inter = Calendar.getInstance();
						inter.set(Calendar.MINUTE, (int) min);
						ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(),
								inter.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
						break;
					}
				}
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new ManagerSay(), 5 * 60400);
			}
			// searching time when attack time will be ended:
			// counting difference between time when attack time ends and
			// current time
			// and then launching change time task
			if (firstTimeRun)
			{
				interval = attackTimeEnd - Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				interval = Config.FS_TIME_ATTACK * 60000L;
			}
			changeCoolDownTimeTask =
					ThreadPoolManager.getInstance().scheduleGeneral(new ChangeCoolDownTime(), interval);

			if (changeAttackTimeTask != null)
			{
				changeAttackTimeTask.cancel(true);
				changeAttackTimeTask = null;
			}
		}
	}

	protected class ChangeCoolDownTime implements Runnable
	{
		@Override
		public void run()
		{
			// Logozo.info("FourSepulchersManager:In Cool-Down Time");
			inEntryTime = false;
			inWarmUpTime = false;
			inAttackTime = false;
			inCoolDownTime = true;

			clean();

			Calendar time = Calendar.getInstance();
			// one hour = 55th min to 55 min of next hour, so we check for this,
			// also check for first launch
			if (Calendar.getInstance().get(Calendar.MINUTE) > newCycleMin && !firstTimeRun)
			{
				time.set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR) + 1);
			}
			time.set(Calendar.MINUTE, newCycleMin);
			Log.fine("FourSepulchersManager: Entry time: " + time.getTime());
			if (firstTimeRun)
			{
				firstTimeRun = false; // cooldown phase ends event hour, so it
			}
			// will be not first run

			long interval = time.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			changeEntryTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChangeEntryTime(), interval);

			if (changeCoolDownTimeTask != null)
			{
				changeCoolDownTimeTask.cancel(true);
				changeCoolDownTimeTask = null;
			}
		}
	}

	public TIntIntHashMap getHallGateKeepers()
	{
		return hallGateKeepers;
	}

	public void showHtmlFile(L2PcInstance player, String file, L2Npc npc, L2PcInstance member)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(player.getHtmlPrefix(), "SepulcherNpc/" + file);
		if (member != null)
		{
			html.replace("%member%", member.getName());
		}
		player.sendPacket(html);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final FourSepulchersManager instance = new FourSepulchersManager();
	}
}
