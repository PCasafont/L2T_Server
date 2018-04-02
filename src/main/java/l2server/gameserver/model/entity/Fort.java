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
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.FortBallistaInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.StaticObjectInstance;
import l2server.gameserver.model.zone.type.FortZone;
import l2server.gameserver.model.zone.type.SiegeZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Fort {
	private static Logger log = LoggerFactory.getLogger(Fort.class.getName());


	// =========================================================
	// Data Field
	private int fortId = 0;
	private List<DoorInstance> doors = new ArrayList<>();
	private StaticObjectInstance flagPole = null;
	private String name = "";
	private FortSiege siege = null;
	private Calendar siegeDate;
	private Calendar lastOwnedTime;
	private FortZone fortZone;
	private SiegeZone zone;
	private L2Clan fortOwner = null;
	private int fortType = 0;
	private int state = 0;
	private int castleId = 0;
	private int blood = 0;
	private int supplyLvL = 0;
	private HashMap<Integer, FortFunction> function;
	private ArrayList<Skill> residentialSkills = new ArrayList<>();
	private ScheduledFuture<?>[] fortUpdater = new ScheduledFuture<?>[2];
	private boolean isSuspiciousMerchantSpawned = false;

	private ArrayList<CombatFlag> flagList = new ArrayList<>();

	private TIntIntHashMap envoyCastles = new TIntIntHashMap(2);

	/**
	 * Fortress Functions
	 */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;

	public class FortFunction {
		private int type;
		private int lvl;
		protected int fee;
		protected int tempFee;
		private long rate;
		private long endDate;
		protected boolean inDebt;
		public boolean cwh;

		public FortFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh) {
			this.type = type;
			this.lvl = lvl;
			fee = lease;
			tempFee = tempLease;
			this.rate = rate;
			endDate = time;
			initializeTask(cwh);
		}

		public int getType() {
			return type;
		}

		public int getLvl() {
			return lvl;
		}

		public int getLease() {
			return fee;
		}

		public long getRate() {
			return rate;
		}

		public long getEndTime() {
			return endDate;
		}

		public void setLvl(int lvl) {
			this.lvl = lvl;
		}

		public void setLease(int lease) {
			fee = lease;
		}

		public void setEndTime(long time) {
			endDate = time;
		}

		private void initializeTask(boolean cwh) {
			if (getOwnerClan() == null) {
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (endDate > currentTime) {
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), endDate - currentTime);
			} else {
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), 0);
			}
		}

		private class FunctionTask implements Runnable {
			public FunctionTask(boolean cwh) {
				FortFunction.this.cwh = cwh;
			}

			@Override
			public void run() {
				try {
					if (getOwnerClan() == null) {
						return;
					}
					if (getOwnerClan().getWarehouse().getAdena() >= fee || !cwh) {
						int fee = FortFunction.this.fee;
						if (getEndTime() == -1) {
							fee = tempFee;
						}

						setEndTime(System.currentTimeMillis() + getRate());
						dbSave();
						if (cwh) {
							getOwnerClan().getWarehouse().destroyItemByItemId("CS_function_fee", 57, fee, null, null);
							if (Config.DEBUG) {
								log.warn("Deducted " + fee + " adena from " + getName() + " owner's cwh for function id : " + getType());
							}
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(true), getRate());
					} else {
						removeFunction(getType());
					}
				} catch (Throwable ignored) {
				}
			}
		}

		public void dbSave() {
			Connection con = null;
			try {
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("REPLACE INTO fort_functions (fort_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				statement.setInt(1, fortId);
				statement.setInt(2, getType());
				statement.setInt(3, getLvl());
				statement.setInt(4, getLease());
				statement.setLong(5, getRate());
				statement.setLong(6, getEndTime());
				statement.execute();
				statement.close();
			} catch (Exception e) {
				log.error(
						"Exception: Fort.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(),
						e);
			} finally {
				L2DatabaseFactory.close(con);
			}
		}
	}

	// =========================================================
	// Constructor
	public Fort(int id, String name, int type, int flagPoleId) {
		fortId = id;
		this.name = name;
		fortType = type;
		flagPole = StaticObjects.getInstance().getObject(flagPoleId);

		loadDbData();

		function = new HashMap<>();
		residentialSkills = ResidentialSkillTable.getInstance().getSkills(fortId);
		if (getOwnerClan() != null) {
			setVisibleFlag(true);
			loadFunctions();
		}

		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			spawnNpcCommanders(); // spawn npc Commanders
			if (getOwnerClan() != null && getFortState() == 0) {
				spawnSpecialEnvoys();
				ThreadPoolManager.getInstance()
						.scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(Fort.this), 60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
			}
		}, 10000L);

		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("INSERT IGNORE INTO fort (id) VALUES (?)");
			statement.setInt(1, fortId);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Exception when creating fort's dynamic data: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Return function with id
	 */
	public FortFunction getFunction(int type) {
		if (function.get(type) != null) {
			return function.get(type);
		}
		return null;
	}

	public static class ScheduleSpecialEnvoysDeSpawn implements Runnable {
		private Fort fortInst;

		public ScheduleSpecialEnvoysDeSpawn(Fort pFort) {
			fortInst = pFort;
		}

		@Override
		public void run() {
			try {
				// if state not decided, change state to indenpendent
				if (fortInst.getFortState() == 0) {
					fortInst.setFortState(1, 0);
				}
				fortInst.despawnSpecialEnvoys();
			} catch (Exception e) {
				log.warn("Exception: ScheduleSpecialEnvoysSpawn() for Fort " + fortInst.getName() + ": " + e.getMessage(), e);
			}
		}
	}

	// =========================================================
	// Method - Public

	public void endOfSiege(L2Clan clan) {
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
	}

	public void engrave(L2Clan clan) {
		setOwner(clan, true);
	}

	/**
	 * Move non clan members off fort area and to nearest town.<BR><BR>
	 */
	public void banishForeigners() {
		getFortZone().banishForeigners(getOwnerClan());
	}

	/**
	 * Return true if object is inside the zone
	 */
	public boolean checkIfInZone(int x, int y, int z) {
		return getZone().isInsideZone(x, y, z);
	}

	public SiegeZone getZone() {
		if (zone == null) {
			for (SiegeZone zone : ZoneManager.getInstance().getAllZones(SiegeZone.class)) {
				if (zone.getSiegeObjectId() == fortId) {
					this.zone = zone;
					break;
				}
			}
		}
		return zone;
	}

	public FortZone getFortZone() {
		if (fortZone == null) {
			for (FortZone zone : ZoneManager.getInstance().getAllZones(FortZone.class)) {
				if (zone.getFortId() == fortId) {
					fortZone = zone;
					break;
				}
			}
		}
		return fortZone;
	}

	/**
	 * Get the objects distance to this fort
	 *
	 */
	public double getDistance(WorldObject obj) {
		return getZone().getDistanceToZone(obj);
	}

	public void closeDoor(Player activeChar, int doorId) {
		openCloseDoor(activeChar, doorId, false);
	}

	public void openDoor(Player activeChar, int doorId) {
		openCloseDoor(activeChar, doorId, true);
	}

	public void openCloseDoor(Player activeChar, int doorId, boolean open) {
		if (activeChar.getClan() != getOwnerClan()) {
			return;
		}

		DoorInstance door = getDoor(doorId);
		if (door != null) {
			if (open) {
				door.openMe();
			} else {
				door.closeMe();
			}
		}
	}

	/**
	 * This method will set owner for Fort
	 *
	 */
	public boolean setOwner(L2Clan clan, boolean updateClansReputation) {
		L2Clan oldowner = getOwnerClan();

		// Remove old owner
		if (oldowner != null && clan != null && clan != oldowner) {
			// Remove points from old owner
			updateClansReputation(oldowner, true);
			try {
				Player oldleader = oldowner.getLeader().getPlayerInstance();
				if (oldleader != null) {
					if (oldleader.getMountType() == 2) {
						oldleader.dismount();
					}
				}
			} catch (Exception e) {
				log.warn("Exception in setOwner: " + e.getMessage(), e);
			}
			removeOwner(true);
		}
		setFortState(0, 0); // initialize fort state

		//	if clan already have castle, don't store him in fortress
		if (clan.getHasCastle() > 0) {
			getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NPCS_RECAPTURED_FORTRESS));
			return false;
		} else {
			// Give points to new owner
			if (updateClansReputation) {
				updateClansReputation(clan, false);
			}

			spawnSpecialEnvoys();
			ThreadPoolManager.getInstance()
					.scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this), 60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
			// if clan have already fortress, remove it
			if (clan.getHasFort() > 0) {
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

			for (Player member : clan.getOnlineMembers(0)) {
				giveResidentialSkills(member);
				member.sendSkillList();
			}
			return true;
		}
	}

	public void removeOwner(boolean updateDB) {
		L2Clan clan = getOwnerClan();
		if (clan != null) {
			for (Player member : clan.getOnlineMembers(0)) {
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
			if (updateDB) {
				updateOwnerInDB();
			}
		}
	}

	public void setBloodOathReward(int val) {
		blood = val;
	}

	public int getBloodOathReward() {
		return blood;
	}

	public void raiseSupplyLvL() {
		supplyLvL++;
		if (supplyLvL > Config.FS_MAX_SUPPLY_LEVEL) {
			supplyLvL = Config.FS_MAX_SUPPLY_LEVEL;
		}
	}

	public void setSupplyLvL(int val) {
		if (val <= Config.FS_MAX_SUPPLY_LEVEL) {
			supplyLvL = val;
		}
	}

	public int getSupplyLvL() {
		return supplyLvL;
	}

	public void saveFortVariables() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET blood=?, supplyLvL=? WHERE id = ?");
			statement.setInt(1, blood);
			statement.setInt(2, supplyLvL);
			statement.setInt(3, fortId);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Exception: saveFortVariables(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Show or hide flag inside flagpole<BR><BR>
	 */
	public void setVisibleFlag(boolean val) {
		StaticObjectInstance flagPole = getFlagPole();
		if (flagPole != null) {
			flagPole.setMeshIndex(val ? 1 : 0);
		}
	}

	/**
	 * Respawn all doors on fort grounds<BR><BR>
	 */
	public void resetDoors() {
		for (DoorInstance door : doors) {
			if (door.getOpen()) {
				door.closeMe();
			}
			if (door.isDead()) {
				door.doRevive();
			}
			if (door.getCurrentHp() < door.getMaxHp()) {
				door.setCurrentHp(door.getMaxHp());
			}
		}
	}

	// =========================================================
	// Method - Private
	// This method loads fort
	private void loadDbData() {
		Connection con = null;
		try {
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT siegeDate, lastOwnedTime, owner, state, castleId, blood, supplyLvl FROM fort WHERE id = ?");
			statement.setInt(1, fortId);
			rs = statement.executeQuery();
			int ownerId = 0;

			if (rs.next()) {
				siegeDate = Calendar.getInstance();
				lastOwnedTime = Calendar.getInstance();
				siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				lastOwnedTime.setTimeInMillis(rs.getLong("lastOwnedTime"));
				ownerId = rs.getInt("owner");
				state = rs.getInt("state");
				castleId = rs.getInt("castleId");
				blood = rs.getInt("blood");
				supplyLvL = rs.getInt("supplyLvL");
			}

			rs.close();
			statement.close();

			if (ownerId > 0) {
				L2Clan clan = ClanTable.getInstance().getClan(ownerId); // Try to find clan instance
				clan.setHasFort(fortId);
				setOwnerClan(clan);
				int runCount = getOwnedTime() / (Config.FS_UPDATE_FRQ * 60);
				long initial = System.currentTimeMillis() - lastOwnedTime.getTimeInMillis();
				while (initial > Config.FS_UPDATE_FRQ * 60000L) {
					initial -= Config.FS_UPDATE_FRQ * 60000L;
				}
				initial = Config.FS_UPDATE_FRQ * 60000L - initial;
				if (Config.FS_MAX_OWN_TIME <= 0 || getOwnedTime() < Config.FS_MAX_OWN_TIME * 3600) {
					fortUpdater[0] = ThreadPoolManager.getInstance()
							.scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, UpdaterType.PERIODIC_UPDATE),
									initial,
									Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
					if (Config.FS_MAX_OWN_TIME > 0) {
						fortUpdater[1] = ThreadPoolManager.getInstance()
								.scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, UpdaterType.MAX_OWN_TIME),
										3600000,
										3600000); // Schedule owner tasks to remove owener
					}
				} else {
					fortUpdater[1] = ThreadPoolManager.getInstance()
							.scheduleGeneral(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME), 60000); // Schedule owner tasks to remove owner
				}
			} else {
				setOwnerClan(null);
			}
		} catch (Exception e) {
			log.warn("Exception: loadFortData(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Load All Functions
	 */
	private void loadFunctions() {
		Connection con = null;
		try {
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM fort_functions WHERE fort_id = ?");
			statement.setInt(1, fortId);
			rs = statement.executeQuery();
			while (rs.next()) {
				function.put(rs.getInt("type"),
						new FortFunction(rs.getInt("type"),
								rs.getInt("lvl"),
								rs.getInt("lease"),
								0,
								rs.getLong("rate"),
								rs.getLong("endTime"),
								true));
			}
			statement.close();
		} catch (Exception e) {
			log.error("Exception: Fort.loadFunctions(): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Remove function In List and in DB
	 */
	public void removeFunction(int functionType) {
		function.remove(functionType);
		Connection con = null;
		try {
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM fort_functions WHERE fort_id=? AND type=?");
			statement.setInt(1, fortId);
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.error("Exception: Fort.removeFunctions(int functionType): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Remove all fort functions.
	 */
	private void removeAllFunctions() {
		HashMap<Integer, FortFunction> toIterate = new HashMap<>(function);
		for (int id : toIterate.keySet()) {
			removeFunction(id);
		}
	}

	public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate, boolean addNew) {
		if (player == null) {
			return false;
		}
		if (Config.DEBUG) {
			log.warn("Called Fort.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " + getOwnerClan());
		}
		if (lease > 0) {
			if (!player.destroyItemByItemId("Consume", 57, lease, null, true)) {
				return false;
			}
		}
		if (addNew) {
			function.put(type, new FortFunction(type, lvl, lease, 0, rate, 0, false));
		} else {
			if (lvl == 0 && lease == 0) {
				removeFunction(type);
			} else {
				int diffLease = lease - function.get(type).getLease();
				if (Config.DEBUG) {
					log.warn("Called Fort.updateFunctions diffLease : " + diffLease);
				}
				if (diffLease > 0) {
					function.remove(type);
					function.put(type, new FortFunction(type, lvl, lease, 0, rate, -1, false));
				} else {
					function.get(type).setLease(lease);
					function.get(type).setLvl(lvl);
					function.get(type).dbSave();
				}
			}
		}
		return true;
	}

	public void activateInstance() {
		loadDoor();
	}

	// This method loads fort door data from database
	private void loadDoor() {
		for (DoorInstance door : DoorTable.getInstance().getDoors()) {
			if (door.getFort() != null && door.getFort().fortId == fortId) {
				doors.add(door);
			}
		}
	}

	private void updateOwnerInDB() {
		L2Clan clan = getOwnerClan();
		int clanId = 0;
		if (clan != null) {
			clanId = clan.getClanId();
			lastOwnedTime.setTimeInMillis(System.currentTimeMillis());
		} else {
			lastOwnedTime.setTimeInMillis(0);
		}

		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET owner=?,lastOwnedTime=?,state=?,castleId=?,blood=? WHERE id = ?");
			statement.setInt(1, clanId);
			statement.setLong(2, lastOwnedTime.getTimeInMillis());
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.setInt(5, getBloodOathReward());
			statement.setInt(6, fortId);
			statement.execute();
			statement.close();

			// ============================================================================
			// Announce to clan memebers
			if (clan != null) {
				clan.setHasFort(fortId); // Set has fort flag for new owner
				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2);
				sm.addString(clan.getName());
				sm.addFortId(fortId);
				Collection<Player> pls = World.getInstance().getAllPlayers().values();
				for (Player player : pls) {
					player.sendPacket(sm);
				}
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				if (fortUpdater[0] != null) {
					fortUpdater[0].cancel(false);
				}
				if (fortUpdater[1] != null) {
					fortUpdater[1].cancel(false);
				}
				fortUpdater[0] = ThreadPoolManager.getInstance()
						.scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.PERIODIC_UPDATE),
								Config.FS_UPDATE_FRQ * 60000L,
								Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
				if (Config.FS_MAX_OWN_TIME > 0) {
					fortUpdater[1] = ThreadPoolManager.getInstance()
							.scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME),
									3600000,
									3600000); // Schedule owner tasks to remove owener
				}
			} else {
				if (fortUpdater[0] != null) {
					fortUpdater[0].cancel(false);
				}
				fortUpdater[0] = null;
				if (fortUpdater[1] != null) {
					fortUpdater[1].cancel(false);
				}
				fortUpdater[1] = null;
			}
		} catch (Exception e) {
			log.warn("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	public final int getFortId() {
		return fortId;
	}

	public final L2Clan getOwnerClan() {
		return fortOwner;
	}

	public final void setOwnerClan(L2Clan clan) {
		setVisibleFlag(clan != null);
		fortOwner = clan;
	}

	public final DoorInstance getDoor(int doorId) {
		if (doorId <= 0) {
			return null;
		}

		for (DoorInstance door : getDoors()) {
			if (door.getDoorId() == doorId) {
				return door;
			}
		}
		return null;
	}

	public final List<DoorInstance> getDoors() {
		return doors;
	}

	public final StaticObjectInstance getFlagPole() {
		return flagPole;
	}

	public final FortSiege getSiege() {
		if (siege == null) {
			siege = new FortSiege(this);
		}
		return siege;
	}

	public final Calendar getSiegeDate() {
		return siegeDate;
	}

	public final void setSiegeDate(Calendar siegeDate) {
		this.siegeDate = siegeDate;
	}

	public final int getOwnedTime() {
		if (lastOwnedTime.getTimeInMillis() == 0) {
			return 0;
		}

		return (int) ((System.currentTimeMillis() - lastOwnedTime.getTimeInMillis()) / 1000);
	}

	public final int getTimeTillRebelArmy() {
		if (lastOwnedTime.getTimeInMillis() == 0) {
			return 0;
		}

		return (int) ((lastOwnedTime.getTimeInMillis() + Config.FS_MAX_OWN_TIME * 3600000L - System.currentTimeMillis()) / 1000L);
	}

	public final long getTimeTillNextFortUpdate() {
		if (fortUpdater[0] == null) {
			return 0;
		}
		return fortUpdater[0].getDelay(TimeUnit.SECONDS);
	}

	public final String getName() {
		return name;
	}

	public void updateClansReputation(L2Clan owner, boolean removePoints) {
		if (owner != null) {
			if (removePoints) {
				owner.takeReputationScore(Config.LOOSE_FORT_POINTS, true);
			} else {
				owner.addReputationScore(Config.TAKE_FORT_POINTS, true);
			}
		}
	}

	private static class endFortressSiege implements Runnable {
		private Fort f;
		private L2Clan clan;

		public endFortressSiege(Fort f, L2Clan clan) {
			this.f = f;
			this.clan = clan;
		}

		@Override
		public void run() {
			try {
				f.engrave(clan);
			} catch (Exception e) {
				log.warn("Exception in endFortressSiege " + e.getMessage(), e);
			}
		}
	}

	/**
	 * @return Returns state of fortress.<BR><BR>
	 * 0 - not decided yet<BR>
	 * 1 - independent<BR>
	 * 2 - contracted with castle<BR>
	 */
	public final int getFortState() {
		return state;
	}

	public final void setFortState(int state, int castleId) {
		this.state = state;
		this.castleId = castleId;
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE fort SET state=?,castleId=? WHERE id = ?");
			statement.setInt(1, getFortState());
			statement.setInt(2, getCastleId());
			statement.setInt(3, fortId);
			statement.execute();
			statement.close();
		} catch (Exception e) {
			log.warn("Exception: setFortState(int state, int castleId): " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * @return Returns Castle Id of fortress contracted with castle.
	 */
	public final int getCastleId() {
		return castleId;
	}

	/**
	 * @return Returns fortress type.<BR><BR>
	 * 0 - small (3 commanders) <BR>
	 * 1 - big (4 commanders + control room)
	 */
	public final int getFortType() {
		return fortType;
	}

	public final int addEnvoyCastleId(int npcId, int castleId) {
		return envoyCastles.put(npcId, castleId);
	}

	public final int getCastleIdFromEnvoy(int npcId) {
		return envoyCastles.get(npcId);
	}

	/**
	 * @return Returns amount of barracks.
	 */
	public final int getFortSize() {
		return getFortType() == 0 ? 3 : 5;
	}

	public void spawnSuspiciousMerchant() {
		if (isSuspiciousMerchantSpawned) {
			return;
		}

		isSuspiciousMerchantSpawned = true;
		List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(name + "_suspicious_merchant");
		for (L2Spawn spawnDat : spawns) {
			spawnDat.doSpawn();
			spawnDat.startRespawn();
		}
	}

	public void despawnSuspiciousMerchant() {
		if (!isSuspiciousMerchantSpawned) {
			return;
		}

		isSuspiciousMerchantSpawned = false;
		List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(name + "_suspicious_merchant");
		for (L2Spawn spawnDat : spawns) {
			spawnDat.stopRespawn();
			spawnDat.getNpc().deleteMe();
		}
	}

	public void spawnNpcCommanders() {
		SpawnTable.getInstance().spawnSpecificTable(name + "_npc_commanders");
	}

	public void despawnNpcCommanders() {
		SpawnTable.getInstance().despawnSpecificTable(name + "_npc_commanders");
	}

	public void spawnSpecialEnvoys() {
		SpawnTable.getInstance().spawnSpecificTable(name + "_envoys");
	}

	public void despawnSpecialEnvoys() {
		SpawnTable.getInstance().despawnSpecificTable(name + "_envoys");
	}

	/**
	 * Spawn guards.<BR><BR>
	 */
	public void spawnSiegeGuard() {
		try {
			List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(name + "_siege_guards");
			for (L2Spawn spawnDat : spawns) {
				spawnDat.doSpawn();
				if (spawnDat.getNpc() instanceof FortBallistaInstance) {
					spawnDat.stopRespawn();
				} else {
					spawnDat.startRespawn();
				}
			}
		} catch (Exception e) {
			log.warn("Error spawning siege guards for fort " + getName() + ":" + e.getMessage(), e);
		}
	}

	/**
	 * Unspawn guards.<BR><BR>
	 */
	public void unspawnSiegeGuard() {
		try {
			List<L2Spawn> spawns = SpawnTable.getInstance().getSpecificSpawns(name + "_siege_guards");
			for (L2Spawn spawnDat : spawns) {
				spawnDat.stopRespawn();
				if (spawnDat.getNpc() != null) {
					spawnDat.getNpc().doDie(spawnDat.getNpc());
				}
			}
		} catch (Exception e) {
			log.warn("Error unspawning siege guards for fort " + getName() + ":" + e.getMessage(), e);
		}
	}

	public ArrayList<Skill> getResidentialSkills() {
		return residentialSkills;
	}

	public void giveResidentialSkills(Player player) {
		if (residentialSkills != null && !residentialSkills.isEmpty()) {
			for (Skill sk : residentialSkills) {
				player.addSkill(sk, false);
			}
		}
	}

	public void removeResidentialSkills(Player player) {
		if (residentialSkills != null && !residentialSkills.isEmpty()) {
			for (Skill sk : residentialSkills) {
				player.removeSkill(sk, false, true);
			}
		}
	}

	public List<L2Spawn> getCommanderSpawns() {
		return SpawnTable.getInstance().getSpecificSpawns(name + "_defending_commanders");
	}

	public List<CombatFlag> getFlags() {
		return flagList;
	}
}
