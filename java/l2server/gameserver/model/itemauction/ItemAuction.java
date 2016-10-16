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

package l2server.gameserver.model.itemauction;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.ItemInfo;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author Forsaiken
 */
public final class ItemAuction
{
	private static final long ENDING_TIME_EXTEND_5 = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	private static final long ENDING_TIME_EXTEND_3 = TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES);

	private final int auctionId;
	private final int instanceId;
	private final long startingTime;
	private volatile long endingTime;
	private final AuctionItem auctionItem;
	private final ArrayList<ItemAuctionBid> auctionBids;
	private final Object auctionStateLock;

	private volatile ItemAuctionState auctionState;
	private volatile ItemAuctionExtendState scheduledAuctionEndingExtendState;
	private volatile ItemAuctionExtendState auctionEndingExtendState;

	private final ItemInfo itemInfo;

	private ItemAuctionBid highestBid;
	private int lastBidPlayerObjId;

	public ItemAuction(final int auctionId, final int instanceId, final long startingTime, final long endingTime, final AuctionItem auctionItem)
	{
		this(auctionId, instanceId, startingTime, endingTime, auctionItem, new ArrayList<>(), ItemAuctionState.CREATED);
	}

	public ItemAuction(final int auctionId, final int instanceId, final long startingTime, final long endingTime, final AuctionItem auctionItem, final ArrayList<ItemAuctionBid> auctionBids, final ItemAuctionState auctionState)
	{
		this.auctionId = auctionId;
		this.instanceId = instanceId;
		this.startingTime = startingTime;
		this.endingTime = endingTime;
		this.auctionItem = auctionItem;
		this.auctionBids = auctionBids;
		this.auctionState = auctionState;
		auctionStateLock = new Object();
		scheduledAuctionEndingExtendState = ItemAuctionExtendState.INITIAL;
		auctionEndingExtendState = ItemAuctionExtendState.INITIAL;

		final L2ItemInstance item = this.auctionItem.createNewItemInstance();
		itemInfo = new ItemInfo(item);
		L2World.getInstance().removeObject(item);

		for (final ItemAuctionBid bid : this.auctionBids)
		{
			if (highestBid == null || highestBid.getLastBid() < bid.getLastBid())
			{
				highestBid = bid;
			}
		}
	}

	public final ItemAuctionState getAuctionState()
	{
		final ItemAuctionState auctionState;

		synchronized (auctionStateLock)
		{
			auctionState = this.auctionState;
		}

		return auctionState;
	}

	public final boolean setAuctionState(final ItemAuctionState expected, final ItemAuctionState wanted)
	{
		synchronized (auctionStateLock)
		{
			if (auctionState != expected)
			{
				return false;
			}

			auctionState = wanted;
			storeMe();
			return true;
		}
	}

	public final int getAuctionId()
	{
		return auctionId;
	}

	public final int getInstanceId()
	{
		return instanceId;
	}

	public final ItemInfo getItemInfo()
	{
		return itemInfo;
	}

	public final L2ItemInstance createNewItemInstance()
	{
		return auctionItem.createNewItemInstance();
	}

	public final long getAuctionInitBid()
	{
		return auctionItem.getAuctionInitBid();
	}

	public final ItemAuctionBid getHighestBid()
	{
		return highestBid;
	}

	public final ItemAuctionExtendState getAuctionEndingExtendState()
	{
		return auctionEndingExtendState;
	}

	public final ItemAuctionExtendState getScheduledAuctionEndingExtendState()
	{
		return scheduledAuctionEndingExtendState;
	}

	public final void setScheduledAuctionEndingExtendState(ItemAuctionExtendState state)
	{
		scheduledAuctionEndingExtendState = state;
	}

	public final long getStartingTime()
	{
		return startingTime;
	}

	public final long getEndingTime()
	{
		return endingTime;
	}

	public final long getStartingTimeRemaining()
	{
		return Math.max(getEndingTime() - System.currentTimeMillis(), 0L);
	}

	public final long getFinishingTimeRemaining()
	{
		return Math.max(getEndingTime() - System.currentTimeMillis(), 0L);
	}

	public final void storeMe()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"INSERT INTO item_auction (auctionId,instanceId,auctionItemId,startingTime,endingTime,auctionStateId) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE auctionStateId=?");
			statement.setInt(1, auctionId);
			statement.setInt(2, instanceId);
			statement.setInt(3, auctionItem.getAuctionItemId());
			statement.setLong(4, startingTime);
			statement.setLong(5, endingTime);
			statement.setByte(6, auctionState.getStateId());
			statement.setByte(7, auctionState.getStateId());
			statement.execute();
			statement.close();
		}
		catch (final SQLException e)
		{
			Log.log(Level.WARNING, "", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final int getAndSetLastBidPlayerObjectId(final int playerObjId)
	{
		final int lastBid = lastBidPlayerObjId;
		lastBidPlayerObjId = playerObjId;
		return lastBid;
	}

	private void updatePlayerBid(final ItemAuctionBid bid, final boolean delete)
	{
		updatePlayerBidInternal(bid, delete);
	}

	final void updatePlayerBidInternal(final ItemAuctionBid bid, final boolean delete)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement;

			if (delete)
			{
				statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=? AND playerObjId=?");
				statement.setInt(1, auctionId);
				statement.setInt(2, bid.getPlayerObjId());
			}
			else
			{
				statement = con.prepareStatement(
						"INSERT INTO item_auction_bid (auctionId,playerObjId,playerBid) VALUES (?,?,?) ON DUPLICATE KEY UPDATE playerBid=?");
				statement.setInt(1, auctionId);
				statement.setInt(2, bid.getPlayerObjId());
				statement.setLong(3, bid.getLastBid());
				statement.setLong(4, bid.getLastBid());
			}

			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void registerBid(final L2PcInstance player, final long newBid)
	{
		if (player == null)
		{
			throw new NullPointerException();
		}

		if (newBid < getAuctionInitBid())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_PRICE_MUST_BE_HIGHER));
			return;
		}

		if (newBid > 100000000000L)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_CANT_EXCEED_100_BILLION));
			return;
		}

		if (getAuctionState() != ItemAuctionState.STARTED)
		{
			return;
		}

		final int playerObjId = player.getObjectId();

		synchronized (auctionBids)
		{
			if (highestBid != null && newBid < highestBid.getLastBid())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BID_MUST_BE_HIGHER_THAN_CURRENT_BID));
				return;
			}

			ItemAuctionBid bid = getBidfor(playerObjId);
			if (bid == null)
			{
				if (!reduceItemCount(player, newBid))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID));
					return;
				}

				bid = new ItemAuctionBid(playerObjId, newBid);
				auctionBids.add(bid);
			}
			else
			{
				if (!bid.isCanceled())
				{
					if (newBid < bid.getLastBid()) // just another check
					{
						player.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.BID_MUST_BE_HIGHER_THAN_CURRENT_BID));
						return;
					}

					if (!reduceItemCount(player, newBid - bid.getLastBid()))
					{
						player.sendPacket(
								SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID));
						return;
					}
				}
				else if (!reduceItemCount(player, newBid))
				{
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID));
					return;
				}

				bid.setLastBid(newBid);
			}

			onPlayerBid(player, bid);
			updatePlayerBid(bid, false);

			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_A_BID_OF_S1).addItemNumber(newBid));
		}
	}

	private void onPlayerBid(final L2PcInstance player, final ItemAuctionBid bid)
	{
		if (highestBid == null)
		{
			highestBid = bid;
		}
		else if (highestBid.getLastBid() < bid.getLastBid())
		{
			final L2PcInstance old = highestBid.getPlayer();
			if (old != null)
			{
				old.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_OUTBID));
			}

			highestBid = bid;
		}

		if (getEndingTime() - System.currentTimeMillis() <= 1000 * 60 * 10) // 10 minutes
		{
			switch (auctionEndingExtendState)
			{
				case INITIAL:
				{
					auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_5_MIN;
					endingTime += ENDING_TIME_EXTEND_5;
					broadcastToAllBidders(SystemMessage
							.getSystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_5_MINUTES));
					break;
				}
				case EXTEND_BY_5_MIN:
				{
					if (getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_3_MIN;
						endingTime += ENDING_TIME_EXTEND_3;
						broadcastToAllBidders(SystemMessage
								.getSystemMessage(SystemMessageId.BIDDER_EXISTS_AUCTION_TIME_EXTENDED_BY_3_MINUTES));
					}
					break;
				}
				case EXTEND_BY_3_MIN:
					if (Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID > 0)
					{
						if (getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
						{
							auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
							endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
						}
					}
					break;
				case EXTEND_BY_CONFIG_PHASE_A:
				{
					if (getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						if (scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B)
						{
							auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_B;
							endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
						}
					}
					break;
				}
				case EXTEND_BY_CONFIG_PHASE_B:
				{
					if (getAndSetLastBidPlayerObjectId(player.getObjectId()) != player.getObjectId())
					{
						if (scheduledAuctionEndingExtendState == ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A)
						{
							endingTime += Config.ALT_ITEM_AUCTION_TIME_EXTENDS_ON_BID;
							auctionEndingExtendState = ItemAuctionExtendState.EXTEND_BY_CONFIG_PHASE_A;
						}
					}
				}
			}
		}
	}

	public final void broadcastToAllBidders(final L2GameServerPacket packet)
	{
		ThreadPoolManager.getInstance().executeTask(() -> broadcastToAllBiddersInternal(packet));
	}

	public final void broadcastToAllBiddersInternal(final L2GameServerPacket packet)
	{
		for (int i = auctionBids.size(); i-- > 0; )
		{
			final ItemAuctionBid bid = auctionBids.get(i);
			if (bid != null)
			{
				final L2PcInstance player = bid.getPlayer();
				if (player != null)
				{
					player.sendPacket(packet);
				}
			}
		}
	}

	public final boolean cancelBid(final L2PcInstance player)
	{
		if (player == null)
		{
			throw new NullPointerException();
		}

		switch (getAuctionState())
		{
			case CREATED:
				return false;

			case FINISHED:
				if (startingTime < System.currentTimeMillis() -
						TimeUnit.MILLISECONDS.convert(Config.ALT_ITEM_AUCTION_EXPIRED_AFTER, TimeUnit.DAYS))
				{
					return false;
				}
				else
				{
					break;
				}
		}

		final int playerObjId = player.getObjectId();

		synchronized (auctionBids)
		{
			if (highestBid == null)
			{
				return false;
			}

			final int bidIndex = getBidIndexfor(playerObjId);
			if (bidIndex == -1)
			{
				return false;
			}

			final ItemAuctionBid bid = auctionBids.get(bidIndex);
			if (bid.getPlayerObjId() == highestBid.getPlayerObjId())
			{
				// can't return winning bid
				if (getAuctionState() == ItemAuctionState.FINISHED)
				{
					return false;
				}

				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HIGHEST_BID_BUT_RESERVE_NOT_MET));
				return true;
			}

			if (bid.isCanceled())
			{
				return false;
			}

			increaseItemCount(player, bid.getLastBid());
			bid.cancelBid();

			// delete bid from database if auction already finished
			updatePlayerBid(bid, getAuctionState() == ItemAuctionState.FINISHED);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANCELED_BID));
		}
		return true;
	}

	public final void clearCanceledBids()
	{
		if (getAuctionState() != ItemAuctionState.FINISHED)
		{
			throw new IllegalStateException("Attempt to clear canceled bids for non-finished auction");
		}

		synchronized (auctionBids)
		{
			for (ItemAuctionBid bid : auctionBids)
			{
				if (bid == null || !bid.isCanceled())
				{
					continue;
				}
				updatePlayerBid(bid, true);
			}
		}
	}

	private boolean reduceItemCount(final L2PcInstance player, final long count)
	{
		if (!player.reduceAdena("ItemAuction", count, player, true))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ADENA_FOR_THIS_BID));
			return false;
		}
		return true;
	}

	private void increaseItemCount(final L2PcInstance player, final long count)
	{
		player.addAdena("ItemAuction", count, player, true);
	}

	/**
	 * Returns the last bid for the given player or -1 if he did not made one yet.
	 *
	 * @param player The player that made the bid
	 * @return The last bid the player made or -1
	 */
	public final long getLastBid(final L2PcInstance player)
	{
		final ItemAuctionBid bid = getBidfor(player.getObjectId());
		return bid != null ? bid.getLastBid() : -1L;
	}

	public final ItemAuctionBid getBidfor(final int playerObjId)
	{
		final int index = getBidIndexfor(playerObjId);
		return index != -1 ? auctionBids.get(index) : null;
	}

	private int getBidIndexfor(final int playerObjId)
	{
		for (int i = auctionBids.size(); i-- > 0; )
		{
			final ItemAuctionBid bid = auctionBids.get(i);
			if (bid != null && bid.getPlayerObjId() == playerObjId)
			{
				return i;
			}
		}
		return -1;
	}
}
