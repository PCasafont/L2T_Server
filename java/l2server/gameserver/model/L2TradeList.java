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

package l2server.gameserver.model;

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.templates.item.L2Item;
import l2server.log.Log;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2TradeList
{
	private final Map<Integer, L2TradeItem> items = new LinkedHashMap<>();
	private final int listId;

	private String buystorename, sellstorename;
	@Setter private boolean hasLimitedStockItem;
	@Getter private int npcId;

	public L2TradeList(int listId)
	{
		this.listId = listId;
	}

	public void setNpcId(int id)
	{
		npcId = id;
	}


	public void addItem(L2TradeItem item)
	{
		items.put(item.getItemId(), item);
		if (item.hasLimitedStock())
		{
			setHasLimitedStockItem(true);
		}
	}

	public void replaceItem(int itemID, long price)
	{
		L2TradeItem item = items.get(itemID);
		if (item != null)
		{
			item.setPrice(price);
		}
	}

	public void removeItem(int itemID)
	{
		items.remove(itemID);
	}

	/**
	 * @return Returns the listId.
	 */
	public int getListId()
	{
		return listId;
	}

	/**
	 * @return Returns the hasLimitedStockItem.
	 */
	public boolean hasLimitedStockItem()
	{
		return hasLimitedStockItem;
	}

	public void setSellStoreName(String name)
	{
		sellstorename = name;
	}

	public String getSellStoreName()
	{
		return sellstorename;
	}

	public void setBuyStoreName(String name)
	{
		buystorename = name;
	}

	public String getBuyStoreName()
	{
		return buystorename;
	}

	/**
	 * @return Returns the items.
	 */
	public Collection<L2TradeItem> getItems()
	{
		return items.values();
	}

	public List<L2TradeItem> getItems(int start, int end)
	{
		List<L2TradeItem> list = new LinkedList<>();
		list.addAll(items.values());
		return list.subList(start, end);
	}

	public long getPriceForItemId(int itemId)
	{
		L2TradeItem item = items.get(itemId);
		if (item != null)
		{
			return item.getPrice();
		}
		return -1;
	}

	public L2TradeItem getItemById(int itemId)
	{
		return items.get(itemId);
	}

	public boolean containsItemId(int itemId)
	{
		return items.containsKey(itemId);
	}

	/**
	 * Itens representation for trade lists
	 *
	 * @author KenM
	 */
	public static class L2TradeItem
	{
		private final int listId;
		private final int itemId;
		private final L2Item template;
		@Getter @Setter private long price;

		// count related
		private AtomicLong currentCount = new AtomicLong();
		@Getter @Setter private int maxCount = -1;
		@Getter private long restoreDelay;
		@Setter private long nextRestoreTime = 0;

		public L2TradeItem(int listId, int itemId)
		{
			this.listId = listId;
			this.itemId = itemId;
			template = ItemTable.getInstance().getTemplate(itemId);
		}

		/**
		 * @return Returns the itemId.
		 */
		public int getItemId()
		{
			return itemId;
		}

		public L2Item getTemplate()
		{
			return template;
		}

		/**
		 * @param currentCount The currentCount to set.
		 */
		public void setCurrentCount(long currentCount)
		{
			this.currentCount.set(currentCount);
		}

		public boolean decreaseCount(long val)
		{
			return currentCount.addAndGet(-val) >= 0;
		}

		/**
		 * @return Returns the currentCount.
		 */
		public long getCurrentCount()
		{
			if (hasLimitedStock() && isPendingStockUpdate())
			{
				restoreInitialCount();
			}
			long ret = currentCount.get();
			return ret > 0 ? ret : 0;
		}

		public boolean isPendingStockUpdate()
		{
			return System.currentTimeMillis() >= nextRestoreTime && currentCount.get() < maxCount;
		}

		public void restoreInitialCount()
		{
			setCurrentCount(getMaxCount());
			nextRestoreTime = nextRestoreTime + getRestoreDelay();

			// consume until next update is on future
			if (isPendingStockUpdate() && getRestoreDelay() > 0)
			{
				nextRestoreTime = System.currentTimeMillis() + getRestoreDelay();
			}

			saveDataTimer();
		}

		/**
		 * @param maxCount The maxCount to set.
		 */

		/**
		 * @return Returns the maxCount.
		 */

		public boolean hasLimitedStock()
		{
			return getMaxCount() > -1;
		}

		/**
		 * @param restoreDelay The restoreDelay to set (in hours)
		 */
		public void setRestoreDelay(int restoreDelay)
		{
			this.restoreDelay = restoreDelay * 60 * 60 * 1000;
		}

		/**
		 * @return Returns the restoreDelay (in milis)
		 */

		/**
		 * For resuming when server loads
		 *
		 * @param nextRestoreTime The nextRestoreTime to set.
		 */

		protected void saveDataTimer()
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(
						"REPLACE INTO shop_item_counts (shop_id, item_id, count, time) VALUES (?, ?, ?, ?)");
				statement.setInt(1, listId);
				statement.setInt(2, itemId);
				statement.setInt(3, maxCount);
				statement.setLong(4, nextRestoreTime);
				statement.executeUpdate();
				statement.close();
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "L2TradeItem: Could not update Timer save in Buylist");
				Log.warning(e.getMessage());
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
}
