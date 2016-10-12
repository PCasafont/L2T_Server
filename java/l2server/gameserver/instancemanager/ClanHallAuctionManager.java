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

import l2server.L2DatabaseFactory;
import l2server.gameserver.model.entity.Auction;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ClanHallAuctionManager
{
	private List<Auction> _auctions;

	private static final String[] ITEM_INIT_DATA = {
			"(22, 0, 'NPC', 'NPC Clan', 'ClanHall', 22, 0, 'Moonstone Hall', 1, 20000000, 0, 1164841200000)",
			"(23, 0, 'NPC', 'NPC Clan', 'ClanHall', 23, 0, 'Onyx Hall', 1, 20000000, 0, 1164841200000)",
			"(24, 0, 'NPC', 'NPC Clan', 'ClanHall', 24, 0, 'Topaz Hall', 1, 20000000, 0, 1164841200000)",
			"(25, 0, 'NPC', 'NPC Clan', 'ClanHall', 25, 0, 'Ruby Hall', 1, 20000000, 0, 1164841200000)",
			"(26, 0, 'NPC', 'NPC Clan', 'ClanHall', 26, 0, 'Crystal Hall', 1, 20000000, 0, 1164841200000)",
			"(27, 0, 'NPC', 'NPC Clan', 'ClanHall', 27, 0, 'Onyx Hall', 1, 20000000, 0, 1164841200000)",
			"(28, 0, 'NPC', 'NPC Clan', 'ClanHall', 28, 0, 'Sapphire Hall', 1, 20000000, 0, 1164841200000)",
			"(29, 0, 'NPC', 'NPC Clan', 'ClanHall', 29, 0, 'Moonstone Hall', 1, 20000000, 0, 1164841200000)",
			"(30, 0, 'NPC', 'NPC Clan', 'ClanHall', 30, 0, 'Emerald Hall', 1, 20000000, 0, 1164841200000)",
			"(31, 0, 'NPC', 'NPC Clan', 'ClanHall', 31, 0, 'The Atramental Barracks', 1, 8000000, 0, 1164841200000)",
			"(32, 0, 'NPC', 'NPC Clan', 'ClanHall', 32, 0, 'The Scarlet Barracks', 1, 8000000, 0, 1164841200000)",
			"(33, 0, 'NPC', 'NPC Clan', 'ClanHall', 33, 0, 'The Viridian Barracks', 1, 8000000, 0, 1164841200000)",
			"(36, 0, 'NPC', 'NPC Clan', 'ClanHall', 36, 0, 'The Golden Chamber', 1, 50000000, 0, 1164841200000)",
			"(37, 0, 'NPC', 'NPC Clan', 'ClanHall', 37, 0, 'The Silver Chamber', 1, 50000000, 0, 1164841200000)",
			"(38, 0, 'NPC', 'NPC Clan', 'ClanHall', 38, 0, 'The Mithril Chamber', 1, 50000000, 0, 1164841200000)",
			"(39, 0, 'NPC', 'NPC Clan', 'ClanHall', 39, 0, 'Silver Manor', 1, 50000000, 0, 1164841200000)",
			"(40, 0, 'NPC', 'NPC Clan', 'ClanHall', 40, 0, 'Gold Manor', 1, 50000000, 0, 1164841200000)",
			"(41, 0, 'NPC', 'NPC Clan', 'ClanHall', 41, 0, 'The Bronze Chamber', 1, 50000000, 0, 1164841200000)",
			"(42, 0, 'NPC', 'NPC Clan', 'ClanHall', 42, 0, 'The Golden Chamber', 1, 50000000, 0, 1164841200000)",
			"(43, 0, 'NPC', 'NPC Clan', 'ClanHall', 43, 0, 'The Silver Chamber', 1, 50000000, 0, 1164841200000)",
			"(44, 0, 'NPC', 'NPC Clan', 'ClanHall', 44, 0, 'The Mithril Chamber', 1, 50000000, 0, 1164841200000)",
			"(45, 0, 'NPC', 'NPC Clan', 'ClanHall', 45, 0, 'The Bronze Chamber', 1, 50000000, 0, 1164841200000)",
			"(46, 0, 'NPC', 'NPC Clan', 'ClanHall', 46, 0, 'Silver Manor', 1, 50000000, 0, 1164841200000)",
			"(47, 0, 'NPC', 'NPC Clan', 'ClanHall', 47, 0, 'Moonstone Hall', 1, 50000000, 0, 1164841200000)",
			"(48, 0, 'NPC', 'NPC Clan', 'ClanHall', 48, 0, 'Onyx Hall', 1, 50000000, 0, 1164841200000)",
			"(49, 0, 'NPC', 'NPC Clan', 'ClanHall', 49, 0, 'Emerald Hall', 1, 50000000, 0, 1164841200000)",
			"(50, 0, 'NPC', 'NPC Clan', 'ClanHall', 50, 0, 'Sapphire Hall', 1, 50000000, 0, 1164841200000)",
			"(51, 0, 'NPC', 'NPC Clan', 'ClanHall', 51, 0, 'Mont Chamber', 1, 50000000, 0, 1164841200000)",
			"(52, 0, 'NPC', 'NPC Clan', 'ClanHall', 52, 0, 'Astaire Chamber', 1, 50000000, 0, 1164841200000)",
			"(53, 0, 'NPC', 'NPC Clan', 'ClanHall', 53, 0, 'Aria Chamber', 1, 50000000, 0, 1164841200000)",
			"(54, 0, 'NPC', 'NPC Clan', 'ClanHall', 54, 0, 'Yiana Chamber', 1, 50000000, 0, 1164841200000)",
			"(55, 0, 'NPC', 'NPC Clan', 'ClanHall', 55, 0, 'Roien Chamber', 1, 50000000, 0, 1164841200000)",
			"(56, 0, 'NPC', 'NPC Clan', 'ClanHall', 56, 0, 'Luna Chamber', 1, 50000000, 0, 1164841200000)",
			"(57, 0, 'NPC', 'NPC Clan', 'ClanHall', 57, 0, 'Traban Chamber', 1, 50000000, 0, 1164841200000)",
			"(58, 0, 'NPC', 'NPC Clan', 'ClanHall', 58, 0, 'Eisen Hall', 1, 50000000, 0, 1164841200000)",
			"(59, 0, 'NPC', 'NPC Clan', 'ClanHall', 59, 0, 'Heavy Metal Hall', 1, 50000000, 0, 1164841200000)",
			"(60, 0, 'NPC', 'NPC Clan', 'ClanHall', 60, 0, 'Molten Ore Hall', 1, 50000000, 0, 1164841200000)",
			"(61, 0, 'NPC', 'NPC Clan', 'ClanHall', 61, 0, 'Titan Hall', 1, 50000000, 0, 1164841200000)",
			"(65, 0, 'NPC', 'NPC Clan', 'ClanHall', 65, 0, 'Emerald Hall', 1, 50000000, 0, 1164841200000)",
			"(66, 0, 'NPC', 'NPC Clan', 'ClanHall', 66, 0, 'Aquamarine Hall', 1, 50000000, 0, 1164841200000)",
			"(67, 0, 'NPC', 'NPC Clan', 'ClanHall', 67, 0, 'Crystal Hall', 1, 50000000, 0, 1164841200000)",
			"(68, 0, 'NPC', 'NPC Clan', 'ClanHall', 68, 0, 'Sapphire Hall', 1, 50000000, 0, 1164841200000)",
			"(69, 0, 'NPC', 'NPC Clan', 'ClanHall', 69, 0, 'Yellow Barracks', 1, 50000000, 0, 1164841200000)",
			"(70, 0, 'NPC', 'NPC Clan', 'ClanHall', 70, 0, 'White Barracks', 1, 50000000, 0, 1164841200000)",
			"(71, 0, 'NPC', 'NPC Clan', 'ClanHall', 71, 0, 'Blue Barracks', 1, 50000000, 0, 1164841200000)",
			"(72, 0, 'NPC', 'NPC Clan', 'ClanHall', 72, 0, 'Brown Barracks', 1, 50000000, 0, 1164841200000)",
			"(73, 0, 'NPC', 'NPC Clan', 'ClanHall', 73, 0, 'Black Barracks', 1, 50000000, 0, 1164841200000)",
			"(74, 0, 'NPC', 'NPC Clan', 'ClanHall', 74, 0, 'Green Barracks', 1, 50000000, 0, 1164841200000)"
	};

	private static final int[] ItemInitDataId = {
			22,
			23,
			24,
			25,
			26,
			27,
			28,
			29,
			30,
			31,
			32,
			33,
			36,
			37,
			38,
			39,
			40,
			41,
			42,
			43,
			44,
			45,
			46,
			47,
			48,
			49,
			50,
			51,
			52,
			53,
			54,
			55,
			56,
			57,
			58,
			59,
			60,
			61,
			65,
			66,
			67,
			68,
			69,
			70,
			71,
			72,
			73,
			74
	};

	public static ClanHallAuctionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private ClanHallAuctionManager()
	{
		Log.info("Initializing AuctionManager");
		_auctions = new ArrayList<>();
		load();
	}

	public void reload()
	{
		_auctions.clear();
		load();
	}

	private void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT id FROM clanhall_auction ORDER BY id");
			rs = statement.executeQuery();
			while (rs.next())
			{
				_auctions.add(new Auction(rs.getInt("id")));
			}
			statement.close();
			Log.info("Loaded: " + getAuctions().size() + " auction(s)");
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: AuctionManager.load(): " + e.getMessage(), e);
		}

		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final Auction getAuction(int auctionId)
	{
		int index = getAuctionIndex(auctionId);
		if (index >= 0)
		{
			return getAuctions().get(index);
		}
		return null;
	}

	public final int getAuctionIndex(int auctionId)
	{
		Auction auction;
		for (int i = 0; i < getAuctions().size(); i++)
		{
			auction = getAuctions().get(i);
			if (auction != null && auction.getId() == auctionId)
			{
				return i;
			}
		}
		return -1;
	}

	public final List<Auction> getAuctions()
	{
		return _auctions;
	}

	/**
	 * Init Clan NPC aution
	 */
	public void initNPC(int id)
	{
		Connection con = null;
		int i;
		for (i = 0; i < ItemInitDataId.length; i++)
		{
			if (ItemInitDataId[i] == id)
			{
				break;
			}
		}
		if (i >= ItemInitDataId.length || ItemInitDataId[i] != id)
		{
			Log.warning("Clan Hall auction not found for Id :" + id);
			return;
		}
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("REPLACE INTO `clanhall_auction` VALUES " + ITEM_INIT_DATA[i]);
			statement.execute();
			statement.close();
			_auctions.add(new Auction(id));
			Log.info("Created auction for ClanHall: " + id);
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Exception: Auction.initNPC(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ClanHallAuctionManager _instance = new ClanHallAuctionManager();
	}
}
