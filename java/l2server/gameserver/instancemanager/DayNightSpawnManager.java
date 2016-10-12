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

import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.instance.L2RaidBossInstance;
import l2server.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author godson
 */
public class DayNightSpawnManager
{

	private Map<L2Spawn, L2RaidBossInstance> _bosses;

	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private DayNightSpawnManager()
	{
		_bosses = new HashMap<>();

		Log.info("DayNightSpawnManager: Day/Night handler initialized");
	}

	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		SpawnTable.getInstance().spawnSpecificTable("day_spawns");

		SpawnTable.getInstance().despawnSpecificTable("night_spawns");
	}

	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		SpawnTable.getInstance().spawnSpecificTable("night_spawns");

		SpawnTable.getInstance().despawnSpecificTable("day_spawns");
	}

	private void changeMode(int mode)
	{
		switch (mode)
		{
			case 0:
				spawnDayCreatures();
				break;
			case 1:
				spawnNightCreatures();
				break;
			default:
				Log.warning("DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}

	public void notifyChangeMode()
	{
		try
		{
			if (TimeController.getInstance().isNowNight())
			{
				changeMode(1);
			}
			else
			{
				changeMode(0);
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
	}

	public void cleanUp()
	{
		_bosses.clear();
	}
}
