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

import l2server.Config;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldRegion;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.zone.SpawnZone;
import l2server.gameserver.model.zone.ZoneType;
import l2server.gameserver.model.zone.form.ZoneCuboid;
import l2server.gameserver.model.zone.form.ZoneCylinder;
import l2server.gameserver.model.zone.form.ZoneNPoly;
import l2server.gameserver.model.zone.type.ArenaZone;
import l2server.gameserver.model.zone.type.OlympiadStadiumZone;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * This class manages the zones
 *
 * @author durgus
 */
public class ZoneManager {
	private static Logger log = LoggerFactory.getLogger(ZoneManager.class.getName());

	//private final HashMap<Integer, ZoneType> zones = new HashMap<Integer, ZoneType>();
	private final Map<Class<? extends ZoneType>, Map<Integer, ? extends ZoneType>> classZones = new HashMap<>();
	private int lastDynamicId = 300000;
	
	public static ZoneManager getInstance() {
		return SingletonHolder.instance;
	}
	
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	private ZoneManager() {
	}
	
	@SuppressWarnings("deprecation")
	@Reload("zones")
	public void reload() {
		// int zoneCount = 0;
		
		// Get the world regions
		int count = 0;
		WorldRegion[][] worldRegions = World.getInstance().getAllWorldRegions();
		for (WorldRegion[] worldRegion : worldRegions) {
			for (WorldRegion aWorldRegion : worldRegion) {
				aWorldRegion.getZones().clear();
				count++;
			}
		}
		GrandBossManager.getInstance().getZones().clear();
		log.info("Removed zones in " + count + " regions.");
		// Load the zones
		load();
		
		for (WorldObject o : World.getInstance().getAllVisibleObjects().values()) {
			if (o instanceof Creature) {
				((Creature) o).revalidateZone(true);
			}
		}
	}
	
	@Load(dependencies = {ClanHallManager.class, GrandBossManager.class, World.class})
	public void load() {
		log.info("Loading zones...");
		classZones.clear();
		
		// Get the world regions
		WorldRegion[][] worldRegions = World.getInstance().getAllWorldRegions();
		
		// Load the zone xml
		try {
			File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "zones");
			if (!dir.exists()) {
				log.debug("Dir " + dir.getAbsolutePath() + " does not exist");
				return;
			}
			
			// Override zones by any custom folder
			File custom = new File(Config.DATAPACK_ROOT, "/data_" + Config.SERVER_NAME + "/zones");
			if (custom.exists()) {
				dir = custom;
			}
			
			File[] files = dir.listFiles();
			ArrayList<File> hash = new ArrayList<>(files.length);
			for (File f : files) {
				// default file first
				if ("zone.xml".equalsIgnoreCase(f.getName())) {
					hash.add(0, f);
				} else if (f.getName().endsWith(".xml")) {
					hash.add(f);
				}
			}
			
			String zoneName;
			int[][] coords;
			int zoneId, minZ, maxZ;
			String zoneType, zoneShape;
			
			for (File f : hash) {
				XmlDocument doc = new XmlDocument(f);
				XmlNode n = doc.getRoot();
				if (n.getName().equalsIgnoreCase("list")) {
					if (!n.getBool("enabled", false)) {
						continue;
					}
					
					for (XmlNode d : n.getChildren()) {
						if (d.getName().equalsIgnoreCase("zone")) {
							if (d.hasAttribute("id")) {
								zoneId = d.getInt("id");
							} else {
								zoneId = lastDynamicId++;
							}
							
							zoneName = d.getString("name", "");
							
							if (d.hasAttribute("minZ")) {
								minZ = d.getInt("minZ");
							} else {
								log.warn("ZoneData: Missing minZ for zone: " + zoneId + " in file: " + f.getName());
								continue;
							}
							
							if (d.hasAttribute("maxZ")) {
								maxZ = d.getInt("maxZ");
							} else {
								log.warn("ZoneData: Missing maxZ for zone: " + zoneId + " in file: " + f.getName());
								continue;
							}
							
							if (d.hasAttribute("type")) {
								zoneType = d.getString("type");
							} else {
								log.warn("ZoneData: Missing type for zone: " + zoneId + " in file: " + f.getName());
								continue;
							}
							
							if (d.hasAttribute("shape")) {
								zoneShape = d.getString("shape");
							} else {
								log.warn("ZoneData: Missing shape for zone: " + zoneId + " in file: " + f.getName());
								continue;
							}
							
							// Create the zone
							Class<?> newZone;
							try {
								newZone = Class.forName("l2server.gameserver.model.zone.type." + zoneType);
							} catch (ClassNotFoundException e) {
								log.warn("ZoneData: No such zone type: " + zoneType + " in file: " + f.getName());
								continue;
							}
							
							Constructor<?> zoneConstructor = newZone.getConstructor(int.class);
							ZoneType temp = (ZoneType) zoneConstructor.newInstance(zoneId);
							
							// Get the zone shape from sql
							try {
								coords = null;
								int[] point;
								ArrayList<int[]> rs = new ArrayList<>();
								
								// loading from XML first
								for (XmlNode cd : d.getChildren()) {
									if (cd.getName().equalsIgnoreCase("node")) {
										point = new int[2];
										point[0] = cd.getInt("X");
										point[1] = cd.getInt("Y");
										rs.add(point);
									}
								}
								
								coords = rs.toArray(new int[rs.size()][]);
								
								if (coords == null || coords.length == 0) {
									log.warn("ZoneData: missing data for zone: " + zoneId + " in both XML and SQL, file: " + f.getName());
									continue;
								}
								
								// Create this zone. Parsing for cuboids is a
								// bit different than for other polygons
								// cuboids need exactly 2 points to be defined.
								// Other polygons need at least 3 (one per
								// vertex)
								if (zoneShape.equalsIgnoreCase("Cuboid")) {
									if (coords.length == 2) {
										temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
									} else {
										log.warn("ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId + " in file: " + f.getName());
										continue;
									}
								} else if (zoneShape.equalsIgnoreCase("NPoly")) {
									// nPoly needs to have at least 3 vertices
									if (coords.length > 2) {
										final int[] aX = new int[coords.length];
										final int[] aY = new int[coords.length];
										for (int i = 0; i < coords.length; i++) {
											aX[i] = coords[i][0];
											aY[i] = coords[i][1];
										}
										
										int minX = World.MAP_MAX_X;
										int maxX = World.MAP_MIN_X;
										int minY = World.MAP_MAX_Y;
										int maxY = World.MAP_MIN_Y;
										for (int i = 0; i < coords.length; i++) {
											if (aX[i] < minX) {
												minX = aX[i];
											}
											if (aX[i] > maxX) {
												maxX = aX[i];
											}
											if (aY[i] < minY) {
												minY = aY[i];
											}
											if (aY[i] > maxY) {
												maxY = aY[i];
											}
										}
										
										temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ, minX, maxX, minY, maxY));
									} else {
										log.warn("ZoneData: Bad data for zone: " + zoneId + " in file: " + f.getName());
										continue;
									}
								} else if (zoneShape.equalsIgnoreCase("Cylinder")) {
									// A Cylinder zone requires a center point
									// at x,y and a radius
									final int zoneRad = d.getInt("rad");
									if (coords.length == 1 && zoneRad > 0) {
										temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
									} else {
										log.warn("ZoneData: Bad data for zone: " + zoneId + " in file: " + f.getName());
										continue;
									}
								} else {
									log.warn("ZoneData: Unknown shape: " + zoneShape + " in file: " + f.getName());
									continue;
								}
							} catch (Exception e) {
								log.warn("ZoneData: Failed to load zone " + zoneId + " coordinates: " + e.getMessage(), e);
							}
							
							// Check for additional parameters
							for (XmlNode cd : d.getChildren()) {
								if (cd.getName().equalsIgnoreCase("stat")) {
									String name = cd.getString("name");
									String val = cd.getString("val");
									
									temp.setParameter(name, val);
								} else if (cd.getName().equalsIgnoreCase("spawn") && temp instanceof SpawnZone) {
									int spawnX = cd.getInt("X");
									int spawnY = cd.getInt("Y");
									int spawnZ = cd.getInt("Z");
									
									if (cd.getBool("isChaotic", false)) {
										((SpawnZone) temp).addChaoticSpawn(spawnX, spawnY, spawnZ);
									} else if (!cd.getBool("isPvP", false) || Config.isServer(Config.TENKAI)) {
										((SpawnZone) temp).addSpawn(spawnX, spawnY, spawnZ);
									}
								}
							}
							if (checkId(zoneId)) {
								log.debug("Caution: Zone (" + zoneId + ") from file: " + f.getName() + " overrides previous definition.");
							}
							
							if (!zoneName.isEmpty()) {
								temp.setName(zoneName);
							}
							
							addZone(zoneId, temp);
							
							// Register the zone into any world region it
							// intersects with...
							// currently 11136 test for each zone :>
							int ax, ay, bx, by;
							for (int x = 0; x < worldRegions.length; x++) {
								for (int y = 0; y < worldRegions[x].length; y++) {
									ax = x - World.OFFSET_X << World.SHIFT_BY;
									bx = x + 1 - World.OFFSET_X << World.SHIFT_BY;
									ay = y - World.OFFSET_Y << World.SHIFT_BY;
									by = y + 1 - World.OFFSET_Y << World.SHIFT_BY;
									
									if (temp.getZone().intersectsRectangle(ax, bx, ay, by)) {
										if (Config.DEBUG) {
											log.info("Zone (" + zoneId + ") added to: " + x + " " + y);
										}
										worldRegions[x][y].addZone(temp);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error while loading zones.", e);
			return;
		}
		
		log.info("Zone: loaded " + classZones.size() + " zone classes and " + getSize() + " zones.");
	}
	
	public int getSize() {
		int i = 0;
		for (Map<Integer, ? extends ZoneType> map : classZones.values()) {
			i += map.size();
		}
		return i;
	}
	
	public boolean checkId(int id) {
		for (Map<Integer, ? extends ZoneType> map : classZones.values()) {
			if (map.containsKey(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add new zone
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> void addZone(Integer id, T zone) {
		//zones.put(id, zone);
		Map<Integer, T> map = (Map<Integer, T>) classZones.get(zone.getClass());
		if (map == null) {
			map = new LinkedHashMap<>();
			map.put(id, zone);
			classZones.put(zone.getClass(), map);
		} else {
			map.put(id, zone);
		}
	}
	
	/**
	 * Returns all zones registered with the ZoneManager.
	 * To minimise iteration processing retrieve zones from WorldRegion for a specific location instead.
	 *
	 * @return zones
	 * @see #getAllZones(Class)
	 */
	@Deprecated
	public Collection<ZoneType> getAllZones() {
		ArrayList<ZoneType> zones = new ArrayList<>();
		for (Map<Integer, ? extends ZoneType> map : classZones.values()) {
			zones.addAll(map.values());
		}
		return zones;
	}
	
	/**
	 * Return all zones by class type
	 *
	 * @param <T>
	 * @param zoneType Zone class
	 * @return Collection of zones
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> Collection<T> getAllZones(Class<T> zoneType) {
		return (Collection<T>) classZones.get(zoneType).values();
	}
	
	/**
	 * Get zone by ID
	 *
	 * @see #getZoneById(int, Class)
	 */
	public ZoneType getZoneById(int id) {
		for (Map<Integer, ? extends ZoneType> map : classZones.values()) {
			if (map.containsKey(id)) {
				return map.get(id);
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneByName(String name, Class<T> type) {
		for (ZoneType zones : classZones.get(type).values()) {
			if (zones == null) {
				continue;
			}
			if (zones.getName().equalsIgnoreCase(name)) {
				return (T) zones;
			}
		}
		return null;
	}
	
	/**
	 * Get zone by ID and zone class
	 *
	 * @return zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZoneById(int id, Class<T> zoneType) {
		return (T) classZones.get(zoneType).get(id);
	}
	
	/**
	 * Returns all zones from where the object is located
	 *
	 * @return zones
	 */
	public ArrayList<ZoneType> getZones(WorldObject object) {
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	/**
	 * Returns zone from where the object is located by type
	 *
	 * @return zone
	 */
	public <T extends ZoneType> T getZone(WorldObject object, Class<T> type) {
		if (object == null) {
			return null;
		}
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	/**
	 * Returns all zones from given coordinates (plane)
	 *
	 * @return zones
	 */
	public ArrayList<ZoneType> getZones(int x, int y) {
		WorldRegion region = World.getInstance().getRegion(x, y);
		ArrayList<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : region.getZones()) {
			if (zone.isInsideZone(x, y)) {
				temp.add(zone);
			}
		}
		return temp;
	}
	
	/**
	 * Returns all zones from given coordinates
	 *
	 * @return zones
	 */
	public ArrayList<ZoneType> getZones(int x, int y, int z) {
		WorldRegion region = World.getInstance().getRegion(x, y);
		ArrayList<ZoneType> temp = new ArrayList<>();
		for (ZoneType zone : region.getZones()) {
			if (zone.isInsideZone(x, y, z)) {
				temp.add(zone);
			}
		}
		return temp;
	}
	
	/**
	 * Returns zone from given coordinates
	 *
	 * @return zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type) {
		WorldRegion region = World.getInstance().getRegion(x, y);
		for (ZoneType zone : region.getZones()) {
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone)) {
				return (T) zone;
			}
		}
		return null;
	}
	
	public final ArenaZone getArena(Creature character) {
		if (character == null) {
			return null;
		}
		
		for (ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ())) {
			if (temp instanceof ArenaZone && temp.isCharacterInZone(character)) {
				return (ArenaZone) temp;
			}
		}
		
		return null;
	}
	
	public final OlympiadStadiumZone getOlympiadStadium(Creature character) {
		if (character == null) {
			return null;
		}
		
		for (ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ())) {
			if (temp instanceof OlympiadStadiumZone && temp.isCharacterInZone(character)) {
				return (OlympiadStadiumZone) temp;
			}
		}
		return null;
	}
	
	/**
	 * For testing purposes only
	 *
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public <T extends ZoneType> T getClosestZone(WorldObject obj, Class<T> type) {
		T zone = getZone(obj, type);
		if (zone == null) {
			double closestdis = Double.MAX_VALUE;
			for (T temp : (Collection<T>) classZones.get(type).values()) {
				double distance = temp.getDistanceToZone(obj);
				if (distance < closestdis) {
					closestdis = distance;
					zone = temp;
				}
			}
			return zone;
		} else {
			return zone;
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ZoneManager instance = new ZoneManager();
	}
}
