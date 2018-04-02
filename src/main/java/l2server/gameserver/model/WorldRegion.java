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

package l2server.gameserver.model;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.actor.*;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.model.zone.type.DerbyTrackZone;
import l2server.gameserver.model.zone.type.PeaceZone;
import l2server.gameserver.model.zone.type.TownZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public final class WorldRegion {
	private static Logger log = LoggerFactory.getLogger(WorldRegion.class.getName());
	
	/**
	 * L2ObjectHashSet(L2PlayableInstance) containing L2PlayableInstance of all player & summon in game in this WorldRegion
	 */
	private Map<Integer, Playable> allPlayable;
	
	/**
	 * L2ObjectHashSet(WorldObject) containing WorldObject visible in this WorldRegion
	 */
	private Map<Integer, WorldObject> visibleObjects;
	
	private List<WorldRegion> surroundingRegions;
	private int tileX, tileY;
	private boolean active = false;
	private ScheduledFuture<?> neighborsTask = null;
	private final ArrayList<ZoneType> zones;
	
	public WorldRegion(int pTileX, int pTileY) {
		allPlayable = new ConcurrentHashMap<>();
		visibleObjects = new ConcurrentHashMap<>();
		surroundingRegions = new ArrayList<>();
		
		tileX = pTileX;
		tileY = pTileY;
		
		// default a newly initialized region to inactive, unless always on is specified
		active = Config.GRIDS_ALWAYS_ON;
		zones = new ArrayList<>();
	}
	
	public ArrayList<ZoneType> getZones() {
		return zones;
	}
	
	public void addZone(ZoneType zone) {
		zones.add(zone);
	}
	
	public void removeZone(ZoneType zone) {
		zones.remove(zone);
	}
	
	public void revalidateZones(Creature character) {
		// do NOT update the world region while the character is still in the process of teleporting
		// Once the teleport is COMPLETED, revalidation occurs safely, at that time.
		
		if (character.isTeleporting()) {
			return;
		}
		
		for (ZoneType z : getZones()) {
			if (z != null) {
				z.revalidateInZone(character);
			}
		}
	}
	
	public void removeFromZones(Creature character) {
		for (ZoneType z : getZones()) {
			if (z != null) {
				z.removeCharacter(character);
			}
		}
	}
	
	public boolean containsZone(int zoneId) {
		for (ZoneType z : getZones()) {
			if (z.getId() == zoneId) {
				return true;
			}
		}
		return false;
	}
	
	public boolean checkEffectRangeInsidePeaceZone(Skill skill, final int x, final int y, final int z) {
		final int range = skill.getEffectRange();
		final int up = y + range;
		final int down = y - range;
		final int left = x + range;
		final int right = x - range;
		
		for (ZoneType e : getZones()) {
			if (e instanceof TownZone && ((TownZone) e).isPeaceZone() || e instanceof DerbyTrackZone || e instanceof PeaceZone) {
				if (e.isInsideZone(x, up, z)) {
					return false;
				}
				
				if (e.isInsideZone(x, down, z)) {
					return false;
				}
				
				if (e.isInsideZone(left, y, z)) {
					return false;
				}
				
				if (e.isInsideZone(right, y, z)) {
					return false;
				}
				
				if (e.isInsideZone(x, y, z)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void onDeath(Creature character, Creature killer) {
		for (ZoneType z : getZones()) {
			if (z != null) {
				z.onDieInside(character, killer);
			}
		}
	}
	
	public void onRevive(Creature character) {
		for (ZoneType z : getZones()) {
			if (z != null) {
				z.onReviveInside(character);
			}
		}
	}
	
	/**
	 * Task of AI notification
	 */
	public class NeighborsTask implements Runnable {
		private boolean isActivating;
		
		public NeighborsTask(boolean isActivating) {
			this.isActivating = isActivating;
		}
		
		@Override
		public void run() {
			if (isActivating) {
				// for each neighbor, if it's not active, activate.
				for (WorldRegion neighbor : getSurroundingRegions()) {
					neighbor.setActive(true);
				}
			} else {
				if (areNeighborsEmpty()) {
					setActive(false);
				}
				
				// check and deactivate
				for (WorldRegion neighbor : getSurroundingRegions()) {
					if (neighbor.areNeighborsEmpty()) {
						neighbor.setActive(false);
					}
				}
			}
		}
	}
	
	private void switchAI(boolean isOn) {
		int c = 0;
		if (!isOn) {
			Collection<WorldObject> vObj = visibleObjects.values();
			//synchronized (visibleObjects)
			{
				for (WorldObject o : vObj) {
					if (o instanceof Attackable) {
						c++;
						Attackable mob = (Attackable) o;
						
						// Set target to null and cancel Attack or Cast
						mob.setTarget(null);
						
						// Stop movement
						mob.stopMove(null);
						
						// Stop all active skills effects in progress on the Creature
						mob.stopAllEffects();
						
						mob.clearAggroList();
						mob.getAttackByList().clear();
						mob.getKnownList().removeAllKnownObjects();
						
						// stop the ai tasks
						if (mob.hasAI()) {
							mob.getAI().setIntention(l2server.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE);
							mob.getAI().stopAITask();
						}
					} else if (o instanceof Vehicle) {
						c++;
						((Vehicle) o).getKnownList().removeAllKnownObjects();
					}
				}
			}
			log.debug(c + " mobs were turned off");
		} else {
			Collection<WorldObject> vObj = visibleObjects.values();
			//synchronized (visibleObjects)
			{
				for (WorldObject o : vObj) {
					if (o instanceof Attackable) {
						c++;
						// Start HP/MP/CP Regeneration task
						((Attackable) o).getStatus().startHpMpRegeneration();
					} else if (o instanceof Npc) {
						((Npc) o).startRandomAnimationTimer();
					}
				}
			}
			//KnownListUpdateTaskManager.getInstance().updateRegion(this, true, true);
			log.debug(c + " mobs were turned on");
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	// check if all 9 neighbors (including self) are inactive or active but with no players.
	// returns true if the above condition is met.
	public boolean areNeighborsEmpty() {
		// if this region is occupied, return false.
		if (isActive() && !allPlayable.isEmpty()) {
			return false;
		}
		
		// if any one of the neighbors is occupied, return false
		for (WorldRegion neighbor : surroundingRegions) {
			if (neighbor.isActive() && !neighbor.allPlayable.isEmpty()) {
				return false;
			}
		}
		
		// in all other cases, return true.
		return true;
	}
	
	/**
	 * this function turns this region's AI and geodata on or off
	 *
	 */
	public void setActive(boolean value) {
		if (active == value) {
			return;
		}
		
		active = value;
		
		// turn the AI on or off to match the region's activation.
		switchAI(value);
		
		// TODO
		// turn the geodata on or off to match the region's activation.
		if (value) {
			log.debug("Starting Grid " + tileX + "," + tileY);
		} else {
			log.debug("Stoping Grid " + tileX + "," + tileY);
		}
	}
	
	/**
	 * Immediately sets self as active and starts a timer to set neighbors as active
	 * this timer is to avoid turning on neighbors in the case when a person just
	 * teleported into a region and then teleported out immediately...there is no
	 * reason to activate all the neighbors in that case.
	 */
	private void startActivation() {
		// first set self to active and do self-tasks...
		setActive(true);
		
		// if the timer to deactivate neighbors is running, cancel it.
		synchronized (this) {
			if (neighborsTask != null) {
				neighborsTask.cancel(true);
				neighborsTask = null;
			}
			
			// then, set a timer to activate the neighbors
			neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
		}
	}
	
	/**
	 * starts a timer to set neighbors (including self) as inactive
	 * this timer is to avoid turning off neighbors in the case when a person just
	 * moved out of a region that he may very soon return to.  There is no reason
	 * to turn self & neighbors off in that case.
	 */
	private void startDeactivation() {
		// if the timer to activate neighbors is running, cancel it.
		synchronized (this) {
			if (neighborsTask != null) {
				neighborsTask.cancel(true);
				neighborsTask = null;
			}
			
			// start a timer to "suggest" a deactivate to self and neighbors.
			// suggest means: first check if a neighbor has L2PcInstances in it.  If not, deactivate.
			neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
		}
	}
	
	/**
	 * Add the WorldObject in the L2ObjectHashSet(WorldObject) visibleObjects containing WorldObject visible in this WorldRegion <BR>
	 * If WorldObject is a Player, Add the Player in the L2ObjectHashSet(Player) allPlayable
	 * containing Player of all player in game in this WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this
	 */
	public void addVisibleObject(WorldObject object) {
		if (object == null) {
			return;
		}
		
		assert object.getWorldRegion() == this;
		
		visibleObjects.put(object.getObjectId(), object);
		
		if (object instanceof Playable) {
			allPlayable.put(object.getObjectId(), (Playable) object);
			
			// if this is the first player to enter the region, activate self & neighbors
			if (allPlayable.size() == 1 && !Config.GRIDS_ALWAYS_ON) {
				startActivation();
			}
		}
	}
	
	/**
	 * Remove the WorldObject from the L2ObjectHashSet(WorldObject) visibleObjects in this WorldRegion <BR><BR>
	 * <p>
	 * If WorldObject is a Player, remove it from the L2ObjectHashSet(Player) allPlayable of this WorldRegion <BR>
	 * Assert : object.getCurrentWorldRegion() == this || object.getCurrentWorldRegion() == null
	 */
	public void removeVisibleObject(WorldObject object) {
		if (object == null) {
			return;
		}
		
		assert object.getWorldRegion() == this || object.getWorldRegion() == null;
		
		visibleObjects.remove(object.getObjectId());
		
		if (object instanceof Playable) {
			allPlayable.remove(object.getObjectId());
			
			if (allPlayable.isEmpty() && !Config.GRIDS_ALWAYS_ON) {
				startDeactivation();
			}
		}
	}
	
	public void addSurroundingRegion(WorldRegion region) {
		surroundingRegions.add(region);
	}
	
	/**
	 * Return the ArrayList surroundingRegions containing all WorldRegion around the current WorldRegion
	 */
	public List<WorldRegion> getSurroundingRegions() {
		return surroundingRegions;
	}
	
	public Map<Integer, Playable> getVisiblePlayable() {
		return allPlayable;
	}
	
	public Map<Integer, WorldObject> getVisibleObjects() {
		return visibleObjects;
	}
	
	public String getName() {
		return "(" + tileX + ", " + tileY + ")";
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public void deleteVisibleNpcSpawns() {
		log.debug("Deleting all visible NPC's in Region: " + getName());
		Collection<WorldObject> vNPC = visibleObjects.values();
		//synchronized (visibleObjects)
		{
			for (WorldObject obj : vNPC) {
				if (obj instanceof Npc) {
					Npc target = (Npc) obj;
					target.deleteMe();
					L2Spawn spawn = target.getSpawn();
					if (spawn != null) {
						spawn.stopRespawn();
						SpawnTable.getInstance().deleteSpawn(spawn, false);
					}
					log.trace("Removed NPC " + target.getObjectId());
				}
			}
		}
		log.info("All visible NPC's deleted in Region: " + getName());
	}
}
