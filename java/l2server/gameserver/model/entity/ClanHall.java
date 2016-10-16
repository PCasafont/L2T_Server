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

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.instancemanager.ClanHallAuctionManager;
import l2server.gameserver.instancemanager.ClanHallManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.type.L2ClanHallZone;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

public class ClanHall
{

	private int clanHallId;
	private List<L2DoorInstance> doors;
	private String name;
	private int ownerId;
	private long lease;
	private String desc;
	private String location;
	protected long paidUntil;
	@Getter @Setter private L2ClanHallZone zone;
	private int grade;
	protected final int chRate = 604800000;
	protected boolean isFree = true;
	private Map<Integer, ClanHallFunction> functions;
	protected boolean paid;

	/**
	 * Clan Hall Functions
	 */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;

	public class ClanHallFunction
	{
		@Getter private int type;
		@Getter @Setter private int lvl;
		protected int fee;
		protected int tempFee;
		@Getter private long rate;
		private long endDate;
		protected boolean inDebt;
		public boolean cwh;
		// first activating clanhall function is payed from player inventory, any others from clan warehouse

		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time, boolean cwh)
		{
			this.type = type;
			this.lvl = lvl;
			fee = lease;
			tempFee = tempLease;
			this.rate = rate;
			endDate = time;
			initializeTask(cwh);
		}



		public int getLease()
		{
			return fee;
		}


		public long getEndTime()
		{
			return endDate;
		}


		public void setLease(int lease)
		{
			fee = lease;
		}

		public void setEndTime(long time)
		{
			endDate = time;
		}

		private void initializeTask(boolean cwh)
		{
			if (isFree)
			{
				return;
			}
			long currentTime = System.currentTimeMillis();
			if (endDate > currentTime)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(cwh), endDate - currentTime);
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
				ClanHallFunction.this.cwh = cwh;
			}

			@Override
			public void run()
			{
				try
				{
					if (isFree)
					{
						return;
					}
					if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= fee || !cwh)
					{
						int fee = ClanHallFunction.this.fee;
						if (getEndTime() == -1)
						{
							fee = tempFee;
						}

						setEndTime(System.currentTimeMillis() + getRate());
						dbSave();
						if (cwh)
						{
							ClanTable.getInstance().getClan(getOwnerId()).getWarehouse()
									.destroyItemByItemId("CH_function_fee", 57, fee, null, null);
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
						"REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
				statement.setInt(1, getId());
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
						"Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " +
								e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public ClanHall(int clanHallId, String name, int ownerId, long lease, String desc, String location, long paidUntil, int Grade, boolean paid)
	{
		this.clanHallId = clanHallId;
		this.name = name;
		this.ownerId = ownerId;
		if (Config.DEBUG)
		{
			Log.warning("Init Owner : " + this.ownerId);
		}
		this.lease = lease;
		this.desc = desc;
		this.location = location;
		this.paidUntil = paidUntil;
		grade = Grade;
		this.paid = paid;
		functions = new HashMap<>();

		if (ownerId != 0)
		{
			isFree = false;
			initialyzeTask(false);
			loadFunctions();
		}
	}

	/**
	 * Return if clanHall is paid or not
	 */
	public final boolean getPaid()
	{
		return paid;
	}

	/**
	 * Return Id Of Clan hall
	 */
	public final int getId()
	{
		return clanHallId;
	}

	/**
	 * Return name
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Return OwnerId
	 */
	public final int getOwnerId()
	{
		return ownerId;
	}

	/**
	 * Return lease
	 */
	public final long getLease()
	{
		return lease;
	}

	/**
	 * Return Desc
	 */
	public final String getDesc()
	{
		return desc;
	}

	/**
	 * Return Location
	 */
	public final String getLocation()
	{
		return location;
	}

	/**
	 * Return PaidUntil
	 */
	public final long getPaidUntil()
	{
		return paidUntil;
	}

	/**
	 * Return Grade
	 */
	public final int getGrade()
	{
		return grade;
	}

	/**
	 * Return all DoorInstance
	 */
	public final List<L2DoorInstance> getDoors()
	{
		if (doors == null)
		{
			doors = new ArrayList<>();
		}
		return doors;
	}

	/**
	 * Return Door
	 */
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

	/**
	 * Return function with id
	 */
	public ClanHallFunction getFunction(int type)
	{
		if (functions.get(type) != null)
		{
			return functions.get(type);
		}
		return null;
	}

	/**
	 * Free this clan hall
	 */
	public void free()
	{
		ownerId = 0;
		isFree = true;
		Set<Integer> functionIds = new HashSet<>(functions.keySet());
		for (int funcId : functionIds)
		{
			removeFunction(funcId);
		}
		functions.clear();
		paidUntil = 0;
		paid = false;
		updateDb();
	}

	/**
	 * Set owner if clan hall is free
	 */
	public void setOwner(L2Clan clan)
	{
		// Verify that this ClanHall is Free and Clan isn't null
		if (ownerId > 0 || clan == null)
		{
			return;
		}
		ownerId = clan.getClanId();
		isFree = false;
		paidUntil = System.currentTimeMillis();
		initialyzeTask(true);
		// Annonce to Online member new ClanHall
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}

	/**
	 * Open or Close Door
	 */
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoor(doorId, open);
		}
	}

	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}

	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
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

	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if (activeChar != null && activeChar.getClanId() == getOwnerId())
		{
			openCloseDoors(open);
		}
	}

	public void openCloseDoors(boolean open)
	{
		for (L2DoorInstance door : getDoors())
		{
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
	}

	/**
	 * Banish Foreigner
	 */
	public void banishForeigners()
	{
		zone.banishForeigners(getOwnerId());
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
			statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();
			while (rs.next())
			{
				functions.put(rs.getInt("type"),
						new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0,
								rs.getLong("rate"), rs.getLong("endTime"), true));
			}
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
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
		functions.remove(functionType);
		Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
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
					"Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, boolean addNew) Owner : " +
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
			functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0, false));
		}
		else
		{
			if (lvl == 0 && lease == 0)
			{
				removeFunction(type);
			}
			else
			{
				int diffLease = lease - functions.get(type).getLease();
				if (Config.DEBUG)
				{
					Log.warning("Called ClanHall.updateFunctions diffLease : " + diffLease);
				}
				if (diffLease > 0)
				{
					functions.remove(type);
					functions.put(type, new ClanHallFunction(type, lvl, lease, 0, rate, -1, false));
				}
				else
				{
					functions.get(type).setLease(lease);
					functions.get(type).setLvl(lvl);
					functions.get(type).dbSave();
				}
			}
		}
		return true;
	}

	/**
	 * Update DB
	 */
	public void updateDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=? WHERE id=?");
			statement.setInt(1, ownerId);
			statement.setLong(2, paidUntil);
			statement.setInt(3, paid ? 1 : 0);
			statement.setInt(4, clanHallId);
			statement.execute();
			statement.close();
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

	/**
	 * Initialize Fee Task
	 */
	private void initialyzeTask(boolean forced)
	{
		long currentTime = System.currentTimeMillis();
		if (paidUntil > currentTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil - currentTime);
		}
		else if (!paid && !forced)
		{
			if (System.currentTimeMillis() + 1000 * 60 * 60 * 24 <= paidUntil + chRate)
			{
				ThreadPoolManager.getInstance()
						.scheduleGeneral(new FeeTask(), System.currentTimeMillis() + 1000 * 60 * 60 * 24);
			}
			else
			{
				ThreadPoolManager.getInstance()
						.scheduleGeneral(new FeeTask(), paidUntil + chRate - System.currentTimeMillis());
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
		}
	}

	/**
	 * Fee Task
	 */
	private class FeeTask implements Runnable
	{
		public FeeTask()
		{
		}

		@Override
		public void run()
		{
			try
			{
				long time = System.currentTimeMillis();

				if (isFree)
				{
					return;
				}

				if (paidUntil > time)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil - time);
					return;
				}

				L2Clan Clan = ClanTable.getInstance().getClan(getOwnerId());
				if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse()
						.getItemByItemId(Config.CH_BID_ITEMID) != null &&
						ClanTable.getInstance().getClan(getOwnerId()).getWarehouse()
								.getItemByItemId(Config.CH_BID_ITEMID).getCount() >= getLease())
				{
					if (paidUntil != 0)
					{
						while (paidUntil <= time)
						{
							paidUntil += chRate;
						}
					}
					else
					{
						paidUntil = time + chRate;
					}
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse()
							.destroyItemByItemId("CH_rental_fee", Config.CH_BID_ITEMID, getLease(), null, null);
					if (Config.DEBUG)
					{
						Log.warning("deducted " + getLease() + " adena from " + getName() +
								" owner's cwh for ClanHall _paidUntil: " + paidUntil);
					}
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), paidUntil - time);
					paid = true;
					updateDb();
				}
				else
				{
					paid = false;
					if (time > paidUntil + chRate)
					{
						if (ClanHallManager.getInstance().loaded())
						{
							ClanHallAuctionManager.getInstance().initNPC(getId());
							ClanHallManager.getInstance().setFree(getId());
							Clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(
									SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
						}
						else
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 3000);
						}
					}
					else
					{
						updateDb();
						SystemMessage sm = SystemMessage.getSystemMessage(
								SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
						sm.addItemNumber(getLease());
						Clan.broadcastToOnlineMembers(sm);
						if (time + 1000 * 60 * 60 * 24 <= paidUntil + chRate)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), time + 1000 * 60 * 60 * 24);
						}
						else
						{
							ThreadPoolManager.getInstance()
									.scheduleGeneral(new FeeTask(), paidUntil + chRate - time);
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}
}
