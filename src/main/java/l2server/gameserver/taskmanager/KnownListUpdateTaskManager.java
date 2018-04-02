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

package l2server.gameserver.taskmanager;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.GuardInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.loader.annotations.Load;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

public class KnownListUpdateTaskManager {
	private static Logger log = LoggerFactory.getLogger(KnownListUpdateTaskManager.class.getName());



	private static final int FULL_UPDATE_TIMER = 100;
	public static boolean updatePass = true;

	// Do full update every FULL_UPDATE_TIMER * KNOWNLIST_UPDATE_INTERVAL
	public static int fullUpdateTimer = FULL_UPDATE_TIMER;

	private static final HashSet<WorldRegion> failedRegions = new HashSet<>(1);

	private KnownListUpdateTaskManager() {
	}
	
	@Load(dependencies = World.class)
	private void initialize() {
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new KnownListUpdate(), 1000, Config.KNOWNLIST_UPDATE_INTERVAL);
	}
	
	public static KnownListUpdateTaskManager getInstance() {
		return SingletonHolder.instance;
	}

	private class KnownListUpdate implements Runnable {
		public KnownListUpdate() {
		}

		@Override
		public void run() {
			try {
				boolean failed;
				for (WorldRegion regions[] : World.getInstance().getAllWorldRegions()) {
					for (WorldRegion r : regions) // go through all world regions
					{
						// avoid stopping update if something went wrong in updateRegion()
						try {
							failed = failedRegions.contains(r); // failed on last pass
							if (r.isActive()) // and check only if the region is active
							{
								updateRegion(r, fullUpdateTimer == FULL_UPDATE_TIMER || failed, updatePass);
							}
							if (failed) {
								failedRegions.remove(r); // if all ok, remove
							}
						} catch (Exception e) {
							log.warn(
									"KnownListUpdateTaskManager: updateRegion(" + fullUpdateTimer + "," + updatePass + ") failed for region " +
											r.getName() + ". Full update scheduled. " + e.getMessage(),
									e);
							failedRegions.add(r);
						}
					}
				}
				updatePass = !updatePass;

				if (fullUpdateTimer > 0) {
					fullUpdateTimer--;
				} else {
					fullUpdateTimer = FULL_UPDATE_TIMER;
				}
			} catch (Exception e) {
				log.warn("", e);
			}
		}
	}

	public void updateRegion(WorldRegion region, boolean fullUpdate, boolean forgetObjects) {
		// synchronized (syncObject)
		{
			Collection<WorldObject> vObj = region.getVisibleObjects().values();
			// synchronized (region.getVisibleObjects())
			{
				for (WorldObject object : vObj) // and for all members in region
				{
					if (object == null || !object.isVisible()) {
						continue; // skip dying objects
					}

					// Some mobs need faster knownlist update
					final boolean aggro = Config.GUARD_ATTACK_AGGRO_MOB && object instanceof GuardInstance ||
							object instanceof Attackable && ((Attackable) object).getEnemyClan() != null;

					if (forgetObjects) {
						object.getKnownList().forgetObjects(aggro || fullUpdate);
						continue;
					}
					for (WorldRegion regi : region.getSurroundingRegions()) {
						if (object instanceof Playable || aggro && regi.isActive() || fullUpdate) {
							Collection<WorldObject> inrObj = regi.getVisibleObjects().values();
							// synchronized (regi.getVisibleObjects())
							{
								for (WorldObject obj : inrObj) {
									if (obj != object) {
										object.getKnownList().addKnownObject(obj);
									}
								}
							}
						} else if (object instanceof Creature) {
							if (regi.isActive()) {
								Collection<Playable> inrPls = regi.getVisiblePlayable().values();
								// synchronized (regi.getVisiblePlayable())
								{
									for (WorldObject obj : inrPls) {
										if (obj != object) {
											object.getKnownList().addKnownObject(obj);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final KnownListUpdateTaskManager instance = new KnownListUpdateTaskManager();
	}
}
