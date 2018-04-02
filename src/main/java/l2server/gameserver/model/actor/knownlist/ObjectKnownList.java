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

import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.util.Util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectKnownList {
	private WorldObject activeObject;
	private Map<Integer, WorldObject> knownObjects;

	public ObjectKnownList(WorldObject activeObject) {
		this.activeObject = activeObject;
	}

	public boolean addKnownObject(WorldObject object) {
		if (object == null) {
			return false;
		}

		// Instance -1 is for GMs that can see everything on all instances
		if (getActiveObject().getInstanceId() != -1 && object.getInstanceId() != getActiveObject().getInstanceId() &&
				object.getInstanceId() != getActiveObject().getObjectId()) {
			return false;
		}

		// Check if already know object
		if (knowsObject(object)) {
			return false;
		}

		// Check if object is not inside distance to watch object
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), getActiveObject(), object, true)) {
			return false;
		}

		return getKnownObjects().put(object.getObjectId(), object) == null;
	}

	public final boolean knowsObject(WorldObject object) {
		if (object == null) {
			return false;
		}

		return getActiveObject() == object || getKnownObjects().containsKey(object.getObjectId());
	}

	/**
	 * Remove all WorldObject from knownObjects
	 */
	public void removeAllKnownObjects() {
		getKnownObjects().clear();
	}

	public final boolean removeKnownObject(WorldObject object) {
		return removeKnownObject(object, false);
	}

	protected boolean removeKnownObject(WorldObject object, boolean forget) {
		if (object == null) {
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
	public final void findObjects() {
		final WorldRegion region = getActiveObject().getWorldRegion();
		if (region == null) {
			return;
		}

		if (getActiveObject() instanceof Playable) {
			for (WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				Collection<WorldObject> vObj = regi.getVisibleObjects().values();
				//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
				{
					//synchronized (regi.getVisibleObjects())
					{
						for (WorldObject object : vObj) {
							if (object != getActiveObject()) {
								addKnownObject(object);
								if (object instanceof Creature) {
									object.getKnownList().addKnownObject(getActiveObject());
								}
							}
						}
					}
				}
			}
		} else if (getActiveObject() instanceof Creature) {
			for (WorldRegion regi : region.getSurroundingRegions()) // offer members of this and surrounding regions
			{
				if (regi.isActive()) {
					Collection<Playable> vPls = regi.getVisiblePlayable().values();
					//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
					{
						//synchronized (regi.getVisiblePlayable())
						{
							for (WorldObject object : vPls) {
								if (object != getActiveObject()) {
									addKnownObject(object);
								}
							}
						}
					}
				}
			}
		}
	}

	// Remove invisible and too far WorldObject from knowObject and if necessary from knownPlayers of the Creature
	public void forgetObjects(boolean fullCheck) {
		//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
		{
			// Go through knownObjects
			final Collection<WorldObject> objs = getKnownObjects().values();
			final Iterator<WorldObject> oIter = objs.iterator();
			WorldObject object;
			//synchronized (getKnownObjects())
			{
				while (oIter.hasNext()) {
					object = oIter.next();
					if (object == null) {
						oIter.remove();
						continue;
					}

					if (!fullCheck && !(object instanceof Playable)) {
						continue;
					}

					// Remove all objects invisible or too far
					if (!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true)) {
						oIter.remove();
						removeKnownObject(object, true);
					}
				}
			}
		}
	}

	public WorldObject getActiveObject() {
		return activeObject;
	}

	public int getDistanceToForgetObject(WorldObject object) {
		return 0;
	}

	public int getDistanceToWatchObject(WorldObject object) {
		return 0;
	}

	/**
	 * Return the knownObjects containing all WorldObject known by the Creature.
	 */
	public final Map<Integer, WorldObject> getKnownObjects() {
		if (knownObjects == null) {
			knownObjects = new ConcurrentHashMap<>();
		}
		return knownObjects;
	}
}
