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

//import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.instancemanager.ClanHallAuctionManager;
import l2server.gameserver.instancemanager.ClanHallManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

public class Auction
{
	private int id = 0;
	private long endDate;
	private int highestBidderId = 0;
	private String highestBidderName = "";
	private long highestBidderMaxBid = 0;
	private int itemId = 0;
	private String itemName = "";
	private int itemObjectId = 0;
	private long itemQuantity = 0;
	private String itemType = "";
	private int sellerId = 0;
	private String sellerClanName = "";
	private String sellerName = "";
	private long currentBid = 0;
	private long startingBid = 0;

	private Map<Integer, Bidder> bidders = new HashMap<>();

	private static final String[] ItemTypeName = {"ClanHall"};

	public enum ItemTypeEnum
	{
		ClanHall
	}

	public static class Bidder
	{
		private String name; //TODO replace with objid
		private String clanName;
		private long bid;
		private Calendar timeBid;

		public Bidder(String name, String clanName, long bid, long timeBid)
		{
			this.name = name;
			this.clanName = clanName;
			this.bid = bid;
			this.timeBid = Calendar.getInstance();
			this.timeBid.setTimeInMillis(timeBid);
		}

		public String getName()
		{
			return this.name;
		}

		public String getClanName()
		{
			return this.clanName;
		}

		public long getBid()
		{
			return this.bid;
		}

		public Calendar getTimeBid()
		{
			return this.timeBid;
		}

		public void setTimeBid(long timeBid)
		{
			this.timeBid.setTimeInMillis(timeBid);
		}

		public void setBid(long bid)
		{
			this.bid = bid;
		}
	}

	/**
	 * Task Sheduler for endAuction
	 */
	public class AutoEndTask implements Runnable
	{
		public AutoEndTask()
		{
		}

		@Override
		public void run()
		{
			try
			{
				endAuction();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	/**
	 * Constructor
	 */
	public Auction(int auctionId)
	{
		this.id = auctionId;
		load();
		startAutoTask();
	}

	public Auction(int itemId, L2Clan Clan, long delay, long bid, String name)
	{
		this.id = itemId;
		this.endDate = System.currentTimeMillis() + delay;
		this.itemId = itemId;
		this.itemName = name;
		this.itemType = "ClanHall";
		this.sellerId = Clan.getLeaderId();
		this.sellerName = Clan.getLeaderName();
		this.sellerClanName = Clan.getName();
		this.startingBid = bid;
	}

	/**
	 * Load auctions
	 */
	private void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT * FROM clanhall_auction WHERE id = ?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();

			while (rs.next())
			{
				this.currentBid = rs.getLong("currentBid");
				this.endDate = rs.getLong("endDate");
				this.itemId = rs.getInt("itemId");
				this.itemName = rs.getString("itemName");
				this.itemObjectId = rs.getInt("itemObjectId");
				this.itemType = rs.getString("itemType");
				this.sellerId = rs.getInt("sellerId");
				this.sellerClanName = rs.getString("sellerClanName");
				this.sellerName = rs.getString("sellerName");
				this.startingBid = rs.getLong("startingBid");
				if (Config.CH_BID_PRICE_DIVIDER > 0)
				{
					this.startingBid /= Config.CH_BID_PRICE_DIVIDER;
				}

				if (Config.isServer(Config.TENKAI))
				{
					if (Config.CH_BID_ITEMID == 57)
					{
						this.startingBid *= Config.RATE_DROP_ITEMS_ID.get(57);
					}
					else
					{
						this.startingBid /= 2000000;
					}
				}
			}
			statement.close();
			loadBid();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: Auction.load(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Load bidders
	 **/
	private void loadBid()
	{
		this.highestBidderId = 0;
		this.highestBidderName = "";
		this.highestBidderMaxBid = 0;

		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(
					"SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM clanhall_auction_bid WHERE auctionId = ? ORDER BY maxBid DESC");
			statement.setInt(1, getId());
			rs = statement.executeQuery();

			while (rs.next())
			{
				if (rs.isFirst())
				{
					this.highestBidderId = rs.getInt("bidderId");
					this.highestBidderName = rs.getString("bidderName");
					this.highestBidderMaxBid = rs.getLong("maxBid");
				}
				this.bidders.put(rs.getInt("bidderId"),
						new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getLong("maxBid"),
								rs.getLong("time_bid")));
			}

			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: Auction.loadBid(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Task Manage
	 */
	private void startAutoTask()
	{
		long currentTime = System.currentTimeMillis();
		long taskDelay = 0;
		if (this.endDate <= currentTime)
		{
			this.endDate = currentTime + 7 * 24 * 60 * 60 * 1000;
			saveAuctionDate();
		}
		else
		{
			taskDelay = this.endDate - currentTime;
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), taskDelay);
	}

	public static String getItemTypeName(ItemTypeEnum value)
	{
		return ItemTypeName[value.ordinal()];
	}

	/**
	 * Save Auction Data End
	 */
	private void saveAuctionDate()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clanhall_auction SET endDate = ? WHERE id = ?");
			statement.setLong(1, this.endDate);
			statement.setInt(2, this.id);
			statement.execute();

			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Set a bid
	 */
	public synchronized void setBid(L2PcInstance bidder, long bid)
	{
		long requiredAdena = bid;
		if (getHighestBidderName().equals(bidder.getClan().getLeaderName()))
		{
			requiredAdena = bid - getHighestBidderMaxBid();
		}
		if (getHighestBidderId() > 0 && bid > getHighestBidderMaxBid() ||
				getHighestBidderId() == 0 && bid >= getStartingBid())
		{
			if (takeItem(bidder, requiredAdena))
			{
				updateInDB(bidder, bid);
				bidder.getClan().setAuctionBiddedAt(this.id, true);
				return;
			}
		}
		if (bid < getStartingBid() || bid <= getHighestBidderMaxBid())
		{
			bidder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_PRICE_MUST_BE_HIGHER));
		}
	}

	/**
	 * Return Item in WHC
	 */
	private void returnItem(String clanName, long quantity, boolean penalty)
	{
		if (penalty)
		{
			quantity *= 0.9; //take 10% tax fee if needed
		}

		// avoid overflow on return
		L2Clan clan = ClanTable.getInstance().getClanByName(clanName);
		if (clan == null)
		{
			return;
		}

		final long limit = MAX_ADENA - (clan.getWarehouse().getItemByItemId(Config.CH_BID_ITEMID) != null ?
				clan.getWarehouse().getItemByItemId(Config.CH_BID_ITEMID).getCount() : 0);
		quantity = Math.min(quantity, limit);

		clan.getWarehouse().addItem("Outbidded", Config.CH_BID_ITEMID, quantity, null, null);
	}

	/**
	 * Take Item in WHC
	 */
	private boolean takeItem(L2PcInstance bidder, long quantity)
	{
		if (bidder.getClan() != null && bidder.getClan().getWarehouse().getItemByItemId(Config.CH_BID_ITEMID) != null &&
				bidder.getClan().getWarehouse().getItemByItemId(Config.CH_BID_ITEMID).getCount() >= quantity)
		{
			bidder.getClan().getWarehouse().destroyItemByItemId("Buy", Config.CH_BID_ITEMID, quantity, bidder, bidder);
			return true;
		}
		if (Config.CH_BID_ITEMID == 57)
		{
			bidder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH));
		}
		else
		{
			bidder.sendMessage(
					"There are not enough " + ItemTable.getInstance().getTemplate(Config.CH_BID_ITEMID).getName() +
							"s in the clan hall warehouse.");
		}
		return false;
	}

	/**
	 * Update auction in DB
	 */
	private void updateInDB(L2PcInstance bidder, long bid)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			if (getBidders().get(bidder.getClanId()) != null)
			{
				statement = con.prepareStatement(
						"UPDATE clanhall_auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
				statement.setInt(1, bidder.getClanId());
				statement.setString(2, bidder.getClan().getLeaderName());
				statement.setLong(3, bid);
				statement.setLong(4, System.currentTimeMillis());
				statement.setInt(5, getId());
				statement.setInt(6, bidder.getClanId());
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement(
						"INSERT INTO clanhall_auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, IdFactory.getInstance().getNextId());
				statement.setInt(2, getId());
				statement.setInt(3, bidder.getClanId());
				statement.setString(4, bidder.getName());
				statement.setLong(5, bid);
				statement.setString(6, bidder.getClan().getName());
				statement.setLong(7, System.currentTimeMillis());
				statement.execute();
				statement.close();
				if (L2World.getInstance().getPlayer(this.highestBidderName) != null)
				{
					L2World.getInstance().getPlayer(this.highestBidderName).sendMessage("You have been out bidded");
				}
			}
			this.highestBidderId = bidder.getClanId();
			this.highestBidderMaxBid = bid;
			this.highestBidderName = bidder.getClan().getLeaderName();
			if (this.bidders.get(this.highestBidderId) == null)
			{
				this.bidders.put(this.highestBidderId, new Bidder(this.highestBidderName, bidder.getClan().getName(), bid,
						Calendar.getInstance().getTimeInMillis()));
			}
			else
			{
				this.bidders.get(this.highestBidderId).setBid(bid);
				this.bidders.get(this.highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
			}
			bidder.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_IN_CLANHALL_AUCTION));
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Auction.updateInDB(L2PcInstance bidder, int bid): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Remove bids
	 */
	private void removeBids()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM clanhall_auction_bid WHERE auctionId=?");
			statement.setInt(1, getId());
			statement.execute();

			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		for (Bidder b : this.bidders.values())
		{
			if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() == 0)
			{
				returnItem(b.getClanName(), b.getBid(), true); // 10 % tax
			}
			else
			{
				if (L2World.getInstance().getPlayer(b.getName()) != null)
				{
					L2World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
				}
			}
			ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
		}
		this.bidders.clear();
	}

	/**
	 * Remove auctions
	 */
	public void deleteAuctionFromDB()
	{
		ClanHallAuctionManager.getInstance().getAuctions().remove(this);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM clanhall_auction WHERE itemId=?");
			statement.setInt(1, this.itemId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * End of auction
	 */
	public void endAuction()
	{
		if (ClanHallManager.getInstance().loaded())
		{
			if (this.highestBidderId == 0 && this.sellerId == 0)
			{
				startAutoTask();
				return;
			}
			if (this.highestBidderId == 0 && this.sellerId > 0)
			{
				/* If seller haven't sell ClanHall, auction removed,
                   THIS MUST BE CONFIRMED */
				int aucId = ClanHallAuctionManager.getInstance().getAuctionIndex(this.id);
				ClanHallAuctionManager.getInstance().getAuctions().remove(aucId);
				return;
			}
			if (this.sellerId > 0)
			{
				returnItem(this.sellerClanName, this.highestBidderMaxBid, true);
				returnItem(this.sellerClanName, ClanHallManager.getInstance().getClanHallById(this.itemId).getLease(), false);
			}
			deleteAuctionFromDB();
			L2Clan clan = ClanTable.getInstance().getClanByName(this.bidders.get(this.highestBidderId).getClanName());
			this.bidders.remove(this.highestBidderId);
			if (clan != null)
			{
				clan.setAuctionBiddedAt(0, true);
				ClanHallManager.getInstance().setOwner(this.itemId, clan);
			}
			removeBids();
		}
		else
		{
            /* Task waiting ClanHallManager is loaded every 3s */
			ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 3000);
		}
	}

	/**
	 * Cancel bid
	 */
	public synchronized void cancelBid(int bidder)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM clanhall_auction_bid WHERE auctionId=? AND bidderId=?");
			statement.setInt(1, getId());
			statement.setInt(2, bidder);
			statement.execute();

			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		returnItem(this.bidders.get(bidder).getClanName(), this.bidders.get(bidder).getBid(), true);
		ClanTable.getInstance().getClanByName(this.bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
		this.bidders.clear();
		loadBid();
	}

	/**
	 * Cancel auction
	 */
	public void cancelAuction()
	{
		deleteAuctionFromDB();
		removeBids();
	}

	/**
	 * Confirm an auction
	 */
	public void confirmAuction()
	{
		ClanHallAuctionManager.getInstance().getAuctions().add(this);
		Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(
					"INSERT INTO clanhall_auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getId());
			statement.setInt(2, this.sellerId);
			statement.setString(3, this.sellerName);
			statement.setString(4, this.sellerClanName);
			statement.setString(5, this.itemType);
			statement.setInt(6, this.itemId);
			statement.setInt(7, this.itemObjectId);
			statement.setString(8, this.itemName);
			statement.setLong(9, this.itemQuantity);
			statement.setLong(10, this.startingBid);
			statement.setLong(11, this.currentBid);
			statement.setLong(12, this.endDate);
			statement.execute();
			statement.close();
			loadBid();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Auction.load(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Get var auction
	 */
	public final int getId()
	{
		return this.id;
	}

	public final long getCurrentBid()
	{
		return this.currentBid;
	}

	public final long getEndDate()
	{
		return this.endDate;
	}

	public final int getHighestBidderId()
	{
		return this.highestBidderId;
	}

	public final String getHighestBidderName()
	{
		return this.highestBidderName;
	}

	public final long getHighestBidderMaxBid()
	{
		return this.highestBidderMaxBid;
	}

	public final int getItemId()
	{
		return this.itemId;
	}

	public final String getItemName()
	{
		return this.itemName;
	}

	public final int getItemObjectId()
	{
		return this.itemObjectId;
	}

	public final long getItemQuantity()
	{
		return this.itemQuantity;
	}

	public final String getItemType()
	{
		return this.itemType;
	}

	public final int getSellerId()
	{
		return this.sellerId;
	}

	public final String getSellerName()
	{
		return this.sellerName;
	}

	public final String getSellerClanName()
	{
		return this.sellerClanName;
	}

	public final long getStartingBid()
	{
		return this.startingBid;
	}

	public final Map<Integer, Bidder> getBidders()
	{
		return this.bidders;
	}
}
