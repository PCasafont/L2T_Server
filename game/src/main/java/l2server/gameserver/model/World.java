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
import l2server.gameserver.GmListTable;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.instancemanager.GrandBossManager;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.ApInstance;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.olympiad.OlympiadManager;
import l2server.util.Point3D;
import l2server.util.StringUtil;
import l2server.util.loader.annotations.Load;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class ...
 *
 * @version $Revision: 1.21.2.5.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class World {
	
	private static Logger log = LoggerFactory.getLogger(World.class.getName());
	
	/**
	 * Gracia border
	 * Flying objects not allowed to the east of it.
	 */
	public static final int GRACIA_MAX_X = -166168;
	public static final int GRACIA_MAX_Z = 6105;
	public static final int GRACIA_MIN_Z = -895;
	
	/*
	 * biteshift, defines number of regions
	 * note, shifting by 15 will result in regions corresponding to map tiles
	 * shifting by 12 divides one tile to 8x8 regions
	 */
	public static final int SHIFT_BY = 12;
	
	private static final int TILE_SIZE = 32768;
	
	/**
	 * Map dimensions
	 */
	public static final int MAP_MIN_X = (Config.WORLD_X_MIN - 20) * TILE_SIZE;
	public static final int MAP_MAX_X = (Config.WORLD_X_MAX - 19) * TILE_SIZE;
	public static final int MAP_MIN_Y = (Config.WORLD_Y_MIN - 18) * TILE_SIZE;
	public static final int MAP_MAX_Y = (Config.WORLD_Y_MAX - 17) * TILE_SIZE;
	
	/**
	 * calculated offset used so top left region is 0,0
	 */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	/**
	 * number of regions
	 */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	//private HashMap<String, Player> allGms;
	
	/**
	 * HashMap(Integer Player id, Player) containing all the players in game
	 */
	private Map<Integer, Player> allPlayers = new ConcurrentHashMap<>();
	
	/**
	 * L2ObjectHashMap(WorldObject) containing all visible objects
	 */
	private Map<Integer, WorldObject> allObjects = new ConcurrentHashMap<>();
	
	/**
	 * List with the pets instances and their owner id
	 */
	private Map<Integer, PetInstance> petsInstance = new ConcurrentHashMap<>();
	
	private WorldRegion[][] worldRegions;
	
	/**
	 * Constructor of World.<BR><BR>
	 */
	private World() {
	}
	
	/**
	 * Return the current instance of World.<BR><BR>
	 */
	public static World getInstance() {
		return SingletonHolder.instance;
	}
	
	/**
	 * Add WorldObject object in allObjects.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Withdraw an item from the warehouse, create an item</li>
	 * <li> Spawn a Creature (PC, NPC, Pet)</li><BR>
	 */
	public void storeObject(WorldObject object) {
		assert !allObjects.containsKey(object.getObjectId());
		
		if (allObjects.containsKey(object.getObjectId())) {
			if (!Config.isServer(Config.TENKAI)) {
				log.warn("[World] object: " + object + " already exist in OID map!");
				log.error(StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
			}
			return;
		}
		
		allObjects.put(object.getObjectId(), object);
	}
	
	public long timeStoreObject(WorldObject object) {
		long time = System.nanoTime();
		allObjects.put(object.getObjectId(), object);
		time = System.nanoTime() - time;
		return time;
	}
	
	/**
	 * Remove WorldObject object from allObjects of World.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li> Crystallize item</li>
	 * <li> Remove NPC/PC/Pet from the world</li><BR>
	 *
	 * @param object WorldObject to remove from allObjects of World
	 */
	public void removeObject(WorldObject object) {
		allObjects.remove(object.getObjectId()); // suggestion by whatev
		//IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	public void removeObjects(List<WorldObject> list) {
		for (WorldObject o : list) {
			if (o != null) {
				allObjects.remove(o.getObjectId()); // suggestion by whatev
			}
		}
		//IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	public void removeObjects(WorldObject[] objects) {
		for (WorldObject o : objects) {
			allObjects.remove(o.getObjectId()); // suggestion by whatev
		}
		//IdFactory.getInstance().releaseId(object.getObjectId());
	}
	
	public long timeRemoveObject(WorldObject object) {
		long time = System.nanoTime();
		allObjects.remove(object.getObjectId());
		time = System.nanoTime() - time;
		return time;
	}
	
	/**
	 * Return the WorldObject object that belongs to an ID or null if no object found.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li><BR>
	 *
	 * @param oID Identifier of the WorldObject
	 */
	public WorldObject findObject(int oID) {
		return allObjects.get(oID);
	}
	
	public long timeFindObject(int objectID) {
		long time = System.nanoTime();
		allObjects.get(objectID);
		time = System.nanoTime() - time;
		return time;
	}
	
	/**
	 * Added by Tempy - 08 Aug 05
	 * Allows easy retrevial of all visible objects in world.
	 * <p>
	 * -- do not use that function, it's unsafe!
	 *
	 * @deprecated
	 */
	@Deprecated
	public final Map<Integer, WorldObject> getAllVisibleObjects() {
		return allObjects;
	}
	
	/**
	 * Get the count of all visible objects in world.<br><br>
	 *
	 * @return count off all World objects
	 */
	public final int getAllVisibleObjectsCount() {
		return allObjects.size();
	}
	
	/**
	 * Return a table containing all GMs.<BR><BR>
	 */
	public ArrayList<Player> getAllGMs() {
		return GmListTable.getInstance().getAllGms(true);
	}
	
	public Map<Integer, Player> getAllPlayers() {
		return allPlayers;
	}
	
	public final Collection<Player> getAllPlayersArray() {
		return allPlayers.values();
	}
	
	/**
	 * Return how many players are online.<BR><BR>
	 *
	 * @return number of online players.
	 */
	public int getAllPlayersCount() {
		return allPlayers.size();
	}
	
	/**
	 * Return the player instance corresponding to the given name.<BR><BR>
	 * <B>If you have access to player objectId use {@link #getPlayer(int playerObjId)}</B>
	 * <BR>
	 *
	 * @param name Name of the player to get Instance
	 */
	public Player getPlayer(String name) {
		return getPlayer(CharNameTable.getInstance().getIdByName(name));
	}
	
	/**
	 * Return the player instance corresponding to the given object ID.<BR><BR>
	 *
	 * @param playerObjId Object ID of the player to get Instance
	 */
	public Player getPlayer(int playerObjId) {
		return allPlayers.get(playerObjId);
	}
	
	/**
	 * Return the pet instance from the given ownerId.<BR><BR>
	 *
	 * @param ownerId ID of the owner
	 */
	public PetInstance getPet(int ownerId) {
		return petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.<BR><BR>
	 *
	 * @param ownerId ID of the owner
	 * @param pet     PetInstance of the pet
	 */
	public PetInstance addPet(int ownerId, PetInstance pet) {
		return petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.<BR><BR>
	 *
	 * @param ownerId ID of the owner
	 */
	public void removePet(int ownerId) {
		petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.<BR><BR>
	 *
	 * @param pet the pet to remove
	 */
	public void removePet(PetInstance pet) {
		petsInstance.remove(pet.getOwner().getObjectId());
	}
	
	/**
	 * Add a WorldObject in the world.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * WorldObject (including Player) are identified in <B>visibleObjects</B> of his current WorldRegion and in <B>knownObjects</B> of other surrounding L2Characters <BR>
	 * Player are identified in <B>allPlayers</B> of World, in <B>allPlayers</B> of his current WorldRegion and in <B>knownPlayer</B> of other surrounding L2Characters <BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the WorldObject object in allPlayers* of World </li>
	 * <li>Add the WorldObject object in gmList** of GmListTable </li>
	 * <li>Add object in knownObjects and knownPlayer* of all surrounding WorldRegion L2Characters </li><BR>
	 * <p>
	 * <li>If object is a Creature, add all surrounding WorldObject in its knownObjects and all surrounding Player in its knownPlayer </li><BR>
	 * <p>
	 * <I>*  only if object is a Player</I><BR>
	 * <I>** only if object is a GM Player</I><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in visibleObjects and allPlayers* of WorldRegion (need synchronisation)</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to allObjects and allPlayers* of World (need synchronisation)</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop an Item </li>
	 * <li> Spawn a Creature</li>
	 * <li> Apply Death Penalty of a Player </li><BR><BR>
	 *
	 * @param object L2object to add in the world
	 */
	public void addVisibleObject(WorldObject object, WorldRegion newRegion) {
		if (!allObjects.containsKey(object.getObjectId())) {
			allObjects.put(object.getObjectId(), object);
		}
		
		// If selected WorldObject is a L2PcIntance, add it in L2ObjectHashSet(Player) allPlayers of World
		// XXX TODO: this code should be obsoleted by protection in putObject func...
		if (object instanceof Player) {
			Player player = (Player) object;
			
			if (!player.isTeleporting()) {
				Player tmp = allPlayers.get(player.getObjectId());
				if (tmp != null) {
					log.warn("Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.logout();
					tmp.logout();
					return;
				}
				allPlayers.put(player.getObjectId(), player);
			}
		}
		
		if (!newRegion.isActive()) {
			return;
		}
		
		// Get all visible objects contained in the visibleObjects of L2WorldRegions
		// in a circular area of 2000 units
		List<WorldObject> visibles = getVisibleObjects(object, 2000);
		if (Config.DEBUG) {
			log.trace("objects in range:" + visibles.size());
		}
		
		// tell the player about the surroundings
		// Go through the visible objects contained in the circular area
		for (WorldObject visible : visibles) {
			if (visible == null) {
				continue;
			}
			
			// Add the object in L2ObjectHashSet(WorldObject) knownObjects of the visible Creature according to conditions :
			//   - Creature is visible
			//   - object is not already known
			//   - object is in the watch distance
			// If WorldObject is a Player, add WorldObject in L2ObjectHashSet(Player) knownPlayer of the visible Creature
			visible.getKnownList().addKnownObject(object);
			
			// Add the visible WorldObject in L2ObjectHashSet(WorldObject) knownObjects of the object according to conditions
			// If visible WorldObject is a Player, add visible WorldObject in L2ObjectHashSet(Player) knownPlayer of the object
			object.getKnownList().addKnownObject(visible);
		}
	}
	
	/**
	 * Add the Player to allPlayers of World.<BR><BR>
	 */
	public void addToAllPlayers(Player cha) {
		allPlayers.put(cha.getObjectId(), cha);
	}
	
	/**
	 * Remove the Player from allPlayers of World.<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Remove a player fom the visible objects </li><BR>
	 */
	public void removeFromAllPlayers(Player cha) {
		allPlayers.remove(cha.getObjectId());
	}
	
	/**
	 * Remove a WorldObject from the world.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * WorldObject (including Player) are identified in <B>visibleObjects</B> of his current WorldRegion and in <B>knownObjects</B> of other surrounding L2Characters <BR>
	 * Player are identified in <B>allPlayers</B> of World, in <B>allPlayers</B> of his current WorldRegion and in <B>knownPlayer</B> of other surrounding L2Characters <BR><BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the WorldObject object from allPlayers* of World </li>
	 * <li>Remove the WorldObject object from visibleObjects and allPlayers* of WorldRegion </li>
	 * <li>Remove the WorldObject object from gmList** of GmListTable </li>
	 * <li>Remove object from knownObjects and knownPlayer* of all surrounding WorldRegion L2Characters </li><BR>
	 * <p>
	 * <li>If object is a Creature, remove all WorldObject from its knownObjects and all Player from its knownPlayer </li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from allObjects of World</B></FONT><BR><BR>
	 * <p>
	 * <I>*  only if object is a Player</I><BR>
	 * <I>** only if object is a GM Player</I><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Pickup an Item </li>
	 * <li> Decay a Creature</li><BR><BR>
	 *
	 * @param object L2object to remove from the world
	 */
	public void removeVisibleObject(WorldObject object, WorldRegion oldRegion) {
		if (object == null) {
			return;
		}
		
		//removeObject(object);
		
		if (oldRegion != null) {
			// Remove the object from the L2ObjectHashSet(WorldObject) visibleObjects of WorldRegion
			// If object is a Player, remove it from the L2ObjectHashSet(Player) allPlayers of this WorldRegion
			oldRegion.removeVisibleObject(object);
			
			// Go through all surrounding WorldRegion L2Characters
			for (WorldRegion reg : oldRegion.getSurroundingRegions()) {
				//synchronized (KnownListUpdateTaskManager.getInstance().getSync())
				{
					Collection<WorldObject> vObj = reg.getVisibleObjects().values();
					//synchronized (reg.getVisibleObjects())
					{
						for (WorldObject obj : vObj) {
							if (obj != null) {
								obj.getKnownList().removeKnownObject(object);
								object.getKnownList().removeKnownObject(obj);
							}
						}
					}
				}
			}
			
			// If object is a Creature :
			// Remove all WorldObject from L2ObjectHashSet(WorldObject) containing all WorldObject detected by the Creature
			// Remove all Player from L2ObjectHashSet(Player) containing all player ingame detected by the Creature
			object.getKnownList().removeAllKnownObjects();
			
			// If selected WorldObject is a L2PcIntance, remove it from L2ObjectHashSet(Player) allPlayers of World
			if (object instanceof Player) {
				if (!((Player) object).isTeleporting()) {
					removeFromAllPlayers((Player) object);
				}
				
				// If selected WorldObject is a GM Player, remove it from Set(Player) gmList of GmListTable
				//if (((Player)object).isGM())
				//GmListTable.getInstance().deleteGm((Player)object);
			}
		}
	}
	
	/**
	 * Return all visible objects of the WorldRegion object's and of its surrounding WorldRegion.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Find Close Objects for Creature </li><BR>
	 *
	 * @param object L2object that determine the current WorldRegion
	 */
	public List<WorldObject> getVisibleObjects(WorldObject object) {
		WorldRegion reg = object.getWorldRegion();
		
		if (reg == null) {
			return null;
		}
		
		// Create an ArrayList in order to contain all visible WorldObject
		List<WorldObject> result = new ArrayList<>();
		
		// Go through the ArrayList of region
		for (WorldRegion regi : reg.getSurroundingRegions()) {
			// Go through visible objects of the selected region
			Collection<WorldObject> vObj = regi.getVisibleObjects().values();
			//synchronized (regi.getVisibleObjects())
			{
				for (WorldObject _object : vObj) {
					if (_object == null || _object.equals(object)) {
						continue; // skip our own character
					}
					if (!_object.isVisible()) {
						continue; // skip dying objects
					}
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the circular area (radius) centered on the object.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Define the aggrolist of monster </li>
	 * <li> Define visible objects of a WorldObject </li>
	 * <li> Skill : Confusion... </li><BR>
	 *
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the circular area
	 */
	public List<WorldObject> getVisibleObjects(WorldObject object, int radius) {
		if (object == null || !object.isVisible()) {
			return new ArrayList<>();
		}
		
		int x = object.getX();
		int y = object.getY();
		int sqRadius = radius * radius;
		
		// Create an ArrayList in order to contain all visible WorldObject
		List<WorldObject> result = new ArrayList<>();
		
		// Go through the ArrayList of region
		for (WorldRegion regi : object.getWorldRegion().getSurroundingRegions()) {
			// Go through visible objects of the selected region
			Collection<WorldObject> vObj = regi.getVisibleObjects().values();
			//synchronized (regi.getVisibleObjects())
			{
				for (WorldObject obj : vObj) {
					if (obj == null || obj.equals(object)) {
						continue; // skip our own character
					}
					
					// Fix for magically stuck objects
					if (obj.getWorldRegion() == null) {
						regi.removeVisibleObject(obj);
						continue;
					}
					
					int x1 = obj.getX();
					int y1 = obj.getY();
					
					double dx = x1 - x;
					double dy = y1 - y;
					
					if (dx * dx + dy * dy < sqRadius) {
						result.add(obj);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the L2WorldRegions in the spheric area (radius) centered on the object.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Define the target list of a skill </li>
	 * <li> Define the target list of a polearme attack </li><BR><BR>
	 *
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the spheric area
	 */
	public List<WorldObject> getVisibleObjects3D(WorldObject object, int radius) {
		if (object == null || !object.isVisible()) {
			return new ArrayList<>();
		}
		
		int x = object.getX();
		int y = object.getY();
		int z = object.getZ();
		int sqRadius = radius * radius;
		
		// Create an ArrayList in order to contain all visible WorldObject
		List<WorldObject> result = new ArrayList<>();
		
		// Go through visible object of the selected region
		for (WorldRegion regi : object.getWorldRegion().getSurroundingRegions()) {
			Collection<WorldObject> vObj = regi.getVisibleObjects().values();
			//synchronized (regi.getVisibleObjects())
			{
				for (WorldObject _object : vObj) {
					if (_object == null || _object.equals(object)) {
						continue; // skip our own character
					}
					
					int x1 = _object.getX();
					int y1 = _object.getY();
					int z1 = _object.getZ();
					
					long dx = x1 - x;
					long dy = y1 - y;
					long dz = z1 - z;
					
					if (dx * dx + dy * dy + dz * dz < sqRadius) {
						result.add(_object);
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible players of the WorldRegion object's and of its surrounding WorldRegion.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All visible object are identified in <B>visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Find Close Objects for Creature </li><BR>
	 *
	 * @param object L2object that determine the current WorldRegion
	 */
	public List<Playable> getVisiblePlayable(WorldObject object) {
		WorldRegion reg = object.getWorldRegion();
		
		if (reg == null) {
			return null;
		}
		
		// Create an ArrayList in order to contain all visible WorldObject
		List<Playable> result = new ArrayList<>();
		
		// Go through the ArrayList of region
		for (WorldRegion regi : reg.getSurroundingRegions()) {
			// Create an Iterator to go through the visible WorldObject of the WorldRegion
			Map<Integer, Playable> allpls = regi.getVisiblePlayable();
			Collection<Playable> playables = allpls.values();
			// Go through visible object of the selected region
			//synchronized (allpls)
			{
				for (Playable _object : playables) {
					if (_object == null || _object.equals(object)) {
						continue; // skip our own character
					}
					
					if (!_object.isVisible()) // GM invisible is different than this...
					{
						continue; // skip dying objects
					}
					
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Calculate the current L2WorldRegions of the object according to its position (x,y).<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Set position of a new WorldObject (drop, spawn...) </li>
	 * <li> Update position of a WorldObject after a mouvement </li><BR>
	 */
	public WorldRegion getRegion(Point3D point) {
		return worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	public WorldRegion getRegion(int x, int y) {
		return worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Returns the whole 2d array containing the world regions
	 * used by ZoneData.java to setup zones inside the world regions
	 *
	 */
	public WorldRegion[][] getAllWorldRegions() {
		return worldRegions;
	}
	
	/**
	 * Check if the current L2WorldRegions of the object is valid according to its position (x,y).<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Init L2WorldRegions </li><BR>
	 *
	 * @param x X position of the object
	 * @param y Y position of the object
	 * @return True if the WorldRegion is valid
	 */
	private boolean validRegion(int x, int y) {
		return x >= 0 && x <= REGIONS_X && y >= 0 && y <= REGIONS_Y;
	}
	
	/**
	 * Init each WorldRegion and their surrounding table.<BR><BR>
	 * <p>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All surrounding WorldRegion are identified in <B>surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Constructor of World </li><BR>
	 */
	@Load
	public void initRegions() {
		worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		
		for (int i = 0; i <= REGIONS_X; i++) {
			for (int j = 0; j <= REGIONS_Y; j++) {
				worldRegions[i][j] = new WorldRegion(i, j);
			}
		}
		
		for (int x = 0; x <= REGIONS_X; x++) {
			for (int y = 0; y <= REGIONS_Y; y++) {
				for (int a = -1; a <= 1; a++) {
					for (int b = -1; b <= 1; b++) {
						if (validRegion(x + a, y + b)) {
							worldRegions[x + a][y + b].addSurroundingRegion(worldRegions[x][y]);
						}
					}
				}
			}
		}
		
		log.info("World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public void deleteVisibleNpcSpawns() {
		log.info("Deleting all visible NPC's.");
		for (int i = 0; i <= REGIONS_X; i++) {
			for (int j = 0; j <= REGIONS_Y; j++) {
				worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		log.info("All visible NPC's deleted.");
	}
	
	public Player getMostPvP(boolean parties, boolean artificial) {
		Player mostPvP = null;
		int max = -1;
		for (Player flagged : getAllPlayers().values()) {
			if (flagged.getPvpFlag() == 0 || flagged.isGM() || flagged.isInsideZone(CreatureZone.ZONE_PEACE) ||
					flagged.isInsideZone(CreatureZone.ZONE_SIEGE) || flagged.isInsideZone(CreatureZone.ZONE_NOSUMMONFRIEND) ||
					flagged.getInstanceId() != 0 || GrandBossManager.getInstance().getZone(flagged) != null) {
				continue;
			}
			
			boolean valid = true;
			
			int count = 0;
			for (Player pl : flagged.getKnownList().getKnownPlayers().values()) {
				if (pl.getPvpFlag() > 0 && !pl.isInsideZone(CreatureZone.ZONE_PEACE)) {
					if (!parties && pl.isInParty() || !artificial && pl instanceof ApInstance) {
						valid = false;
						break;
					}
					
					count++;
				}
			}
			
			if (valid && count > max) {
				max = count;
				mostPvP = flagged;
			}
		}
		
		return mostPvP;
	}
	
	public List<Player> getAllPlayerShops() {
		List<Player> shops = new ArrayList<>();
		for (Player player : getAllPlayersArray()) {
			if (player == null || player.isInJail() || !player.isInStoreMode()) {
				continue;
			}
			
			shops.add(player);
		}
		return shops;
	}
	
	public List<Player> getAllOlympiadPlayers() {
		List<Player> players = new ArrayList<>();
		for (Player player : getAllPlayersArray()) {
			if (player == null || player.isInJail()) {
				continue;
			}
			
			if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegisteredInComp(player)) {
				players.add(player);
			}
		}
		return players;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final World instance = new World();
	}
}
