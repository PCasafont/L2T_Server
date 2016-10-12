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

package l2server.gameserver.model.entity;

import gnu.trove.TIntIntHashMap;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.FortUpdater;
import l2server.gameserver.FortUpdater.UpdaterType;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.*;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2FortBallistaInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2server.gameserver.model.zone.type.L2FortZone;
import l2server.gameserver.model.zone.type.L2SiegeZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Fort
{
	// =========================================================
	// Data Field
	private int _fortId = 0;
	private List<L2DoorInstance> _doors = new ArrayList<>();
	private L2StaticObjectInstance _flagPole = null;
	private String _name = "";
	private FortSiege _siege = null;
	private Calendar _siegeDate;
	private Calendar _lastOwnedTime;
	private L2FortZone _fortZone;
	private L2SiegeZone _zone;
	private L2Clan _fortOwner = null;
	private int _fortType = 0;
	private int _state = 0;
	private int _castleId = 0;
	private int _blood = 0;
	private int _supplyLvL = 0;
	private HashMap<Integer, FortFunction> _function;
	private ArrayList<L2Skill> _residentialSkills = new ArrayList<>();
	private ScheduledFuture<?>[] _fortUpdater = new ScheduledFuture<?>[2];
	private boolean _isSuspiciousMerchantSpawned = false;

	private ArrayList<CombatFlag> _flagList = new ArrayList<>();

	private TIntIntHashMap _envoyCastles = new TIntIntHashMap(2);

	/**
	 * Fortress Functions
	 */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;

	public class FortFunction
	{
		private int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private long _rate;
		private long _endDate;
		protected boolean _inDebt;
		public boolean _cwh;

		public FortFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeTask(cwh);
		}

		public int getType()
		{
			return _type;
		}

		public int getLvl()
		{
			return _lvl;
		}

		public int getLease()
		{
			return _fee;
		}

		public long getRate()
		{
			return _rate;
		}

		public long getEndTime()
		{
			return _endDate;
		}

		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}

		public void setLease(int lease)
		{
			_fee = lease;
		}

		public void setEndTime(long time)
		{
			_endDate = time;
		}

		private void initializeTask(boolean cwh)
		{
			if (getOwnerClan() == null)
			{
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (_endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), _endDate - currentTime);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
			}
		}

		private class FunctionTask implements Runnable
		{
			public FunctionTask(boolean cwh)
			{
				_cwh = cwh;
			}

			@Override
			public void run()
			{
				try
				{
					if (getOwnerClan() == null)
					{
						return;
					}
					if (getOwnerClan().getWarehouse().getAdena() >= _fee || !_cwh)
					{
						int fee = _fee;
						if (getEndTime() == -1)
						{
							fee = _tempFee;
						}

						setEndTime(System.currentTimeMillis() + getRate());
						dbSave();
						if (_cwh)
						{
							getOwnerClan().getWarehouse().destroyItemByItemId("CS_function_fee", 57, fee, null, null);
							if (Config.DEBUG)
							{
								Log.warning("Deducted " + fee + " adena from " + getName() +
										" owner's cwh for function id : " + getType());
							}
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					}
					else
					{
						removeFunction(getType());
					}
				}
				catch (Throwable ignored)
				{
				}
			}
		}

		public void dbSave()
		{
			Connection con = null;
			try
			{
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(
						"REPLACE INTO fort_functions (fort_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				statement.setInt(1, _fortId);
				statement.setInt(2, getType());
				statement.setInt(3, getLvl());
				statement.setInt(4, getLease());
				statement.setLong(5, getRate());
				statement.setLong(6, getEndTime());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE,
						"Exception: Fort.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " +
								e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	// =========================================================
	// Constructor
	public Fort(int id, String name, int type, int flagPoleId)
	{
		_fortId = id;
		_name = name;
		_fortType = type;
		_flagPole = StaticObjects.getInstance().getObject(flagPoleId);

		loadDbData();

		_function = new HashMap<>();
		_residentialSkills = ResidentialSkillTable.getInstance().getSkills(_fortId);
		if (getOwnerClan() != null)
		{
			setVisibleFlag(true);
			loadFunctions();
		}

		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			spawnNpcCommanders(); // spawn npc Commanders
			if (getOwnerClan() != null && getFortState() == 0)
			{
				spawnSpecialEnvoys();
				ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(Fort.this),
						60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
			}
		}, 10000L);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("INSERT IGNORE INTO fort (id) VALUES (?)");
			statement.setInt(1, _fortId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception when creating fort's dynamic data: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Return function with id
	 */
	public FortFunction getFunction(int type)
	{
		if (_function.get(type) != null)
		{
			return _function.get(type);
		}
		return null;
	}

	public static class ScheduleSpecialEnvoysDeSpawn implements Runnable
	{
		private Fort _fortInst;

		public ScheduleSpecialEnvoysDeSpawn(Fort pFort)
		{
			_fortInst = pFort;
		}

		@Override
		public void run()
		{
			try
			{
				// if state not decided, change state to indenpendent
				if (_fortInst.getFortState() == 0)
				{
					_fortInst.setFortState(1, 0);
				}
				_fortInst.despawnSpecialEnvoys();
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING,
						"Exception: ScheduleSpecialEnvoysSpawn() for Fort " + _fortInst.getName() + ": " +
								e.getMessage(), e);
			}
		}
	}

	// =========================================================
	// Method - Public

	public void endOfSiege(L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
	}

	public void engrave(L2Clan clan)
	{
		setOwner(clan, true);
	}

	/**
	 * Move non clan members off fort area and to nearest town.<BR><BR>
	 */
	public void banishForeigners()
	{
		getFortZone().banishForeigners(getOwnerClan());
	}

	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getZone().isInsideZone(x, y, z);
	}

	public L2SiegeZone getZone()
	{
		if (_zone == null)
		{
			for (L2SiegeZone zone : ZoneManager.getInstance().getAllZones(L2SiegeZone.class))
			{
				if (zone.getSiegeObjectId() == _fortId)
				{
					_zone = zone;
					break;
				}
			}
		}
		return _zone;
	}

	public L2FortZone getFortZone()
	{
		if (_fortZone == null)
		{
			for (L2FortZone zone : ZoneManager.getInstance().getAllZones(L2FortZone.class))
			{
				if (zone.getFortId() == _fortId)
				{
					_fortZone = zone;
					break;
				}
			}
		}
		return _fortZone;
	}

	/**
	 * Get the objects distance to this fort
	 *
	 * @return
	 */
	public double getDistance(L2Object obj)
	{
		return getZone().getDistanceToZone(obj);
	}

	public void closeDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}

	public void openDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}

	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if (activeChar.getClan() != getOwnerClan())
		{
			return;
		}

		L2DoorInstance door = getDoor(doorId);
		if (door != null)
		{
			if (open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}

	/**
	 * This method will set owner for Fort
	 *
	 * @param clan
	 */
	public boolean setOwner(L2Clan clan, boolean updateClansReputation)
	{
		L2Clan oldowner = getOwnerClan();

		// Remove old owner
		if (oldowner != null && clan != null && clan != oldowner)
		{
			// Remove points from old owner
			updateClansReputation(oldowner, true);
			try
			{
				L2PcInstance oldleader = oldowner.getLeader().getPlayerInstance();
				if (oldleader != null)
				{
					if (oldleader.getMountType() == 2)
					{
						oldleader.dismount();
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Exception in setOwner: " + e.getMessage(), e);
			}
			removeOwner(true);
		}
		setFortState(0, 0); // initialize fort state

		//	if clan already have castle, don't store him in fortress
		if (clan.getHasCastle() > 0)
		{
			getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NPCS_RECAPTURED_FORTRESS));
			return false;
		}
		else
		{
			// Give points to new owner
			if (updateClansReputation)
			{
				updateClansReputation(clan, false);
			}

			spawnSpecialEnvoys();
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this),
					60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
			// if clan have already fortress, remove it
			if (clan.getHasFort() > 0)
			{
				FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
			}

			setBloodOathReward(0);
			setSupplyLvL(0);
			setOwnerClan(clan);
			updateOwnerInDB(); // Update in database
			saveFortVariables();

			if (getSiege().getIsInProgress()) // If siege in progress
			{
				getSiege().endSiege();
			}

			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				giveResidentialSkills(member);
				member.sendSkillList();
			}
			return true;
		}
	}

	public void removeOwner(boolean updateDB)
	{
		L2Clan clan = getOwnerClan();
		if (clan != null)
		{
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}
			clan.setHasFort(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			setOwnerClan(null);
			setBloodOathReward(0);
			setSupplyLvL(0);
			saveFortVariables();
			removeAllFunctions();
			if (updateDB)
			{
				updateOwnerInDB();
			}
		}
	}

	public void setBloodOathReward(int val)
	{
		_blood = val;
	}

	public int getBloodOathReward()
	{
		return _blood;
	}

	public void raiseSupplyLvL()
	{
		_supplyLvL++;
		if (_supplyLvL > Config.FS_MAX_SUPPLY_LEVEL)
		{
			_supplyLvL = Config.FS_MAX_SUPPLY_LEVEL;
		}
	}

	public void setSupplyLvL(int val)
	{
		if (val <= Config.FS_MAX_SUPPLY_LEVEL)
		{
			_supplyLvL = val;
		}
	}

	public int getSupplyLvL()
	{
		return _supplyLvL;
	}

	public void saveFortVariables()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET blood=?, supplyLvL=? WHERE id = ?");
			statement.setInt(1, _blood);
			statement.setInt(2, _supplyLvL);
			statement.setInt(3, _fortId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: saveFortVariables(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Show or hide flag inside flagpole<BR><BR>
	 */
	public void setVisibleFlag(boolean val)
	{
		L2StaticObjectInstance flagPole = getFlagPole();
		if (flagPole != null)
		{
			flagPole.setMeshIndex(val ? 1 : 0);
		}
	}

	/**
	 * Respawn all doors on fort grounds<BR><BR>
	 */
	public void resetDoors()
	{
		for (L2DoorInstance door : _doors)
		{
			if (door.getOpen())
			{
				door.closeMe();
			}
			if (door.isDead())
			{
				door.doRevive();
			}
			if (door.getCurrentHp() < door.getMaxHp())
			{
				door.setCurrentHp(door.getMaxHp());
			}
		}
	}

	// =========================================================
	// Method - Private
	// This method loads fort
	private void loadDbData()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(
					"SELECT siegeDate, lastOwnedTime, owner, state, castleId, blood, supplyLvl FROM fort WHERE id = ?");
			statement.setInt(1, _fortId);
			rs = statement.executeQuery();
			int ownerId = 0;

			if (rs.next())
			{
				_siegeDate = Calendar.getInstance();
				_lastOwnedTime = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				_lastOwnedTime.setTimeInMillis(rs.getLong("lastOwnedTime"));
				ownerId = rs.getInt("owner");
				_state = rs.getInt("state");
				_castleId = rs.getInt("castleId");
				_blood = rs.getInt("blood");
				_supplyLvL = rs.getInt("supplyLvL");
			}

			rs.close();
			statement.close();

			if (ownerId > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(ownerId); // Try to find clan instance
				clan.setHasFort(_fortId);
				setOwnerClan(clan);
				int runCount = getOwnedTime() / (Config.FS_UPDATE_FRQ * 60);
				long initial = System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis();
				while (initial > Config.FS_UPDATE_FRQ * 60000L)
				{
					initial -= Config.FS_UPDATE_FRQ * 60000L;
				}
				initial = Config.FS_UPDATE_FRQ * 60000L - initial;
				if (Config.FS_MAX_OWN_TIME <= 0 || getOwnedTime() < Config.FS_MAX_OWN_TIME * 3600)
				{
					_fortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
							new FortUpdater(this, clan, runCount, UpdaterType.PERIODIC_UPDATE), initial,
							Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
					if (Config.FS_MAX_OWN_TIME > 0)
					{
						_fortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
								new FortUpdater(this, clan, runCount, UpdaterType.MAX_OWN_TIME), 3600000,
								3600000); // Schedule owner tasks to remove owener
					}
				}
				else
				{
					_fortUpdater[1] = ThreadPoolManager.getInstance()
							.scheduleGeneral(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME),
									60000); // Schedule owner tasks to remove owner
				}
			}
			else
			{
				setOwnerClan(null);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: loadFortData(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Load All Functions
	 */
	private void loadFunctions()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM fort_functions WHERE fort_id = ?");
			statement.setInt(1, _fortId);
			rs = statement.executeQuery();
			while (rs.next())
			{
				_function.put(rs.getInt("type"),
						new FortFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"),
								rs.getLong("endTime"), true));
			}
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Fort.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Remove function In List and in DB
	 */
	public void removeFunction(int functionType)
	{
		_function.remove(functionType);
		Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM fort_functions WHERE fort_id=? AND type=?");
			statement.setInt(1, _fortId);
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Fort.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Remove all fort functions.
	 */
	private void removeAllFunctions()
	{
		HashMap<Integer, FortFunction> toIterate = new HashMap<>(_function);
		for (int id : toIterate.keySet())
		{
			removeFunction(id);
		}
	}

	public boolean updateFunctions(L2PcInstance player, int type, int lvl, int lease, long rate, boolean addNew)
	{
		if (player == null)
		{
			return false;
		}
		if (Config.DEBUG)
		{
			Log.warning(
					"Called Fort.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " +
							getOwnerClan());
		}
		if (lease > 0)
		{
			if (!player.destroyItemByItemId("Consume", 57, lease, null, true))
			{
				return false;
			}
		}
		if (addNew)
		{
			_function.put(type, new FortFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if (lvl == 0 && lease == 0)
			{
				removeFunction(type);
			}
			else
			{
				int diffLease = lease - _function.get(type).getLease();
				if (Config.DEBUG)
				{
					Log.warning("Called Fort.updateFunctions diffLease : " + diffLease);
				}
				if (diffLease > 0)
				{
					_function.remove(type);
					_function.put(type, new FortFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					_function.get(type).setLease(lease);
					_function.get(type).setLvl(lvl);
					_function.get(type).dbSave();
				}
			}
		}
		return true;
	}

	public void activateInstance()
	{
		loadDoor();
	}

	// This method loads fort door data from database
	private void loadDoor()
	{
		for (L2DoorInstance door : DoorTable.getInstance().getDoors())
		{
			if (door.getFort() != null && door.getFort()._fortId == _fortId)
			{
				_doors.add(door);
			}
		}
	}

	private void updateOwnerInDB()
	{
		L2Clan clan = getOwnerClan();
		int clanId = 0;
		if (clan != null)
		{
			clanId = clan.getClanId();
			_lastOwnedTime.setTimeInMillis(System.currentTimeMillis());
		}
		else
		{
			_lastOwnedTime.setTimeInMillis(0);
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement(
					"UPDATE fort SET owner=?,lastOwnedTime=?,state=?,castleId=?,blood=? WHERE id = ?");
			statement.setInt(1, clanId);
			statement.setLong(2, _lastOwnedTime.getTimeInMillis());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setInt(5, getBloodOathReward());
			statement.setInt(6, _fortId);
			statement.execute();
			statement.close();

			// ============================================================================
			// Announce to clan memebers
			if (clan != null)
			{
				clan.setHasFort(_fortId); // Set has fort flag for new owner
				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2);
				sm.addString(clan.getName());
				sm.addFortId(_fortId);
				Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
				for (L2PcInstance player : pls)
				{
					player.sendPacket(sm);
				}
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				if (_fortUpdater[0] != null)
				{
					_fortUpdater[0].cancel(false);
				}
				if (_fortUpdater[1] != null)
				{
					_fortUpdater[1].cancel(false);
				}
				_fortUpdater[0] = ThreadPoolManager.getInstance()
						.scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.PERIODIC_UPDATE),
								Config.FS_UPDATE_FRQ * 60000L,
								Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
				if (Config.FS_MAX_OWN_TIME > 0)
				{
					_fortUpdater[1] = ThreadPoolManager.getInstance()
							.scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME),
									3600000, 3600000); // Schedule owner tasks to remove owener
				}
			}
			else
			{
				if (_fortUpdater[0] != null)
				{
					_fortUpdater[0].cancel(false);
				}
				_fortUpdater[0] = null;
				if (_fortUpdater[1] != null)
				{
					_fortUpdater[1].cancel(false);
				}
				_fortUpdater[1] = null;
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final int getFortId()
	{
		return _fortId;
	}

	public final L2Clan getOwnerClan()
	{
		return _fortOwner;
	}

	public final void setOwnerClan(L2Clan clan)
	{
		setVisibleFlag(clan != null);
		_fortOwner = clan;
	}

	public final L2DoorInstance getDoor(int doorId)
	{
		if (doorId <= 0)
		{
			return null;
		}

		for (L2DoorInstance door : getDoors())
		{
			if (door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}

	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public final L2StaticObjectInstance getFlagPole()
	{
		return _flagPole;
	}

	public final FortSiege getSiege()
	{
		if (_siege == null)
		{
			_siege = new FortSiege(this);
		}
		return _siege;
	}

	public final Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public final void setSiegeDate(Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}

	public final int getOwnedTime()
	{
		if (_lastOwnedTime.getTimeInMillis() == 0)
		{
			return 0;
		}

		return (int) ((System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis()) / 1000);
	}

	public final int getTimeTillRebelArmy()
	{
		if (_lastOwnedTime.getTimeInMillis() == 0)
		{
			return 0;
		}

		return (int) (
				(_lastOwnedTime.getTimeInMillis() + Config.FS_MAX_OWN_TIME * 3600000L - System.currentTimeMillis()) /
						1000L);
	}

	public final long getTimeTillNextFortUpdate()
	{
		if (_fortUpdater[0] == null)
		{
			return 0;
		}
		return _fortUpdater[0].getDelay(TimeUnit.SECONDS);
	}

	public final String getName()
	{
		return _name;
	}

	public void updateClansReputation(L2Clan owner, boolean removePoints)
	{
		if (owner != null)
		{
			if (removePoints)
			{
				owner.takeReputationScore(Config.LOOSE_FORT_POINTS, true);
			}
			else
			{
				owner.addReputationScore(Config.TAKE_FORT_POINTS, true);
			}
		}
	}

	private static class endFortressSiege implements Runnable
	{
		private Fort _f;
		private L2Clan _clan;

		public endFortressSiege(Fort f, L2Clan clan)
		{
			_f = f;
			_clan = clan;
		}

		@Override
		public void run()
		{
			try
			{
				_f.engrave(_clan);
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "Exception in endFortressSiege " + e.getMessage(), e);
			}
		}
	}

	/**
	 * @return Returns state of fortress.<BR><BR>
	 * 0 - not decided yet<BR>
	 * 1 - independent<BR>
	 * 2 - contracted with castle<BR>
	 */
	public final int getFortState()
	{
		return _state;
	}

	/**
	 */
	public final void setFortState(int state, int castleId)
	{
		_state = state;
		_castleId = castleId;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET state=?,castleId=? WHERE id = ?");
			statement.setInt(1, getFortState());
			statement.setInt(2, getCastleId());
			statement.setInt(3, _fortId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: setFortState(int state, int castleId): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * @return Returns Castle Id of fortress contracted with castle.
	 */
	public final int getCastleId()
	{
		return _castleId;
	}

	/**
	 * @return Returns fortress type.<BR><BR>
	 * 0 - small (3 commanders) <BR>
	 * 1 - big (4 commanders + control room)
	 */
	public final int getFortType()
	{
		return _fortType;
	}

	public final int addEnvoyCastleId(int npcId, int castleId)
	{
		return _envoyCastles.put(npcId, castleId);
	}

	public final int getCastleIdFromEnvoy(int npcId)
	{
		return _envoyCastles.get(npcId);
	}

	/**
	 * @return Returns amount of barracks.
	 */
	public final int getFortSize()
	{
		return getFortType() == 0 ? 3 : 5;
	}

	public void spawnSuspiciousMerchant()
	{
		if (_isSuspiciousMerchantSpawned)
		{
			return;
		}

		_isSuspiciousMerchantSpawned = true;
		List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(_name + "_suspicious_merchant");
		for (L2Spawn spawnDat : spawns)
		{
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}

	public void despawnSuspiciousMerchant()
	{
		if (!_isSuspiciousMerchantSpawned)
		{
			return;
		}

		_isSuspiciousMerchantSpawned = false;
		List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(_name + "_suspicious_merchant");
		for (L2Spawn spawnDat : spawns)
		{
			spawnDat.stopRespawn();
			spawnDat.getNpc().deleteMe();
		}
	}

	public void spawnNpcCommanders()
	{
		SpawnTable.getInstance().spawnSpecificTable(_name + "_npc_commanders");
	}

	public void despawnNpcCommanders()
	{
		SpawnTable.getInstance().despawnSpecificTable(_name + "_npc_commanders");
	}

	public void spawnSpecialEnvoys()
	{
		SpawnTable.getInstance().spawnSpecificTable(_name + "_envoys");
	}

	public void despawnSpecialEnvoys()
	{
		SpawnTable.getInstance().despawnSpecificTable(_name + "_envoys");
	}

	/**
	 * Spawn guards.<BR><BR>
	 */
	public void spawnSiegeGuard()
	{
		try
		{
			List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(_name + "_siege_guards");
			for (L2Spawn spawnDat : spawns)
			{
				spawnDat.doSpawn();
				if (spawnDat.getNpc() instanceof L2FortBallistaInstance)
				{
					spawnDat.stopRespawn();
				}
				else
				{
					spawnDat.startRespawn();
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error spawning siege guards for fort " + getName() + ":" + e.getMessage(), e);
		}
	}

	/**
	 * Unspawn guards.<BR><BR>
	 */
	public void unspawnSiegeGuard()
	{
		try
		{
			List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(_name + "_siege_guards");
			for (L2Spawn spawnDat : spawns)
			{
				spawnDat.stopRespawn();
				if (spawnDat.getNpc() != null)
				{
					spawnDat.getNpc().doDie(spawnDat.getNpc());
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error unspawning siege guards for fort " + getName() + ":" + e.getMessage(), e);
		}
	}

	public ArrayList<L2Skill> getResidentialSkills()
	{
		return _residentialSkills;
	}

	public void giveResidentialSkills(L2PcInstance player)
	{
		if (_residentialSkills != null && !_residentialSkills.isEmpty())
		{
			for (L2Skill sk : _residentialSkills)
			{
				player.addSkill(sk, false);
			}
		}
	}

	public void removeResidentialSkills(L2PcInstance player)
	{
		if (_residentialSkills != null && !_residentialSkills.isEmpty())
		{
			for (L2Skill sk : _residentialSkills)
			{
				player.removeSkill(sk, false, true);
			}
		}
	}

	public List<L2Spawn> getCommanderSpawns()
	{
		return SpawnTable.getInstance().getSpecificSpawns(_name + "_defending_commanders");
	}

	public List<CombatFlag> getFlags()
	{
		return _flagList;
	}
}
