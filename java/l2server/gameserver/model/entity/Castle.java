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
import l2server.gameserver.CastleUpdater;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.DoorTable;
import l2server.gameserver.datatables.ResidentialSkillTable;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2server.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2server.gameserver.instancemanager.FortManager;
import l2server.gameserver.instancemanager.ZoneManager;
import l2server.gameserver.model.*;
import l2server.gameserver.model.actor.instance.L2ArtefactInstance;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.model.zone.type.L2CastleTeleportZone;
import l2server.gameserver.model.zone.type.L2CastleZone;
import l2server.gameserver.model.zone.type.L2SiegeZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExCastleTendency;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Broadcast;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

public class Castle
{

	// =========================================================
	// Data Field
	private List<CropProcure> procure = new ArrayList<>();
	private List<SeedProduction> production = new ArrayList<>();
	private List<CropProcure> procureNext = new ArrayList<>();
	private List<SeedProduction> productionNext = new ArrayList<>();

	private boolean isNextPeriodApproved = false;

	private static final String CASTLE_MANOR_DELETE_PRODUCTION =
			"DELETE FROM castle_manor_production WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD =
			"DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
	private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD =
			"DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
	private static final String CASTLE_UPDATE_CROP =
			"UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
	private static final String CASTLE_UPDATE_SEED =
			"UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";
	// =========================================================
	// Data Field
	private int castleId = 0;
	private List<L2DoorInstance> doors = new ArrayList<>();
	private String name = "";
	private int ownerId = 0;
	private Siege siege = null;
	private Calendar siegeDate;
	private boolean isTimeRegistrationOver = true;
	// true if Castle Lords set the time, or 24h is elapsed after the siege
	private Calendar siegeTimeRegistrationEndDate; // last siege end date + 1 day
	private int taxPercent = 0;
	private double taxRate = 0;
	private long treasury = 0;
	private boolean showNpcCrest = false;
	private L2SiegeZone zone = null;
	private L2CastleZone castleZone = null;
	private L2CastleTeleportZone teleZone;
	private L2Clan formerOwner = null;
	private List<L2ArtefactInstance> artefacts = new ArrayList<>(1);
	private TIntIntHashMap engrave = new TIntIntHashMap(1);
	private Map<Integer, CastleFunction> function;
	private ArrayList<L2Skill> residentialSkills = new ArrayList<>();
	private int bloodAlliance = 0;

	public static final int TENDENCY_NONE = 0;
	public static final int TENDENCY_LIGHT = 1;
	public static final int TENDENCY_DARKNESS = 2;

	private int tendency = TENDENCY_NONE;

	/**
	 * Castle Functions
	 */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_RESTORE_HP = 2;
	public static final int FUNC_RESTORE_MP = 3;
	public static final int FUNC_RESTORE_EXP = 4;
	public static final int FUNC_SUPPORT = 5;

	public class CastleFunction
	{
		private int type;
		private int lvl;
		protected int fee;
		protected int tempFee;
		private long rate;
		private long endDate;
		protected boolean inDebt;
		public boolean cwh;

		public CastleFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			this.type = type;
			this.lvl = lvl;
			this.fee = lease;
			this.tempFee = tempLease;
			this.rate = rate;
			this.endDate = time;
			initializeTask(cwh);
		}

		public int getType()
		{
			return this.type;
		}

		public int getLvl()
		{
			return this.lvl;
		}

		public int getLease()
		{
			return this.fee;
		}

		public long getRate()
		{
			return this.rate;
		}

		public long getEndTime()
		{
			return this.endDate;
		}

		public void setLvl(int lvl)
		{
			this.lvl = lvl;
		}

		public void setLease(int lease)
		{
			this.fee = lease;
		}

		public void setEndTime(long time)
		{
			this.endDate = time;
		}

		private void initializeTask(boolean cwh)
		{
			if (getOwnerId() <= 0)
			{
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (this.endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), this.endDate - currentTime);
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
				CastleFunction.this.cwh = cwh;
			}

			@Override
			public void run()
			{
				try
				{
					if (getOwnerId() <= 0)
					{
						return;
					}
					if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= CastleFunction.this.fee || !CastleFunction.this.cwh)
					{
						int fee = CastleFunction.this.fee;
						if (getEndTime() == -1)
						{
							fee = CastleFunction.this.tempFee;
						}

						setEndTime(System.currentTimeMillis() + getRate());
						dbSave();
						if (CastleFunction.this.cwh)
						{
							ClanTable.getInstance().getClan(getOwnerId()).getWarehouse()
									.destroyItemByItemId("CS_function_fee", 57, fee, null, null);
							if (Config.DEBUG)
							{
								Log.warning("deducted " + fee + " adena from " + getName() +
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
				catch (Exception e)
				{
					Log.log(Level.SEVERE, "", e);
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
						"REPLACE INTO castle_functions (castle_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				statement.setInt(1, getCastleId());
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
						"Exception: Castle.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " +
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
	public Castle(int castleId)
	{
		this.castleId = castleId;
		/*if (this.castleId == 7 || castleId == 9) // Goddard and Schuttgart
            this.nbArtifact = 2;*/
	}

	// =========================================================
	// Method - Public

	/**
	 * Return function with id
	 */
	public CastleFunction getFunction(int type)
	{
		if (this.function.get(type) != null)
		{
			return this.function.get(type);
		}
		return null;
	}

	public synchronized void engrave(L2Clan clan, L2Object target)
	{
		if (!this.artefacts.contains(target))
		{
			return;
		}
		this.engrave.put(target.getObjectId(), clan.getClanId());

		//Broadcast.toGameMasters("Engraved = " + this.engrave.size() + ". Arts = " + artAmount);
		if (this.engrave.size() == this.artefacts.size())
		{
			for (L2ArtefactInstance art : this.artefacts)
			{
				if (this.engrave.get(art.getObjectId()) != clan.getClanId())
				{
					//Broadcast.toGameMasters("BLABLABLA");
					return;
				}
			}
			this.engrave.clear();
			setOwner(clan);
		}
	}

	// This method add to the treasury

	/**
	 * Add amount to castle instance's treasury (warehouse).
	 */
	public void addToTreasury(long amount)
	{
		// check if owned
		if (getOwnerId() <= 0)
		{
			return;
		}

		if (this.name.equalsIgnoreCase("Schuttgart") || this.name.equalsIgnoreCase("Goddard"))
		{
			Castle rune = CastleManager.getInstance().getCastle("rune");
			if (rune != null)
			{
				long runeTax = (long) (amount * rune.getTaxRate());
				if (rune.getOwnerId() > 0)
				{
					rune.addToTreasury(runeTax);
				}
				amount -= runeTax;
			}
		}
		if (!this.name.equalsIgnoreCase("aden") && !this.name.equalsIgnoreCase("Rune") &&
				!this.name.equalsIgnoreCase("Schuttgart") && !this.name.equalsIgnoreCase(
				"Goddard")) // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
		{
			Castle aden = CastleManager.getInstance().getCastle("aden");
			if (aden != null)
			{
				long adenTax = (long) (amount *
						aden.getTaxRate()); // Find out what Aden gets from the current castle instance's income
				if (aden.getOwnerId() > 0)
				{
					aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
				}

				amount -= adenTax; // Subtract Aden's income from current castle instance's income
			}
		}

		addToTreasuryNoTax(amount);
	}

	/**
	 * Add amount to castle instance's treasury (warehouse), no tax paying.
	 */
	public boolean addToTreasuryNoTax(long amount)
	{
		if (getOwnerId() <= 0)
		{
			return false;
		}

		if (amount < 0)
		{
			amount *= -1;
			if (this.treasury < amount)
			{
				return false;
			}
			this.treasury -= amount;
		}
		else
		{
			if (this.treasury + amount > PcInventory.MAX_ADENA)
			{
				this.treasury = PcInventory.MAX_ADENA;
			}
			else
			{
				this.treasury += amount;
			}
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE castle SET treasury = ? WHERE id = ?");
			statement.setLong(1, getTreasury());
			statement.setInt(2, getCastleId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return true;
	}

	/**
	 * Move non clan members off castle area and to nearest town.<BR><BR>
	 */
	public void banishForeigners()
	{
		getCastleZone().banishForeigners(getOwnerId());
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
		if (this.zone == null)
		{
			for (L2SiegeZone zone : ZoneManager.getInstance().getAllZones(L2SiegeZone.class))
			{
				if (zone.getSiegeObjectId() == getCastleId())
				{
					this.zone = zone;
					break;
				}
			}
		}
		return this.zone;
	}

	public L2CastleZone getCastleZone()
	{
		if (this.castleZone == null)
		{
			for (L2CastleZone zone : ZoneManager.getInstance().getAllZones(L2CastleZone.class))
			{
				if (zone.getCastleId() == getCastleId())
				{
					this.castleZone = zone;
					break;
				}
			}
		}
		return this.castleZone;
	}

	public L2CastleTeleportZone getTeleZone()
	{
		if (this.teleZone == null)
		{
			for (L2CastleTeleportZone zone : ZoneManager.getInstance().getAllZones(L2CastleTeleportZone.class))
			{
				if (zone.getCastleId() == getCastleId())
				{
					this.teleZone = zone;
					break;
				}
			}
		}
		return this.teleZone;
	}

	public void oustAllPlayers()
	{
		getTeleZone().oustAllPlayers();
	}

	/**
	 * Get the objects distance to this castle
	 *
	 * @param obj
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
		if (activeChar.getClanId() != getOwnerId())
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

	// This method is used to begin removing all castle upgrades
	public void removeUpgrade()
	{
		Set<Integer> toIterate = new HashSet<>(this.function.keySet());
		toIterate.forEach(this::removeFunction);
		this.function.clear();
	}

	// This method updates the castle tax rate
	public void setOwner(L2Clan clan)
	{
		// Remove old owner
		if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
		{
			L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
			if (oldOwner != null)
			{
				if (this.formerOwner == null)
				{
					this.formerOwner = oldOwner;
					if (Config.REMOVE_CASTLE_CIRCLETS)
					{
						CastleManager.getInstance().removeCirclet(this.formerOwner, getCastleId());
					}
				}
				try
				{
					L2PcInstance oldleader = oldOwner.getLeader().getPlayerInstance();
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
				oldOwner.setHasCastle(0); // Unset has castle flag for old owner
				for (L2PcInstance member : oldOwner.getOnlineMembers(0))
				{
					removeResidentialSkills(member);
					member.sendSkillList();
				}

				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_CHATBOX_S1);
				sm.addString(oldOwner.getName() + " has lost " + getName() + " Castle.");

				Broadcast.toAllOnlinePlayers(sm);
			}
		}

		updateOwnerInDB(clan); // Update in database
		setShowNpcCrest(false);

		// if clan have fortress, remove it
		if (clan.getHasFort() > 0)
		{
			FortManager.getInstance().getFortByOwner(clan).removeOwner(true);
		}

		if (getSiege().getIsInProgress()) // If siege in progress
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}

		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			giveResidentialSkills(member);
			member.sendSkillList();
		}
	}

	public void removeOwner(L2Clan clan)
	{
		if (clan != null)
		{
			this.formerOwner = clan;
			if (Config.REMOVE_CASTLE_CIRCLETS)
			{
				CastleManager.getInstance().removeCirclet(this.formerOwner, getCastleId());
			}
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}
			clan.setHasCastle(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}

		updateOwnerInDB(null);
		if (getSiege().getIsInProgress())
		{
			getSiege().midVictory();
		}
		else
		{
			getSiege().removeSiegeClan(clan);
		}

		for (Map.Entry<Integer, CastleFunction> fc : this.function.entrySet())
		{
			removeFunction(fc.getKey());
		}
		this.function.clear();
	}

	// This method updates the castle tax rate
	public void setTaxPercent(L2PcInstance activeChar, int taxPercent)
	{
		int maxTax = this.tendency == 2 ? 30 : 0;

		if (taxPercent < 0 || taxPercent > maxTax)
		{
			activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
			return;
		}

		setTaxPercent(taxPercent);
		activeChar.sendMessage(getName() + " castle tax changed to " + taxPercent + "%.");
	}

	public void setTaxPercent(int taxPercent)
	{
		this.taxPercent = taxPercent;
		this.taxRate = this.taxPercent / 100.0;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update castle set taxPercent = ? where id = ?");
			statement.setInt(1, taxPercent);
			statement.setInt(2, getCastleId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Respawn all doors on castle grounds<BR><BR>
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}

	/**
	 * Respawn all doors on castle grounds<BR><BR>
	 */
	public void spawnDoor(boolean isDoorWeak)
	{
		for (L2DoorInstance door : this.doors)
		{
			if (door.isDead())
			{
				door.doRevive();
				if (isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				else
				{
					door.setCurrentHp(door.getMaxHp());
				}
			}

			if (door.getOpen())
			{
				door.closeMe();
			}
		}
	}

	// =========================================================
	// Method - Private
	// This method loads castle
	public void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("Select * from castle where id = ?");
			statement.setInt(1, getCastleId());
			rs = statement.executeQuery();

			if (rs.next())
			{
				this.name = rs.getString("name");
				//_OwnerId = rs.getInt("ownerId");

				this.siegeDate = Calendar.getInstance();
				this.siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				this.siegeTimeRegistrationEndDate = Calendar.getInstance();
				this.siegeTimeRegistrationEndDate.setTimeInMillis(rs.getLong("regTimeEnd"));
				this.isTimeRegistrationOver = rs.getBoolean("regTimeOver");

				this.taxPercent = rs.getInt("taxPercent");
				this.treasury = rs.getLong("treasury");

				this.showNpcCrest = rs.getBoolean("showNpcCrest");

				this.bloodAlliance = rs.getInt("bloodAlliance");

				this.tendency = rs.getInt("tendency");
			}
			rs.close();
			statement.close();

			this.taxRate = this.taxPercent / 100.0;

			statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
			statement.setInt(1, getCastleId());
			rs = statement.executeQuery();

			while (rs.next())
			{
				this.ownerId = rs.getInt("clan_id");
			}

			if (getOwnerId() > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(getOwnerId()); // Try to find clan instance
				ThreadPoolManager.getInstance()
						.scheduleGeneral(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
				clan.checkTendency();
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: loadCastleData(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		this.function = new HashMap<>();
		this.residentialSkills = ResidentialSkillTable.getInstance().getSkills(getCastleId());
		if (getOwnerId() != 0)
		{
			loadFunctions();
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
			statement = con.prepareStatement("Select * from castle_functions where castle_id = ?");
			statement.setInt(1, getCastleId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				this.function.put(rs.getInt("type"),
						new CastleFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0,
								rs.getLong("rate"), rs.getLong("endTime"), true));
			}
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Castle.loadFunctions(): " + e.getMessage(), e);
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
		this.function.remove(functionType);
		Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM castle_functions WHERE castle_id=? AND type=?");
			statement.setInt(1, getCastleId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Castle.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
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
					"Called Castle.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " +
							getOwnerId());
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
			this.function.put(type, new CastleFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if (lvl == 0 && lease == 0)
			{
				removeFunction(type);
			}
			else
			{
				int diffLease = lease - this.function.get(type).getLease();
				if (Config.DEBUG)
				{
					Log.warning("Called Castle.updateFunctions diffLease : " + diffLease);
				}
				if (diffLease > 0)
				{
					this.function.remove(type);
					this.function.put(type, new CastleFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					this.function.get(type).setLease(lease);
					this.function.get(type).setLvl(lvl);
					this.function.get(type).dbSave();
				}
			}
		}
		return true;
	}

	public void activateInstance()
	{
		loadDoor();
	}

	// This method loads castle door data from database
	private void loadDoor()
	{
		for (L2DoorInstance door : DoorTable.getInstance().getDoors())
		{
			if (door.getCastle() != null && door.getCastle().getCastleId() == getCastleId())
			{
				this.doors.add(door);
			}
		}
		//Logozo.info("Castle "+this+" loaded "+_doors.size()+" doors.");
	}

	private void updateOwnerInDB(L2Clan clan)
	{
		if (clan != null)
		{
			this.ownerId = clan.getClanId(); // Update owner id property
		}
		else
		{
			this.ownerId = 0; // Remove owner
			resetManor();
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			// ============================================================================
			// NEED TO REMOVE HAS CASTLE FLAG FROM CLAN_DATA
			// SHOULD BE CHECKED FROM CASTLE TABLE
			statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
			statement.setInt(1, getCastleId());
			statement.execute();
			statement.close();

			statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
			statement.setInt(1, getCastleId());
			statement.setInt(2, getOwnerId());
			statement.execute();
			statement.close();
			// ============================================================================

			// Announce to clan memebers
			if (clan != null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LIGHT_BLUE_DOWNSTAIRS_S1);
				sm.addString(clan.getName() + " has taken " + getName() + " Castle!");

				Broadcast.toAllOnlinePlayers(sm);

				clan.setHasCastle(getCastleId()); // Set has castle flag for new owner
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				ThreadPoolManager.getInstance()
						.scheduleGeneral(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
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

	// =========================================================
	// Property
	public final int getCastleId()
	{
		return this.castleId;
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
		return this.doors;
	}

	public final String getName()
	{
		return this.name;
	}

	public final int getOwnerId()
	{
		return this.ownerId;
	}

	public final Siege getSiege()
	{
		if (this.siege == null)
		{
			this.siege = new Siege(new Castle[]{this});
		}
		return this.siege;
	}

	public final Calendar getSiegeDate()
	{
		return this.siegeDate;
	}

	public boolean getIsTimeRegistrationOver()
	{
		return this.isTimeRegistrationOver;
	}

	public void setIsTimeRegistrationOver(boolean val)
	{
		this.isTimeRegistrationOver = val;
	}

	public Calendar getTimeRegistrationOverDate()
	{
		if (this.siegeTimeRegistrationEndDate == null)
		{
			this.siegeTimeRegistrationEndDate = Calendar.getInstance();
		}
		return this.siegeTimeRegistrationEndDate;
	}

	public final int getTaxPercent()
	{
		return this.taxPercent;
	}

	public final double getTaxRate()
	{
		return this.taxRate;
	}

	public final long getTreasury()
	{
		return this.treasury;
	}

	public final boolean getShowNpcCrest()
	{
		return this.showNpcCrest;
	}

	public final void setShowNpcCrest(boolean showNpcCrest)
	{
		if (this.showNpcCrest != showNpcCrest)
		{
			this.showNpcCrest = showNpcCrest;
			updateShowNpcCrest();
		}
	}

	public List<SeedProduction> getSeedProduction(int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? this.production : this.productionNext;
	}

	public List<CropProcure> getCropProcure(int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? this.procure : this.procureNext;
	}

	public void setSeedProduction(List<SeedProduction> seed, int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			this.production = seed;
		}
		else
		{
			this.productionNext = seed;
		}
	}

	public void setCropProcure(List<CropProcure> crop, int period)
	{
		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			this.procure = crop;
		}
		else
		{
			this.procureNext = crop;
		}
	}

	public SeedProduction getSeed(int seedId, int period)
	{
		for (SeedProduction seed : getSeedProduction(period))
		{
			if (seed.getId() == seedId)
			{
				return seed;
			}
		}
		return null;
	}

	public CropProcure getCrop(int cropId, int period)
	{
		for (CropProcure crop : getCropProcure(period))
		{
			if (crop.getId() == cropId)
			{
				return crop;
			}
		}
		return null;
	}

	public long getManorCost(int period)
	{
		List<CropProcure> procure;
		List<SeedProduction> production;

		if (period == CastleManorManager.PERIOD_CURRENT)
		{
			procure = this.procure;
			production = this.production;
		}
		else
		{
			procure = this.procureNext;
			production = this.productionNext;
		}

		long total = 0;
		if (production != null)
		{
			for (SeedProduction seed : production)
			{
				total += L2Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
			}
		}
		if (procure != null)
		{
			for (CropProcure crop : procure)
			{
				total += crop.getPrice() * crop.getStartAmount();
			}
		}
		return total;
	}

	//save manor production data
	public void saveSeedData()
	{
		Connection con = null;
		PreparedStatement statement;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION);
			statement.setInt(1, getCastleId());

			statement.execute();
			statement.close();

			if (this.production != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				String values[] = new String[this.production.size()];
				for (SeedProduction s : this.production)
				{
					values[count++] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," +
							s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_CURRENT + ")";
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}

			if (this.productionNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				String values[] = new String[this.productionNext.size()];
				for (SeedProduction s : this.productionNext)
				{
					values[count++] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," +
							s.getStartProduce() + "," + s.getPrice() + "," + CastleManorManager.PERIOD_NEXT + ")";
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.severe("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	//save manor production data for specified period
	public void saveSeedData(int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PRODUCTION_PERIOD);
			statement.setInt(1, getCastleId());
			statement.setInt(2, period);
			statement.execute();
			statement.close();

			List<SeedProduction> prod = null;
			prod = getSeedProduction(period);

			if (prod != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES ";
				String values[] = new String[prod.size()];
				for (SeedProduction s : prod)
				{
					values[count++] = "(" + getCastleId() + "," + s.getId() + "," + s.getCanProduce() + "," +
							s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.severe("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	//save crop procure data
	public void saveCropData()
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE);
			statement.setInt(1, getCastleId());
			statement.execute();
			statement.close();
			if (this.procure != null && this.procure.size() > 0)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				String values[] = new String[this.procure.size()];
				for (CropProcure cp : this.procure)
				{
					values[count++] =
							"(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() +
									"," + cp.getPrice() + "," + cp.getReward() + "," +
									CastleManorManager.PERIOD_CURRENT + ")";
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
			if (this.procureNext != null && this.procureNext.size() > 0)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				String values[] = new String[this.procureNext.size()];
				for (CropProcure cp : this.procureNext)
				{
					values[count++] =
							"(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() +
									"," + cp.getPrice() + "," + cp.getReward() + "," + CastleManorManager.PERIOD_NEXT +
									")";
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.severe("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	//	save crop procure data for specified period
	public void saveCropData(int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(CASTLE_MANOR_DELETE_PROCURE_PERIOD);
			statement.setInt(1, getCastleId());
			statement.setInt(2, period);
			statement.execute();
			statement.close();

			List<CropProcure> proc = null;
			proc = getCropProcure(period);

			if (proc != null && proc.size() > 0)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES ";
				String values[] = new String[proc.size()];

				for (CropProcure cp : proc)
				{
					values[count++] =
							"(" + getCastleId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() +
									"," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
				}
				if (values.length > 0)
				{
					query += values[0];
					for (int i = 1; i < values.length; i++)
					{
						query += "," + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.severe("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void updateCrop(int cropId, long amount, int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(CASTLE_UPDATE_CROP);
			statement.setLong(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, getCastleId());
			statement.setInt(4, period);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.severe("Error adding crop data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void updateSeed(int seedId, long amount, int period)
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(CASTLE_UPDATE_SEED);
			statement.setLong(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, getCastleId());
			statement.setInt(4, period);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.severe("Error adding seed production data for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean isNextPeriodApproved()
	{
		return this.isNextPeriodApproved;
	}

	public void setNextPeriodApproved(boolean val)
	{
		this.isNextPeriodApproved = val;
	}

	public void updateClansReputation()
	{
		if (this.formerOwner != null)
		{
			if (this.formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
			{
				int maxreward = Math.max(0, this.formerOwner.getReputationScore());
				this.formerOwner.takeReputationScore(Config.LOOSE_CASTLE_POINTS, true);
				L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
				if (owner != null)
				{
					owner.addReputationScore(Math.min(Config.TAKE_CASTLE_POINTS, maxreward), true);
				}
			}
			else
			{
				this.formerOwner.addReputationScore(Config.CASTLE_DEFENDED_POINTS, true);
			}
		}
		else
		{
			L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
			if (owner != null)
			{
				owner.addReputationScore(Config.TAKE_CASTLE_POINTS, true);
			}
		}
	}

	public void updateShowNpcCrest()
	{
		Connection con = null;
		PreparedStatement statement;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("UPDATE castle SET showNpcCrest = ? WHERE id = ?");
			statement.setString(1, String.valueOf(getShowNpcCrest()));
			statement.setInt(2, getCastleId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.severe("Error saving showNpcCrest for castle " + getName() + ": " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public ArrayList<L2Skill> getResidentialSkills()
	{
		return this.residentialSkills;
	}

	public void giveResidentialSkills(L2PcInstance player)
	{
		if (this.residentialSkills != null && !this.residentialSkills.isEmpty())
		{
			for (L2Skill sk : this.residentialSkills)
			{
				player.addSkill(sk, false);
			}
		}
	}

	public void removeResidentialSkills(L2PcInstance player)
	{
		if (this.residentialSkills != null && !this.residentialSkills.isEmpty())
		{
			for (L2Skill sk : this.residentialSkills)
			{
				player.removeSkill(sk, false, true);
			}
		}
	}

	/**
	 * Register Artefact to castle
	 *
	 * @param artefact
	 */
	public void registerArtefact(L2ArtefactInstance artefact)
	{
		if (Config.DEBUG)
		{
			Log.info("ArtefactId: " + artefact.getObjectId() + " is registered to " + getName() + " castle.");
		}
		this.artefacts.add(artefact);
	}

	public List<L2ArtefactInstance> getArtefacts()
	{
		return this.artefacts;
	}

	public void resetManor()
	{
		setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
		setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
		setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
		setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			saveCropData();
			saveSeedData();
		}
	}

	public int getBloodAlliance()
	{
		return this.bloodAlliance;
	}

	public void setBloodAlliance(int count)
	{
		this.bloodAlliance = count;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update castle set bloodAlliance = ? where id = ?");
			statement.setInt(1, this.bloodAlliance);
			statement.setInt(2, getCastleId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getTendency()
	{
		return this.tendency;
	}

	public void setTendency(int tendency)
	{
		//There is no need do nothing here if old tendency = new tendency
		if (this.tendency == tendency)
		{
			return;
		}

		this.tendency = tendency;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE castle SET tendency = ? WHERE id = ?");
			statement.setInt(1, this.tendency);
			statement.setInt(2, getCastleId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			player.sendPacket(new ExCastleTendency(getCastleId(), getTendency()));
		}

		setTaxPercent(this.tendency == 2 ? 30 : 0);

		//Manage tendency spawns
		manageTendencyChangeSpawns();
	}

	public void manageTendencyChangeSpawns()
	{
		if (this.tendency == 0)
		{
			return;
		}

		if (this.tendency == TENDENCY_LIGHT)
		{
			SpawnTable.getInstance().spawnSpecificTable(getName() + "_light_tendency");

			SpawnTable.getInstance().despawnSpecificTable(getName() + "_darkness_tendency");
		}
		else if (this.tendency == TENDENCY_DARKNESS)
		{
			SpawnTable.getInstance().spawnSpecificTable(getName() + "_darkness_tendency");

			SpawnTable.getInstance().despawnSpecificTable(getName() + "_light_tendency");
		}
	}
}
