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
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.itemcontainer.ClanWarehouse;
import l2server.gameserver.model.itemcontainer.ItemContainer;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Class For Castle Manor Manager Load manor data from DB Update/Reload/Delete
 * Handles all schedule for manor
 *
 * @author l3x
 */

public class CastleManorManager
{

	public static final int PERIOD_CURRENT = 0;
	public static final int PERIOD_NEXT = 1;

	private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
	private static final String CASTLE_MANOR_LOAD_PRODUCTION =
			"SELECT * FROM castle_manor_production WHERE castle_id=?";

	private static final int NEXT_PERIOD_APPROVE = Config.ALT_MANOR_APPROVE_TIME; // 6:00
	private static final int NEXT_PERIOD_APPROVE_MIN = Config.ALT_MANOR_APPROVE_MIN; //
	private static final int MANOR_REFRESH = Config.ALT_MANOR_REFRESH_TIME; // 20:00
	private static final int MANOR_REFRESH_MIN = Config.ALT_MANOR_REFRESH_MIN; //
	protected static final long MAINTENANCE_PERIOD = Config.ALT_MANOR_MAINTENANCE_PERIOD; // 6 mins

	private Calendar _manorRefresh;
	private Calendar _periodApprove;

	private boolean _underMaintenance;
	private boolean _disabled;

	protected ScheduledFuture<?> _scheduledManorRefresh;
	protected ScheduledFuture<?> _scheduledMaintenanceEnd;
	protected ScheduledFuture<?> _scheduledNextPeriodapprove;

	public static CastleManorManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public static class CropProcure
	{
		final int _cropId;
		long _buyResidual;
		final int _rewardType;
		final long _buy;
		final long _price;

		public CropProcure(int id)
		{
			_cropId = id;
			_buyResidual = 0;
			_rewardType = 0;
			_buy = 0;
			_price = 0;
		}

		public CropProcure(int id, long amount, int type, long buy, long price)
		{
			_cropId = id;
			_buyResidual = amount;
			_rewardType = type;
			_buy = buy;
			_price = price;
		}

		public int getReward()
		{
			return _rewardType;
		}

		public int getId()
		{
			return _cropId;
		}

		public long getAmount()
		{
			return _buyResidual;
		}

		public long getStartAmount()
		{
			return _buy;
		}

		public long getPrice()
		{
			return _price;
		}

		public void setAmount(long amount)
		{
			_buyResidual = amount;
		}
	}

	public static class SeedProduction
	{
		final int _seedId;
		long _residual;
		final long _price;
		final long _sales;

		public SeedProduction(int id)
		{
			_seedId = id;
			_residual = 0;
			_price = 0;
			_sales = 0;
		}

		public SeedProduction(int id, long amount, long price, long sales)
		{
			_seedId = id;
			_residual = amount;
			_price = price;
			_sales = sales;
		}

		public int getId()
		{
			return _seedId;
		}

		public long getCanProduce()
		{
			return _residual;
		}

		public long getPrice()
		{
			return _price;
		}

		public long getStartProduce()
		{
			return _sales;
		}

		public void setCanProduce(long amount)
		{
			_residual = amount;
		}
	}

	private CastleManorManager()
	{
		Log.info("Initializing CastleManorManager");
		load(); // load data from database
		init(); // schedule all manor related events
		_underMaintenance = false;
		_disabled = !Config.ALLOW_MANOR;

		boolean isApproved;
		if (_periodApprove.getTimeInMillis() > _manorRefresh.getTimeInMillis())
		// Next approve period already scheduled
		{
			isApproved = _manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
		}
		else
		{
			isApproved = _periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() &&
					_manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
		}

		for (Castle c : CastleManager.getInstance().getCastles())
		{
			c.setNextPeriodApproved(isApproved);
		}
	}

	private void load()
	{
		Connection con = null;
		ResultSet rs;
		try
		{
			// Get Connection
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statementProduction = con.prepareStatement(CASTLE_MANOR_LOAD_PRODUCTION);
			PreparedStatement statementProcure = con.prepareStatement(CASTLE_MANOR_LOAD_PROCURE);
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				ArrayList<SeedProduction> production = new ArrayList<>();
				ArrayList<SeedProduction> productionNext = new ArrayList<>();
				ArrayList<CropProcure> procure = new ArrayList<>();
				ArrayList<CropProcure> procureNext = new ArrayList<>();

				// restore seed production info
				statementProduction.setInt(1, castle.getCastleId());
				rs = statementProduction.executeQuery();
				statementProduction.clearParameters();
				while (rs.next())
				{
					int seedId = rs.getInt("seed_id");
					int canProduce = rs.getInt("can_produce");
					int startProduce = rs.getInt("start_produce");
					int price = rs.getInt("seed_price");
					int period = rs.getInt("period");
					if (period == PERIOD_CURRENT)
					{
						production.add(new SeedProduction(seedId, canProduce, price, startProduce));
					}
					else
					{
						productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
					}
				}
				rs.close();

				castle.setSeedProduction(production, PERIOD_CURRENT);
				castle.setSeedProduction(productionNext, PERIOD_NEXT);

				// restore procure info

				statementProcure.setInt(1, castle.getCastleId());
				rs = statementProcure.executeQuery();
				statementProcure.clearParameters();
				while (rs.next())
				{
					int cropId = rs.getInt("crop_id");
					int canBuy = rs.getInt("can_buy");
					int startBuy = rs.getInt("start_buy");
					int rewardType = rs.getInt("reward_type");
					int price = rs.getInt("price");
					int period = rs.getInt("period");
					if (period == PERIOD_CURRENT)
					{
						procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
					}
					else
					{
						procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price));
					}
				}
				rs.close();

				castle.setCropProcure(procure, PERIOD_CURRENT);
				castle.setCropProcure(procureNext, PERIOD_NEXT);

				if (!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty())
				{
					Log.fine(castle.getName() + ": Data loaded");
				}
			}
			statementProduction.close();
			statementProcure.close();
		}
		catch (Exception e)
		{
			Log.severe("Error restoring manor data: " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void init()
	{
		_manorRefresh = Calendar.getInstance();
		_manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
		_manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);

		_periodApprove = Calendar.getInstance();
		_periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
		_periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);

		updateManorRefresh();
		updatePeriodApprove();
	}

	public void updateManorRefresh()
	{
		Log.info("Manor System: Manor refresh updated");

		_scheduledManorRefresh = ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (!isDisabled())
			{
				setUnderMaintenance(true);
				Log.info("Manor System: Under maintenance mode started");

				_scheduledMaintenanceEnd = ThreadPoolManager.getInstance().scheduleGeneral(() ->
				{
					Log.info("Manor System: Next period started");
					setNextPeriod();
					try
					{
						save();
					}
					catch (Exception e)
					{
						Log.log(Level.WARNING, "Manor System: Failed to save manor data: " + e.getMessage(), e);
					}
					setUnderMaintenance(false);
				}, MAINTENANCE_PERIOD);
			}
			updateManorRefresh();
		}, getMillisToManorRefresh());
	}

	public void updatePeriodApprove()
	{
		Log.info("Manor System: Manor period approve updated");

		_scheduledNextPeriodapprove = ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (!isDisabled())
			{
				approveNextPeriod();
				Log.info("Manor System: Next period approved");
			}
			updatePeriodApprove();
		}, getMillisToNextPeriodApprove());
	}

	public long getMillisToManorRefresh()
	{
		// use safe interval 120s to prevent double run
		if (_manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 120000)
		{
			setNewManorRefresh();
		}

		Log.info("Manor System: New Schedule for manor refresh @ " + _manorRefresh.getTime());

		return _manorRefresh.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public void setNewManorRefresh()
	{
		_manorRefresh = Calendar.getInstance();
		_manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
		_manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);
		_manorRefresh.set(Calendar.SECOND, 0);
		_manorRefresh.add(Calendar.HOUR_OF_DAY, 24);
	}

	public long getMillisToNextPeriodApprove()
	{
		// use safe interval 120s to prevent double run
		if (_periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 120000)
		{
			setNewPeriodApprove();
		}

		Log.info("Manor System: New Schedule for period approve @ " + _periodApprove.getTime());

		return _periodApprove.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
	}

	public void setNewPeriodApprove()
	{
		_periodApprove = Calendar.getInstance();
		_periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
		_periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);
		_periodApprove.set(Calendar.SECOND, 0);
		_periodApprove.add(Calendar.HOUR_OF_DAY, 24);
	}

	public void setNextPeriod()
	{
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			if (c.getOwnerId() <= 0)
			{
				continue;
			}
			L2Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
			if (clan == null)
			{
				continue;
			}

			ItemContainer cwh = clan.getWarehouse();
			if (!(cwh instanceof ClanWarehouse))
			{
				Log.info("Can't get clan warehouse for clan " + ClanTable.getInstance().getClan(c.getOwnerId()));
				continue;
			}

			for (CropProcure crop : c.getCropProcure(PERIOD_CURRENT))
			{
				if (crop.getStartAmount() == 0)
				{
					continue;
				}
				// adding bought crops to clan warehouse
				if (crop.getStartAmount() - crop.getAmount() > 0)
				{
					long count = crop.getStartAmount() - crop.getAmount();
					count = count * 90 / 100;
					if (count < 1)
					{
						if (Rnd.nextInt(99) < 90)
						{
							count = 1;
						}
					}
					if (count > 0)
					{
						cwh.addItem("Manor", L2Manor.getInstance().getMatureCrop(crop.getId()), count, null, null);
					}
				}
				// reserved and not used money giving back to treasury
				if (crop.getAmount() > 0)
				{
					c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
				}
			}

			c.setSeedProduction(c.getSeedProduction(PERIOD_NEXT), PERIOD_CURRENT);
			c.setCropProcure(c.getCropProcure(PERIOD_NEXT), PERIOD_CURRENT);

			if (c.getTreasury() < c.getManorCost(PERIOD_CURRENT))
			{
				c.setSeedProduction(getNewSeedsList(c.getCastleId()), PERIOD_NEXT);
				c.setCropProcure(getNewCropsList(c.getCastleId()), PERIOD_NEXT);
			}
			else
			{
				ArrayList<SeedProduction> production = new ArrayList<>();
				for (SeedProduction s : c.getSeedProduction(PERIOD_CURRENT))
				{
					s.setCanProduce(s.getStartProduce());
					production.add(s);
				}
				c.setSeedProduction(production, PERIOD_NEXT);

				ArrayList<CropProcure> procure = new ArrayList<>();
				for (CropProcure cr : c.getCropProcure(PERIOD_CURRENT))
				{
					cr.setAmount(cr.getStartAmount());
					procure.add(cr);
				}
				c.setCropProcure(procure, PERIOD_NEXT);
			}
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				c.saveCropData();
				c.saveSeedData();
			}

			// Sending notification to a clan leader
			L2PcInstance clanLeader = null;
			clanLeader = L2World.getInstance().getPlayer(clan.getLeaderName());
			if (clanLeader != null)
			{
				clanLeader.sendPacket(
						SystemMessage.getSystemMessage(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED));
			}

			c.setNextPeriodApproved(false);
		}
	}

	public void approveNextPeriod()
	{
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			boolean notFunc = false;

			if (c.getOwnerId() <= 0)
			{ // Castle has no owner
				c.setCropProcure(new ArrayList<>(), PERIOD_NEXT);
				c.setSeedProduction(new ArrayList<>(), PERIOD_NEXT);
			}
			else if (c.getTreasury() < c.getManorCost(PERIOD_NEXT))
			{
				notFunc = true;
				Log.info("Manor for castle " + c.getName() + " disabled, not enough adena in treasury: " +
						c.getTreasury() + ", " + c.getManorCost(PERIOD_NEXT) + " required.");
				c.setSeedProduction(getNewSeedsList(c.getCastleId()), PERIOD_NEXT);
				c.setCropProcure(getNewCropsList(c.getCastleId()), PERIOD_NEXT);
			}
			else
			{
				ItemContainer cwh = ClanTable.getInstance().getClan(c.getOwnerId()).getWarehouse();
				if (!(cwh instanceof ClanWarehouse))
				{
					Log.info("Can't get clan warehouse for clan " + ClanTable.getInstance().getClan(c.getOwnerId()));
					continue;
				}
				int slots = 0;
				for (CropProcure crop : c.getCropProcure(PERIOD_NEXT))
				{
					if (crop.getStartAmount() > 0)
					{
						if (cwh.getItemByItemId(L2Manor.getInstance().getMatureCrop(crop.getId())) == null)
						{
							slots++;
						}
					}
				}
				if (!cwh.validateCapacity(slots))
				{
					notFunc = true;
					Log.info(
							"Manor for castle " + c.getName() + " disabled, not enough free slots in clan warehouse: " +
									(Config.WAREHOUSE_SLOTS_CLAN - cwh.getSize()) + ", but " + slots + " required.");
					c.setSeedProduction(getNewSeedsList(c.getCastleId()), PERIOD_NEXT);
					c.setCropProcure(getNewCropsList(c.getCastleId()), PERIOD_NEXT);
				}
			}
			c.setNextPeriodApproved(true);
			c.addToTreasuryNoTax(-1 * c.getManorCost(PERIOD_NEXT));

			if (notFunc)
			{
				L2Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
				L2PcInstance clanLeader = null;
				if (clan != null)
				{
					clanLeader = L2World.getInstance().getPlayer(clan.getLeaderId());
				}
				if (clanLeader != null)
				{
					clanLeader.sendPacket(SystemMessage.getSystemMessage(
							SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION));
				}
			}
		}
	}

	private ArrayList<SeedProduction> getNewSeedsList(int castleId)
	{
		ArrayList<SeedProduction> seeds = new ArrayList<>();
		ArrayList<Integer> seedsIds = L2Manor.getInstance().getSeedsForCastle(castleId);
		seeds.addAll(seedsIds.stream().map(SeedProduction::new).collect(Collectors.toList()));
		return seeds;
	}

	private ArrayList<CropProcure> getNewCropsList(int castleId)
	{
		ArrayList<CropProcure> crops = new ArrayList<>();
		ArrayList<Integer> cropsIds = L2Manor.getInstance().getCropsForCastle(castleId);
		crops.addAll(cropsIds.stream().map(CropProcure::new).collect(Collectors.toList()));

		return crops;
	}

	public boolean isUnderMaintenance()
	{
		return _underMaintenance;
	}

	public void setUnderMaintenance(boolean mode)
	{
		_underMaintenance = mode;
	}

	public boolean isDisabled()
	{
		return _disabled;
	}

	public void setDisabled(boolean mode)
	{
		_disabled = mode;
	}

	public SeedProduction getNewSeedProduction(int id, long amount, long price, long sales)
	{
		return new SeedProduction(id, amount, price, sales);
	}

	public CropProcure getNewCropProcure(int id, long amount, int type, long price, long buy)
	{
		return new CropProcure(id, amount, type, buy, price);
	}

	public void save()
	{
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			c.saveSeedData();
			c.saveCropData();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CastleManorManager _instance = new CastleManorManager();
	}
}
