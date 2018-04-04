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

package l2server.gameserver.cache;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.actor.instance.Player;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author -Nemesiss-
 */
public class WarehouseCacheManager {
	protected final ConcurrentHashMap<Player, Long> cachedWh;
	protected final long cacheTime;

	public static WarehouseCacheManager getInstance() {
		return SingletonHolder.instance;
	}

	private WarehouseCacheManager() {
		cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000L; // 60*1000 = 60000
		cachedWh = new ConcurrentHashMap<>();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}

	public void addCacheTask(Player pc) {
		cachedWh.put(pc, System.currentTimeMillis());
	}

	public void remCacheTask(Player pc) {
		cachedWh.remove(pc);
	}

	public class CacheScheduler implements Runnable {
		@Override
		public void run() {
			long cTime = System.currentTimeMillis();
			for (Player pc : cachedWh.keySet()) {
				if (cTime - cachedWh.get(pc) > cacheTime) {
					pc.clearWarehouse();
					cachedWh.remove(pc);
				}
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final WarehouseCacheManager instance = new WarehouseCacheManager();
	}
}
