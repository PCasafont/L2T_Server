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

package l2server.gameserver.model.actor.knownlist;

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2WorldRegion;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.util.Util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectKnownList
{
	private L2Object _activeObject;
	private Map<Integer, L2Object> _knownObjects;

	public ObjectKnownList(L2Object activeObject)
	{
		_activeObject = activeObject;
	}

	public boolean addKnownObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}

		// Instance -1 is for GMs that can see everything on all instances
		if (getActiveObject().getInstanceId() != -1 && object.getInstanceId() != getActiveObject().getInstanceId() &&
				object.getInstanceId() != getActiveObject().getObjectId())
		{
			return false;
		}

		// Check if already know object
		if (knowsObject(object))
		{
			return false;
		}

		// Check if object is not inside distance to watch object
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true))
		{
			return false;
		}

		return getKnownObjects().put(object.getObjectId(), object) == null;
	}

	public final boolean knowsObject(L2Object object)
	{
		if (object == null)
		{
			return false;
		}

		return getActiveObject() == object || getKnownObjects().containsKey(object.getObjectId());
	}

	/**
	 * Remove all L2Object from _knownObjects
	 */
	public void removeAllKnownObjects()
	{
		getKnownObjects().clear();
	}

	public final boolean removeKnownObject(L2Object object)
	{
		return removeKnownObject(object, false);
	}

	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if (object == null)
		{
			return false;
		}

		if (forget) // on forget objects removed from list by iterator
		{
			return true;
		}

		return getKnownObjects().remove(object.getObjectId()) != null;
	}

	// used only in Config.MOVE_BASED_KNOWNLIST and does not support guards seeing
	// moving monsters
	public final void findObjects()
	{
		final L2WorldRegion region = getActiveObject().getWorldRegion();
		if (region == null)
		{
			return;
		}

		if (getActiveObject() instanceof L2Playable)
		{
			for (L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				Collection<L2Object> vObj = regi.getVisibleObjects().values();
				//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
				{
					//synchronized (regi.getVisibleObjects())
					{
						for (L2Object _object : vObj)
						{
							if (_object != getActiveObject())
							{
								addKnownObject(_object);
								if (_object instanceof L2Character)
								{
									_object.getKnownList().addKnownObject(getActiveObject());
								}
							}
						}
					}
				}
			}
		}
		else if (getActiveObject() instanceof L2Character)
		{
			for (L2WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				if (regi.isActive())
				{
					Collection<L2Playable> vPls = regi.getVisiblePlayable().values();
					//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
					{
						//synchronized (regi.getVisiblePlayable())
						{
							for (L2Object _object : vPls)
							{
								if (_object != getActiveObject())
								{
									addKnownObject(_object);
								}
							}
						}
					}
				}
			}
		}
	}

	// Remove invisible and too far L2Object from _knowObject and if necessary from _knownPlayers of the L2Character
	public void forgetObjects(boolean fullCheck)
	{
		//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
		{
			// Go through knownObjects
			final Collection<L2Object> objs = getKnownObjects().values();
			final Iterator<L2Object> oIter = objs.iterator();
			L2Object object;
			//synchronized (getKnownObjects())
			{
				while (oIter.hasNext())
				{
					object = oIter.next();
					if (object == null)
					{
						oIter.remove();
						continue;
					}

					if (!fullCheck && !(object instanceof L2Playable))
					{
						continue;
					}

					// Remove all objects invisible or too far
					if (!object.isVisible() ||
							!Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object,
									true))
					{
						oIter.remove();
						removeKnownObject(object, true);
					}
				}
			}
		}
	}

	public L2Object getActiveObject()
	{
		return _activeObject;
	}

	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}

	/**
	 * Return the _knownObjects containing all L2Object known by the L2Character.
	 */
	public final Map<Integer, L2Object> getKnownObjects()
	{
		if (_knownObjects == null)
		{
			_knownObjects = new ConcurrentHashMap<>();
		}
		return _knownObjects;
	}
}
