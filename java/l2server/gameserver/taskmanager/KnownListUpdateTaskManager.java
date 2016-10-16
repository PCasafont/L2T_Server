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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2GuardInstance;
import l2server.log.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;

public class KnownListUpdateTaskManager
{

	private static final int FULL_UPDATE_TIMER = 100;
	public static boolean updatePass = true;

	// Do full update every FULL_UPDATE_TIMER * KNOWNLIST_UPDATE_INTERVAL
	public static int _fullUpdateTimer = FULL_UPDATE_TIMER;

	private static final HashSet<L2WorldRegion> _failedRegions = new HashSet<>(1);

	private KnownListUpdateTaskManager()
	{
		ThreadPoolManager.getInstance()
				.scheduleAiAtFixedRate(new KnownListUpdate(), 1000, Config.KNOWNLIST_UPDATE_INTERVAL);
	}

	public static KnownListUpdateTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private class KnownListUpdate implements Runnable
	{
		public KnownListUpdate()
		{
		}

		@Override
		public void run()
		{
			try
			{
				boolean failed;
				for (L2WorldRegion regions[] : L2World.getInstance().getAllWorldRegions())
				{
					for (L2WorldRegion r : regions) // go through all world regions
					{
						// avoid stopping update if something went wrong in updateRegion()
						try
						{
							failed = _failedRegions.contains(r); // failed on last pass
							if (r.isActive()) // and check only if the region is active
							{
								updateRegion(r, _fullUpdateTimer == FULL_UPDATE_TIMER || failed, updatePass);
							}
							if (failed)
							{
								_failedRegions.remove(r); // if all ok, remove
							}
						}
						catch (Exception e)
						{
							Log.log(Level.WARNING,
									"KnownListUpdateTaskManager: updateRegion(" + _fullUpdateTimer + "," + updatePass +
											") failed for region " + r.getName() + ". Full update scheduled. " +
											e.getMessage(), e);
							_failedRegions.add(r);
						}
					}
				}
				updatePass = !updatePass;

				if (_fullUpdateTimer > 0)
				{
					_fullUpdateTimer--;
				}
				else
				{
					_fullUpdateTimer = FULL_UPDATE_TIMER;
				}
			}
			catch (Exception e)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	public void updateRegion(L2WorldRegion region, boolean fullUpdate, boolean forgetObjects)
	{
		// synchronized (syncObject)
		{
			Collection<L2Object> vObj = region.getVisibleObjects().values();
			// synchronized (region.getVisibleObjects())
			{
				for (L2Object object : vObj) // and for all members in region
				{
					if (object == null || !object.isVisible())
					{
						continue; // skip dying objects
					}

					// Some mobs need faster knownlist update
					final boolean aggro = Config.GUARD_ATTACK_AGGRO_MOB && object instanceof L2GuardInstance ||
							object instanceof L2Attackable && ((L2Attackable) object).getEnemyClan() != null;

					if (forgetObjects)
					{
						object.getKnownList().forgetObjects(aggro || fullUpdate);
						continue;
					}
					for (L2WorldRegion regi : region.getSurroundingRegions())
					{
						if (object instanceof L2Playable || aggro && regi.isActive() || fullUpdate)
						{
							Collection<L2Object> inrObj = regi.getVisibleObjects().values();
							// synchronized (regi.getVisibleObjects())
							{
								for (L2Object _object : inrObj)
								{
									if (_object != object)
									{
										object.getKnownList().addKnownObject(_object);
									}
								}
							}
						}
						else if (object instanceof L2Character)
						{
							if (regi.isActive())
							{
								Collection<L2Playable> inrPls = regi.getVisiblePlayable().values();
								// synchronized (regi.getVisiblePlayable())
								{
									for (L2Object _object : inrPls)
									{
										if (_object != object)
										{
											object.getKnownList().addKnownObject(_object);
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
	private static class SingletonHolder
	{
		protected static final KnownListUpdateTaskManager _instance = new KnownListUpdateTaskManager();
	}
}
