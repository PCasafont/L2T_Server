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
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author -Nemesiss-
 */
public class WarehouseCacheManager
{
	protected final ConcurrentHashMap<L2PcInstance, Long> _cachedWh;
	protected final long _cacheTime;

	public static WarehouseCacheManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private WarehouseCacheManager()
	{
		_cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000L; // 60*1000 = 60000
		_cachedWh = new ConcurrentHashMap<>();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}

	public void addCacheTask(L2PcInstance pc)
	{
		_cachedWh.put(pc, System.currentTimeMillis());
	}

	public void remCacheTask(L2PcInstance pc)
	{
		_cachedWh.remove(pc);
	}

	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			long cTime = System.currentTimeMillis();
			for (L2PcInstance pc : _cachedWh.keySet())
			{
				if (cTime - _cachedWh.get(pc) > _cacheTime)
				{
					pc.clearWarehouse();
					_cachedWh.remove(pc);
				}
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final WarehouseCacheManager _instance = new WarehouseCacheManager();
	}
}
