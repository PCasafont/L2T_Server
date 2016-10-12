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
import l2server.gameserver.instancemanager.ItemsOnGroundManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2World;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.log.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemsAutoDestroy
{
	protected List<L2ItemInstance> _items = null;
	protected static long _sleep;

	private ItemsAutoDestroy()
	{
		Log.info("Initializing ItemsAutoDestroy.");
		_items = new CopyOnWriteArrayList<>();
		_sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
		if (_sleep ==
				0) // it should not happend as it is not called when AUTODESTROY_ITEM_AFTER = 0 but we never know..
		{
			_sleep = 3600000;
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 5000, 5000);
	}

	public static ItemsAutoDestroy getInstance()
	{
		return SingletonHolder._instance;
	}

	public synchronized void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}

	public synchronized void removeItems()
	{
		if (Config.DEBUG)
		{
			Log.info("[ItemsAutoDestroy] : " + _items.size() + " items to check.");
		}

		if (_items.isEmpty())
		{
			return;
		}

		long curtime = System.currentTimeMillis();
		for (L2ItemInstance item : _items)
		{
			if (item == null || item.getDropTime() == 0 || item.getLocation() != L2ItemInstance.ItemLocation.VOID)
			{
				_items.remove(item);
			}
			else
			{
				if (item.getItem().getAutoDestroyTime() > 0)
				{
					if (curtime - item.getDropTime() > item.getItem().getAutoDestroyTime())
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item);
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}

				if (item.getItemType() == L2EtcItemType.HERB)
				{
					if (curtime - item.getDropTime() > Config.HERB_AUTO_DESTROY_TIME * 1000)
					{
						L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						L2World.getInstance().removeObject(item);
						_items.remove(item);
						if (Config.SAVE_DROPPED_ITEM)
						{
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}
				else if (curtime - item.getDropTime() > _sleep)
				{
					L2World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					L2World.getInstance().removeObject(item);
					_items.remove(item);
					if (Config.SAVE_DROPPED_ITEM)
					{
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
		}

		if (Config.DEBUG)
		{
			Log.info("[ItemsAutoDestroy] : " + _items.size() + " items remaining.");
		}
	}

	protected class CheckItemsForDestroy extends Thread
	{
		@Override
		public void run()
		{
			removeItems();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroy _instance = new ItemsAutoDestroy();
	}
}
