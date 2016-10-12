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
		return SingletonHolder._instance;
	}

	private final TIntObjectHashMap<ItemAuctionInstance> _managerInstances;
	private final AtomicInteger _auctionIds;

	private ItemAuctionManager()
	{
		_managerInstances = new TIntObjectHashMap<>();
		_auctionIds = new AtomicInteger(1);

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
				_auctionIds.set(rset.getInt(1) + 1);
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

							if (_managerInstances.containsKey(instanceId))
							{
								throw new Exception("Dublicated instanceId " + instanceId);
							}

							final ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, _auctionIds, nb);
							_managerInstances.put(instanceId, instance);
						}
					}
				}
			}
			Log.info("ItemAuctionManager: Loaded " + _managerInstances.size() + " instance(s).");
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "ItemAuctionManager: Failed loading auctions from xml.", e);
		}
	}

	public final void shutdown()
	{
		final ItemAuctionInstance[] instances =
				_managerInstances.getValues(new ItemAuctionInstance[_managerInstances.size()]);
		for (final ItemAuctionInstance instance : instances)
		{
			instance.shutdown();
		}
	}

	public final ItemAuctionInstance getManagerInstance(final int instanceId)
	{
		return _managerInstances.get(instanceId);
	}

	public final int getNextAuctionId()
	{
		return _auctionIds.getAndIncrement();
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
		protected static final ItemAuctionManager _instance = new ItemAuctionManager();
	}
}
