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

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.model.itemauction.ItemAuctionInstance;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * @author Forsaiken
 */
public final class ItemAuctionManager
{
	public static ItemAuctionManager getInstance()
	{
		return SingletonHolder.instance;
	}

	private final TIntObjectHashMap<ItemAuctionInstance> managerInstances;
	private final AtomicInteger auctionIds;

	private ItemAuctionManager()
	{
		managerInstances = new TIntObjectHashMap<>();
		auctionIds = new AtomicInteger(1);

		if (!Config.ALT_ITEM_AUCTION_ENABLED || Config.IS_CLASSIC)
		{
			Log.info("ItemAuctionManager: Disabled by config.");
			return;
		}

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				auctionIds.set(rset.getInt(1) + 1);
			}
		}
		catch (final SQLException e)
		{
			Log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		final File file = new File(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "ItemAuctions.xml");
		if (!file.exists())
		{
			Log.warning("ItemAuctionManager: Missing ItemAuctions.xml!");
			return;
		}

		try
		{
			XmlDocument doc = new XmlDocument(file);
			for (XmlNode na : doc.getChildren())
			{
				if (na.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode nb : na.getChildren())
					{
						if (nb.getName().equalsIgnoreCase("instance"))
						{
							final int instanceId = nb.getInt("id");

							if (managerInstances.containsKey(instanceId))
							{
								throw new Exception("Dublicated instanceId " + instanceId);
							}

							final ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, auctionIds, nb);
							managerInstances.put(instanceId, instance);
						}
					}
				}
			}
			Log.info("ItemAuctionManager: Loaded " + managerInstances.size() + " instance(s).");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions from xml.", e);
		}
	}

	public final void shutdown()
	{
		final ItemAuctionInstance[] instances = managerInstances.getValues(new ItemAuctionInstance[managerInstances.size()]);
		for (final ItemAuctionInstance instance : instances)
		{
			instance.shutdown();
		}
	}

	public final ItemAuctionInstance getManagerInstance(final int instanceId)
	{
		return managerInstances.get(instanceId);
	}

	public final int getNextAuctionId()
	{
		return auctionIds.getAndIncrement();
	}

	public static void deleteAuction(final int auctionId)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?");
			statement.setInt(1, auctionId);
			statement.execute();
			statement.close();
		}
		catch (final SQLException e)
		{
			Log.log(Level.SEVERE, "L2ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ItemAuctionManager instance = new ItemAuctionManager();
	}
}
