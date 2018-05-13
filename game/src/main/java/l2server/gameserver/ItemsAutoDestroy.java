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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.World;
import l2server.gameserver.templates.item.EtcItemType;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemsAutoDestroy {
	private static Logger log = LoggerFactory.getLogger(ItemsAutoDestroy.class.getName());

	protected List<Item> items = null;
	protected static long sleep;

	private ItemsAutoDestroy() {
	}
	
	@Load
	public void initialize() {
		if (Config.AUTODESTROY_ITEM_AFTER * 1000 <= 0 && Config.HERB_AUTO_DESTROY_TIME * 1000 <= 0) {
			return;
		}
		log.debug("Initializing ItemsAutoDestroy.");
		items = new CopyOnWriteArrayList<>();
		sleep = Config.AUTODESTROY_ITEM_AFTER * 1000;
		if (sleep == 0) // it should not happend as it is not called when AUTODESTROY_ITEM_AFTER = 0 but we never know..
		{
			sleep = 3600000;
		}
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckItemsForDestroy(), 5000, 5000);
	}
	
	public static ItemsAutoDestroy getInstance() {
		return SingletonHolder.instance;
	}

	public synchronized void addItem(Item item) {
		item.setDropTime(System.currentTimeMillis());
		items.add(item);
	}

	public synchronized void removeItems() {
		if (Config.DEBUG) {
			log.info("[ItemsAutoDestroy] : " + items.size() + " items to check.");
		}

		if (items.isEmpty()) {
			return;
		}

		long curtime = System.currentTimeMillis();
		for (Item item : items) {
			if (item == null || item.getDropTime() == 0 || item.getLocation() != Item.ItemLocation.VOID) {
				items.remove(item);
			} else {
				if (item.getItem().getAutoDestroyTime() > 0) {
					if (curtime - item.getDropTime() > item.getItem().getAutoDestroyTime()) {
						World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						World.getInstance().removeObject(item);
						items.remove(item);
						if (Config.SAVE_DROPPED_ITEM) {
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				}

				if (item.getItemType() == EtcItemType.HERB) {
					if (curtime - item.getDropTime() > Config.HERB_AUTO_DESTROY_TIME * 1000) {
						World.getInstance().removeVisibleObject(item, item.getWorldRegion());
						World.getInstance().removeObject(item);
						items.remove(item);
						if (Config.SAVE_DROPPED_ITEM) {
							ItemsOnGroundManager.getInstance().removeObject(item);
						}
					}
				} else if (curtime - item.getDropTime() > sleep) {
					World.getInstance().removeVisibleObject(item, item.getWorldRegion());
					World.getInstance().removeObject(item);
					items.remove(item);
					if (Config.SAVE_DROPPED_ITEM) {
						ItemsOnGroundManager.getInstance().removeObject(item);
					}
				}
			}
		}

		if (Config.DEBUG) {
			log.info("[ItemsAutoDestroy] : " + items.size() + " items remaining.");
		}
	}

	protected class CheckItemsForDestroy extends Thread {
		@Override
		public void run() {
			removeItems();
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ItemsAutoDestroy instance = new ItemsAutoDestroy();
	}
}
