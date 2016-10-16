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
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

//import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

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
		@Getter private String clanName;
		@Getter @Setter private long bid;
		@Getter private Calendar timeBid;

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
			return name;
		}

		public void setTimeBid(long timeBid)
		{
			this.timeBid.setTimeInMillis(timeBid);
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
		id = auctionId;
		load();
		startAutoTask();
	}

	public Auction(int itemId, L2Clan Clan, long delay, long bid, String name)
	{
		id = itemId;
		endDate = System.currentTimeMillis() + delay;
		this.itemId = itemId;
		itemName = name;
		itemType = "ClanHall";
		sellerId = Clan.getLeaderId();
		sellerName = Clan.getLeaderName();
		sellerClanName = Clan.getName();
		startingBid = bid;
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
				currentBid = rs.getLong("currentBid");
				endDate = rs.getLong("endDate");
				itemId = rs.getInt("itemId");
				itemName = rs.getString("itemName");
				itemObjectId = rs.getInt("itemObjectId");
				itemType = rs.getString("itemType");
				sellerId = rs.getInt("sellerId");
				sellerClanName = rs.getString("sellerClanName");
				sellerName = rs.getString("sellerName");
				startingBid = rs.getLong("startingBid");
				if (Config.CH_BID_PRICE_DIVIDER > 0)
				{
					startingBid /= Config.CH_BID_PRICE_DIVIDER;
				}

				if (Config.isServer(Config.TENKAI))
				{
					if (Config.CH_BID_ITEMID == 57)
					{
						startingBid *= Config.RATE_DROP_ITEMS_ID.get(57);
					}
					else
					{
						startingBid /= 2000000;
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
		highestBidderId = 0;
		highestBidderName = "";
		highestBidderMaxBid = 0;

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
					highestBidderId = rs.getInt("bidderId");
					highestBidderName = rs.getString("bidderName");
					highestBidderMaxBid = rs.getLong("maxBid");
				}
				bidders.put(rs.getInt("bidderId"),
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
		if (endDate <= currentTime)
		{
			endDate = currentTime + 7 * 24 * 60 * 60 * 1000;
			saveAuctionDate();
		}
		else
		{
			taskDelay = endDate - currentTime;
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
			statement.setLong(1, endDate);
			statement.setInt(2, id);
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
				bidder.getClan().setAuctionBiddedAt(id, true);
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
				if (L2World.getInstance().getPlayer(highestBidderName) != null)
				{
					L2World.getInstance().getPlayer(highestBidderName).sendMessage("You have been out bidded");
				}
			}
			highestBidderId = bidder.getClanId();
			highestBidderMaxBid = bid;
			highestBidderName = bidder.getClan().getLeaderName();
			if (bidders.get(highestBidderId) == null)
			{
				bidders.put(highestBidderId, new Bidder(highestBidderName, bidder.getClan().getName(), bid,
						Calendar.getInstance().getTimeInMillis()));
			}
			else
			{
				bidders.get(highestBidderId).setBid(bid);
				bidders.get(highestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
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
		for (Bidder b : bidders.values())
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
		bidders.clear();
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
			statement.setInt(1, itemId);
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
			if (highestBidderId == 0 && sellerId == 0)
			{
				startAutoTask();
				return;
			}
			if (highestBidderId == 0 && sellerId > 0)
			{
				/* If seller haven't sell ClanHall, auction removed,
				   THIS MUST BE CONFIRMED */
				int aucId = ClanHallAuctionManager.getInstance().getAuctionIndex(id);
				ClanHallAuctionManager.getInstance().getAuctions().remove(aucId);
				return;
			}
			if (sellerId > 0)
			{
				returnItem(sellerClanName, highestBidderMaxBid, true);
				returnItem(sellerClanName, ClanHallManager.getInstance().getClanHallById(itemId).getLease(), false);
			}
			deleteAuctionFromDB();
			L2Clan clan = ClanTable.getInstance().getClanByName(bidders.get(highestBidderId).getClanName());
			bidders.remove(highestBidderId);
			if (clan != null)
			{
				clan.setAuctionBiddedAt(0, true);
				ClanHallManager.getInstance().setOwner(itemId, clan);
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
		returnItem(bidders.get(bidder).getClanName(), bidders.get(bidder).getBid(), true);
		ClanTable.getInstance().getClanByName(bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
		bidders.clear();
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
			statement.setInt(2, sellerId);
			statement.setString(3, sellerName);
			statement.setString(4, sellerClanName);
			statement.setString(5, itemType);
			statement.setInt(6, itemId);
			statement.setInt(7, itemObjectId);
			statement.setString(8, itemName);
			statement.setLong(9, itemQuantity);
			statement.setLong(10, startingBid);
			statement.setLong(11, currentBid);
			statement.setLong(12, endDate);
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
		return id;
	}

	public final long getCurrentBid()
	{
		return currentBid;
	}

	public final long getEndDate()
	{
		return endDate;
	}

	public final int getHighestBidderId()
	{
		return highestBidderId;
	}

	public final String getHighestBidderName()
	{
		return highestBidderName;
	}

	public final long getHighestBidderMaxBid()
	{
		return highestBidderMaxBid;
	}

	public final int getItemId()
	{
		return itemId;
	}

	public final String getItemName()
	{
		return itemName;
	}

	public final int getItemObjectId()
	{
		return itemObjectId;
	}

	public final long getItemQuantity()
	{
		return itemQuantity;
	}

	public final String getItemType()
	{
		return itemType;
	}

	public final int getSellerId()
	{
		return sellerId;
	}

	public final String getSellerName()
	{
		return sellerName;
	}

	public final String getSellerClanName()
	{
		return sellerClanName;
	}

	public final long getStartingBid()
	{
		return startingBid;
	}

	public final Map<Integer, Bidder> getBidders()
	{
		return bidders;
	}
}
