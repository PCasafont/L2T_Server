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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.SpawnGroup;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.SpawnData;
import l2server.gameserver.templates.chars.NpcTemplate;
import l2server.util.Rnd;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Nightmare
 */
public class SpawnTable {
	private static Logger log = LoggerFactory.getLogger(SpawnTable.class.getName());

	private CopyOnWriteArraySet<L2Spawn> spawnTable = new CopyOnWriteArraySet<>();
	private ConcurrentMap<String, List<L2Spawn>> specificSpawnTable = new ConcurrentHashMap<>();
	private CopyOnWriteArraySet<SpawnGroup> spawnGroups = new CopyOnWriteArraySet<>();
	private int customSpawnCount;
	
	public static SpawnTable getInstance() {
		return SingletonHolder.instance;
	}
	
	private SpawnTable() {
	}
	
	public CopyOnWriteArraySet<L2Spawn> getSpawnTable() {
		return spawnTable;
	}
	
	public List<L2Spawn> getSpecificSpawns(String name) {
		name = name.toLowerCase();
		return specificSpawnTable.get(name);
	}
	
	public CopyOnWriteArraySet<SpawnGroup> getSpawnGroups() {
		return spawnGroups;
	}
	
	public void spawnSpecificTable(String tableName) {
		tableName = tableName.toLowerCase();
		List<L2Spawn> table = specificSpawnTable.get(tableName);
		if (table == null) {
			log.warn("Specific spawn table not found: " + tableName);
			return;
		}
		
		for (L2Spawn spawn : table) {
			if (spawn == null) {
				continue;
			}
			
			if (spawn.isRespawnEnabled() || tableName.contains("gainak")) {
				spawn.startRespawn();
			}
			
			spawn.doSpawn(true);
		}
	}
	
	public void despawnSpecificTable(String tableName) {
		tableName = tableName.toLowerCase();
		
		List<L2Spawn> table = specificSpawnTable.get(tableName);
		if (table == null) {
			log.warn("Specific spawn table not found: " + tableName);
			return;
		}
		
		for (L2Spawn spawn : table) {
			if (spawn == null) {
				continue;
			}
			
			spawn.stopRespawn();
			
			Npc npc = spawn.getNpc();
			if (npc != null) {
				npc.deleteMe();
			}
		}
	}
	
	@Load(dependencies = {NpcTable.class, CastleManager.class, World.class})
	public void load() {
		if (Config.ALT_DEV_NO_SPAWNS) {
			return;
		}
		
		int count = 0;
		for (NpcTemplate t : NpcTable.getInstance().getAllTemplates()) {
			for (SpawnData sp : t.getSpawns()) {
				try {
					L2Spawn spawn = new L2Spawn(t);
					spawn.setX(sp.X);
					spawn.setY(sp.Y);
					spawn.setZ(sp.Z);
					spawn.setHeading(sp.Heading);
					spawn.setRespawnDelay(sp.Respawn);
					spawn.setRandomRespawnDelay(sp.RandomRespawn);
					spawn.setDbName(sp.DbName);
					if (sp.Respawn <= 0) {
						spawn.stopRespawn();
					} else {
						spawn.startRespawn();
					}
					
					spawn.doSpawn();
					
					spawnTable.add(spawn);
					
					count++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		log.info("Template spawns: Loaded " + count + " Npc Spawn Locations.");
		
		if (Config.DEBUG) {
			log.debug("SpawnTable: Spawning completed, total number of NPCs in the world: " + (spawnTable.size() + customSpawnCount + count));
		}
		
		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "spawns");
		if (!dir.exists()) {
			log.warn("Dir " + dir.getAbsolutePath() + " doesn't exist");
			return;
		}
		
		// Override spawns by any custom folder
		File custom = new File(Config.DATAPACK_ROOT, "/data_" + Config.SERVER_NAME + "/spawns");
		if (custom.exists()) {
			dir = custom;
		}
		
		count = 0;
		File[] files = dir.listFiles();
		for (File f : files) {
			if (!f.getName().endsWith(".xml")) {
				continue;
			}
			
			try {
				XmlDocument doc = new XmlDocument(f);
				List<L2Spawn> spawns = loadSpawns(doc.getRoot(), true);
				//for (L2Spawn spawn : spawns)
				//	spawn.doSpawn();
				spawnTable.addAll(spawns);
				count += spawns.size();
			} catch (Exception e) {
				log.warn("Could not parse " + f.getName() + " file.", e);
			}
		}
		
		log.info("SpawnTable: Loaded " + count + " global spawns!");
		log.info("SpawnTable: Loaded " + specificSpawnTable.size() + " specific spawn tables!");
	}
	
	private List<L2Spawn> loadSpawns(XmlNode node, boolean isRoot) {
		List<L2Spawn> spawns = new ArrayList<>();
		for (XmlNode npcNode : node.getChildren()) {
			if (npcNode.getName().equalsIgnoreCase("specificSpawnList") && isRoot) {
				String name = npcNode.getString("name");
				specificSpawnTable.put(name, loadSpawns(npcNode, false));
			} else if (npcNode.getName().equalsIgnoreCase("group") && isRoot) {
				SpawnGroup spawnGroup = new SpawnGroup(npcNode);
				
				spawnGroups.add(spawnGroup);
				
				spawnTable.addAll(spawnGroup.getSpawns());
			} else if (npcNode.getName().equalsIgnoreCase("spawn")) {
				int npcId = npcNode.getInt("npcId");
				List<int[]> randomCoords = null;
				int x = 0;
				int y = 0;
				int z = 0;
				int heading = 0;
				if (!npcNode.hasAttribute("x")) {
					randomCoords = new ArrayList<>();
					for (XmlNode coordNode : npcNode.getChildren()) {
						if (!coordNode.getName().equalsIgnoreCase("randomCoord")) {
							continue;
						}
						
						x = coordNode.getInt("x");
						y = coordNode.getInt("y");
						z = coordNode.getInt("z");
						heading = coordNode.getInt("heading");
						int chance = coordNode.getInt("chance");
						randomCoords.add(new int[]{x, y, z, heading, chance});
					}
				} else {
					x = npcNode.getInt("x");
					y = npcNode.getInt("y");
					z = npcNode.getInt("z");
					heading = npcNode.getInt("heading");
				}
				
				int respawn = npcNode.getInt("respawn");
				int randomRespawn = npcNode.getInt("randomRespawn", 0);
				String dbName = npcNode.getString("dbName", "");
				
				NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
				if (template == null) {
					continue;
				}
				
				if (Config.isServer(Config.TENKAI_LEGACY)) {
					if (template.Type.equals("L2RaidBoss") && template.Level <= 85) {
						continue;
					}
					
					if (node.getName().endsWith("siege_guards")) {
						template.Level = 103;
					}
				}
				
				L2Spawn newSpawn = null;
				try {
					newSpawn = new L2Spawn(template);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
				newSpawn.setX(x);
				newSpawn.setY(y);
				newSpawn.setZ(z);
				newSpawn.setHeading(heading);
				newSpawn.setRandomCoords(randomCoords);
				
				newSpawn.setRespawnDelay(respawn);
				newSpawn.setRandomRespawnDelay(randomRespawn);
				newSpawn.setDbName(dbName);
				
				if (respawn <= 0) {
					newSpawn.stopRespawn();
				} else {
					newSpawn.startRespawn();
				}
				
				if (isRoot) {
					newSpawn.doSpawn();
				}
				
				spawns.add(newSpawn);
				
				if (template.Type.equals("L2Monster")) {
					template.addKnownSpawn(newSpawn);
				}
			}
		}
		
		return spawns;
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb) {
		spawnTable.add(spawn);
		
		if (storeInDb) {
			SpawnData sp = new SpawnData(spawn.getX(),
					spawn.getY(),
					spawn.getZ(),
					spawn.getHeading(),
					spawn.getRespawnDelay(),
					spawn.getRandomRespawnDelay());
			sp.DbName = spawn.getDbName();
			spawn.getTemplate().getSpawns().add(sp);
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb) {
		if (!spawnTable.remove(spawn)) {
			return;
		}
		
		if (updateDb) {
			for (SpawnData sp : spawn.getTemplate().getSpawns()) {
				if (sp.X == spawn.getX() && sp.Y == spawn.getY() && sp.Z == spawn.getZ()) {
					spawn.getTemplate().getSpawns().remove(sp);
					break;
				}
			}
		}
	}
	
	/**
	 * Get all the spawn of a NPC<BR><BR>
	 *
	 * @param npcId : ID of the NPC to find.
	 */
	public void findNPCInstances(Player activeChar, int npcId, int teleportIndex, boolean showposition) {
		int index = 0;
		for (L2Spawn spawn : spawnTable) {
			if (npcId == spawn.getNpcId()) {
				index++;
				Npc npc = spawn.getNpc();
				if (teleportIndex > -1) {
					if (teleportIndex == index) {
						if (showposition && npc != null) {
							activeChar.teleToLocation(npc.getX(), npc.getY(), npc.getZ(), true);
						} else {
							activeChar.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ(), true);
						}
					}
				} else {
					if (showposition && npc != null) {
						activeChar.sendMessage(
								index + " - " + spawn.getTemplate().Name + " (" + spawn + "): " + npc.getX() + " " + npc.getY() + " " + npc.getZ());
					} else {
						activeChar.sendMessage(
								index + " - " + spawn.getTemplate().Name + " (" + spawn + "): " + spawn.getX() + " " + spawn.getY() + " " +
										spawn.getZ());
					}
				}
			}
		}
		
		if (index == 0) {
			activeChar.sendMessage("No current spawns found.");
		}
	}
	
	public L2Spawn findFirst(String name) {
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable()) {
			if (spawn.getTemplate().getName().equalsIgnoreCase(name) && spawn.getNpc() != null) {
				return spawn;
			}
		}
		
		for (SpawnGroup group : spawnGroups) {
			for (L2Spawn spawn : group.getSpawns()) {
				if (spawn.getTemplate().getName().equalsIgnoreCase(name) && spawn.getNpc() != null) {
					return spawn;
				}
			}
		}
		
		return null;
	}
	
	public List<L2Spawn> getAllSpawns(final int npcId) {
		if (npcId == 0) {
			int min = 0;
			int max = 0;
			for (L2Spawn spawn : spawnTable) {
				if (spawn.getZ() < min) {
					min = spawn.getZ();
				}
				if (spawn.getZ() > max) {
					max = spawn.getZ();
				}
			}
			
			System.out.println(min + " " + max);
		}
		List<L2Spawn> result = new ArrayList<>();
		
		for (L2Spawn spawn : spawnTable) {
			if (npcId != spawn.getNpcId()) {
				continue;
			}
			
			result.add(spawn);
		}
		
		return result;
	}
	
	public L2Spawn getRandomMonsterSpawn() {
		L2Spawn spawn = null;
		while (spawn == null || spawn.getNpc() == null || !(spawn.getNpc() instanceof MonsterInstance)) {
			int randomId = Rnd.get(spawnTable.size());
			int i = 0;
			for (L2Spawn s : spawnTable) {
				if (i == randomId) {
					spawn = s;
					break;
				}
				i++;
			}
		}
		return spawn;
	}
	
	private double totalDistributedSpawnWeight = 0.0;
	private Map<L2Spawn, Double> distributedSpawnWeights = new LinkedHashMap<>();
	
	public L2Spawn getRandomDistributedSpawn() {
		if (distributedSpawnWeights.isEmpty()) {
			calculateDistributedSpawnWeights();
		}
		
		double random = Rnd.get() * totalDistributedSpawnWeight;
		double current = 0.0;
		for (Entry<L2Spawn, Double> entry : distributedSpawnWeights.entrySet()) {
			current += entry.getValue();
			if (current > random) {
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	private void calculateDistributedSpawnWeights() {
		totalDistributedSpawnWeight = 0.0;
		distributedSpawnWeights.clear();
		for (L2Spawn spawn : spawnTable) {
			if (spawn == null) {
				continue;
			}
			
			Npc npc = spawn.getNpc();
			if (npc == null) {
				continue;
			}

			/*int radiusToCheck = 3000;
			int knownChars = 1;
			for (L2Spawn toCheck : spawnTable)
			{
				if (toCheck == null)
					continue;

				Npc npcToCheck = toCheck.getNpc();
				if (npcToCheck == null)
					continue;

				if (Util.checkIfInRange(radiusToCheck, npc, npcToCheck, true))
					knownChars++;
			}*/
			
			int knownChars = npc.getKnownList().getKnownCharactersInRadius(1000).size() + 1;
			double weight = 1.0 / knownChars;
			
			totalDistributedSpawnWeight += weight;
			distributedSpawnWeights.put(spawn, weight);
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final SpawnTable instance = new SpawnTable();
	}
}
