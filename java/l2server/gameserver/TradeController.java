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

package l2server.gameserver;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.L2TradeList.L2TradeItem;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController implements Reloadable
{

	private Map<Integer, L2TradeList> _lists = new HashMap<>();

	public static TradeController getInstance()
    {
		return SingletonHolder._instance;
	}

	private TradeController()
	{
		reload();
		loadItemCounts();

		ReloadableManager.getInstance().register("shops", this);
	}

	@Override
	public boolean reload()
	{
		_lists.clear();
		if (!load(Config.DATAPACK_ROOT + "/data_" + Config.SERVER_NAME + "/shops/"))
		{
			return false;
		}

		return load(Config.DATAPACK_ROOT + "/" + Config.DATA_FOLDER + "/shops/");

	}

	public boolean load(String path)
	{
		File dir = new File(path);
		if (!dir.exists())
		{
			return false;
		}

		for (File file : dir.listFiles())
		{
			if (!file.getName().endsWith(".xml"))
			{
				continue;
			}

			XmlDocument doc = new XmlDocument(file);
			for (XmlNode n : doc.getChildren())
			{
				if (n.getName().equalsIgnoreCase("list"))
				{
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("shop"))
						{
							int id = d.getInt("id");
							int npcId = d.getInt("npcId");

							if (_lists.containsKey(id))
							{
								continue;
							}

							L2TradeList buy = new L2TradeList(id);

							L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
							if (npcTemplate == null)
							{
								if (npcId != -1)
								{
									Log.warning("No template found for NpcId " + npcId);
								}

								continue;
							}

							for (XmlNode shopNode : d.getChildren())
							{
								if (shopNode.getName().equalsIgnoreCase("item"))
								{
									int itemId = shopNode.getInt("id");

									final L2Item itemTemplate = ItemTable.getInstance().getTemplate(itemId);
									if (itemTemplate == null)
									{
										Log.warning("Skipping itemId: " + itemId + " on buylistId: " + id +
												", missing data for that item.");
										continue;
									}

									L2TradeItem item = new L2TradeItem(id, itemId);
									long price = shopNode.getLong("price", -1);
									int count = shopNode.getInt("count", -1);
									int time = shopNode.getInt("time", 0);
									if (price <= -1)
									{
										price = itemTemplate.getReferencePrice();
										if (price == 0 && npcId != -1)
										{
											Log.warning(
													"ItemId: " + itemId + " on buylistId: " + id + " has price = 0!");
										}
									}

									if (Config.DEBUG)
									{
										// debug
										double diff = (double) price /
												ItemTable.getInstance().getTemplate(itemId).getReferencePrice();
										if (diff < 0.8 || diff > 1.2)
										{
											Log.severe("PRICING DEBUG: TradeListId: " + id + " -  ItemId: " + itemId +
													" (" + ItemTable.getInstance().getTemplate(itemId).getName() +
													") diff: " + diff + " - Price: " + price + " - Reference: " +
													ItemTable.getInstance().getTemplate(itemId).getReferencePrice());
										}
									}

									item.setPrice(price);

									item.setRestoreDelay(time);
									item.setMaxCount(count);

									buy.addItem(item);

									itemTemplate.setSalePrice(0);
								}
							}

							buy.setNpcId(npcId);
							_lists.put(id, buy);
						}
					}
				}
			}
		}

		Log.info("TradeController: Loaded " + _lists.size() + " Buylists.");
		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Standard Shops reloaded";
	}

	private void loadItemCounts()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM shop_item_counts");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				int shopId = rset.getInt("shop_id");
				int itemId = rset.getInt("item_id");
				int currentCount = rset.getInt("count");
				long savedTime = rset.getLong("time");

				L2TradeList tradeList = _lists.get(shopId);
                if (tradeList == null)
                {
                    continue;
                }

				L2TradeItem item = tradeList.getItemById(itemId);

				if (currentCount > -1)
				{
					item.setCurrentCount(currentCount);
				}
				else
				{
					item.setCurrentCount(item.getMaxCount());
					PreparedStatement st =
							con.prepareStatement("DELETE FROM shop_item_counts WHERE shop_id = ? AND item_id = ?");
					st.setInt(1, shopId);
					st.setInt(2, itemId);
					st.executeUpdate();
					continue;
				}

				item.setNextRestoreTime(savedTime);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}

	public L2TradeList getBuyList(int listId)
	{
		return _lists.get(listId);
	}

	public List<L2TradeList> getBuyListByNpcId(int npcId)
	{
		List<L2TradeList> lists = new ArrayList<>();
		Collection<L2TradeList> values = _lists.values();

		for (L2TradeList list : values)
		{
			int tradeNpcId = list.getNpcId();
			if (tradeNpcId == -1)
			{
				continue;
			}
			if (npcId == tradeNpcId)
			{
				lists.add(list);
			}
		}
		return lists;
	}

	public void dataCountStore()
	{
		Connection con = null;
		int listId;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("UPDATE shop_item_counts SET count = ? WHERE shop_id = ? AND item_id = ?");
			for (L2TradeList list : _lists.values())
			{
				if (list.hasLimitedStockItem())
				{
					listId = list.getListId();

					for (L2TradeItem item : list.getItems())
					{
						long currentCount = item.getCurrentCount();
						if (item.hasLimitedStock() && currentCount < item.getMaxCount())
						{
							statement.setLong(1, currentCount);
							statement.setInt(2, listId);
							statement.setInt(3, item.getItemId());
							statement.executeUpdate();
							statement.clearParameters();
						}
					}
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "TradeController: Could not store Count Item: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TradeController _instance = new TradeController();
	}
}
