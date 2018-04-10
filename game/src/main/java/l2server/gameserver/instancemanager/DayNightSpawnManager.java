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
import l2server.gameserver.model.actor.instance.RaidBossInstance;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author godson
 */
public class DayNightSpawnManager {
	private static Logger log = LoggerFactory.getLogger(DayNightSpawnManager.class.getName());

	private Map<L2Spawn, RaidBossInstance> bosses = new HashMap<>();

	public static DayNightSpawnManager getInstance() {
		return SingletonHolder.instance;
	}

	private DayNightSpawnManager() {
	}

	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures() {
		SpawnTable.getInstance().spawnSpecificTable("day_spawns");

		SpawnTable.getInstance().despawnSpecificTable("night_spawns");
	}

	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures() {
		SpawnTable.getInstance().spawnSpecificTable("night_spawns");

		SpawnTable.getInstance().despawnSpecificTable("day_spawns");
	}

	private void changeMode(int mode) {
		switch (mode) {
			case 0:
				spawnDayCreatures();
				break;
			case 1:
				spawnNightCreatures();
				break;
			default:
				log.warn("DayNightSpawnManager: Wrong mode sent");
				break;
		}
	}

	@Load(dependencies = {TimeController.class, SpawnTable.class})
	public void notifyChangeMode() {
		try {
			if (TimeController.getInstance().isNowNight()) {
				changeMode(1);
			} else {
				changeMode(0);
			}
		} catch (Exception e) {
			log.warn("Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final DayNightSpawnManager instance = new DayNightSpawnManager();
	}

	public void cleanUp() {
		bosses.clear();
	}
}
