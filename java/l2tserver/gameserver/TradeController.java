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
package l2tserver.gameserver;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import l2tserver.Config;
import l2tserver.L2DatabaseFactory;
import l2tserver.gameserver.datatables.ItemTable;
import l2tserver.gameserver.model.L2TradeList;
import l2tserver.gameserver.model.L2TradeList.L2TradeItem;
import l2tserver.log.Log;
import l2tserver.util.xml.XmlDocument;
import l2tserver.util.xml.XmlNode;

/**
 * This class ...
 *
 * @version $Revision: 1.5.4.13 $ $Date: 2005/04/06 16:13:38 $
 */
public class TradeController implements Reloadable
{
	
	private Map<Integer, L2TradeList> _lists = new HashMap<Integer, L2TradeList>();
	
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
	
	public boolean reload()
	{
		_lists.clear();
		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "shops");
		for (File file : dir.listFiles())
		{
			if (!file.getName().endsWith(".xml"))
				continue;
			
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
							
							L2TradeList buy = new L2TradeList(id);
							
							for (XmlNode shopNode : d.getChildren())
							{
								if (shopNode.getName().equalsIgnoreCase("item"))
								{
									int itemId = shopNode.getInt("id");
									long price = -1;
									if (shopNode.hasAttribute("price"))
										price = shopNode.getLong("price");
									int count = -1;
									if (shopNode.hasAttribute("count"))
										count = shopNode.getInt("count");
									int time = 0;
									if (shopNode.hasAttribute("time"))
										time = shopNode.getInt("time");
									
									L2TradeItem item = new L2TradeItem(id, itemId);
									if (ItemTable.getInstance().getTemplate(itemId) == null)
									{
										Log.warning("Skipping itemId: " + itemId + " on buylistId: " + id + ", missing data for that item.");
										continue;
									}
									
									if (price <= -1)
									{
										price = ItemTable.getInstance().getTemplate(itemId).getReferencePrice();
										if (price == 0 && npcId != -1)
											Log.warning("ItemId: " + itemId + " on buylistId: " + id + " has price = 0!");
									}
									
									if (Config.DEBUG)
									{
										// debug
										double diff = ((double) (price)) / ItemTable.getInstance().getTemplate(itemId).getReferencePrice();
										if (diff < 0.8 || diff > 1.2)
										{
											Log.severe("PRICING DEBUG: TradeListId: " + id + " -  ItemId: " + itemId + " ("
													+ ItemTable.getInstance().getTemplate(itemId).getName() + ") diff: " + diff + " - Price: " + price
													+ " - Reference: " + ItemTable.getInstance().getTemplate(itemId).getReferencePrice());
										}
									}
									
									item.setPrice(price);
									
									item.setRestoreDelay(time);
									item.setMaxCount(count);
									
									buy.addItem(item);
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
				
				L2TradeItem item = _lists.get(shopId).getItemById(itemId);
				
				if (currentCount > -1)
					item.setCurrentCount(currentCount);
				else
				{
					item.setCurrentCount(item.getMaxCount());
					PreparedStatement st = con.prepareStatement("DELETE FROM shop_item_counts WHERE shop_id = ? AND item_id = ?");
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
		List<L2TradeList> lists = new ArrayList<L2TradeList>();
		Collection<L2TradeList> values = _lists.values();
		
		for (L2TradeList list : values)
		{
			int tradeNpcId = list.getNpcId();
			if (tradeNpcId == -1)
				continue;
			if (npcId == tradeNpcId)
				lists.add(list);
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
			PreparedStatement statement = con.prepareStatement("UPDATE shop_item_counts SET count = ? WHERE shop_id = ? AND item_id = ?");
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
