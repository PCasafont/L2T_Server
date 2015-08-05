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
package l2tserver.gameserver.datatables;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;

import l2tserver.Config;
import l2tserver.gameserver.model.L2Spawn;
import l2tserver.gameserver.model.SpawnGroup;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2MonsterInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.templates.SpawnData;
import l2tserver.gameserver.templates.chars.L2NpcTemplate;
import l2tserver.log.Log;
import l2tserver.util.Rnd;
import l2tserver.util.xml.XmlDocument;
import l2tserver.util.xml.XmlNode;

/**
 * @author Nightmare
 */
public class SpawnTable
{
	private CopyOnWriteArraySet<L2Spawn> _spawnTable = new CopyOnWriteArraySet<L2Spawn>();
	private ConcurrentMap<String, List<L2Spawn>> _specificSpawnTable = new ConcurrentHashMap<String, List<L2Spawn>>();
	private CopyOnWriteArraySet<SpawnGroup> _spawnGroups = new CopyOnWriteArraySet<SpawnGroup>();

	public static SpawnTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
			fillSpawnTable();
	}
	
	public CopyOnWriteArraySet<L2Spawn> getSpawnTable()
	{
		return _spawnTable;
	}
	
	public List<L2Spawn> getSpecificSpawns(String name)
	{
		name = name.toLowerCase();
		return _specificSpawnTable.get(name);
	}
	
	public void spawnSpecificTable(String tableName)
	{
		tableName = tableName.toLowerCase();
		List<L2Spawn> table = _specificSpawnTable.get(tableName);
		if (table == null)
		{
			Log.warning("Specific spawn table not found: " + tableName);
			return;
		}
		
		for (L2Spawn spawn : table)
		{
			if (spawn == null)
				continue;
			
			spawn.startRespawn();
			spawn.doSpawn(true);
		}	
	}
	
	public void despawnSpecificTable(String tableName)
	{
		tableName = tableName.toLowerCase();
		
		List<L2Spawn> table = _specificSpawnTable.get(tableName);
		if (table == null)
		{
			Log.warning("Specific spawn table not found: " + tableName);
			return;
		}
		
		for (L2Spawn spawn : table)
		{
			if (spawn == null)
				continue;
			
			spawn.stopRespawn();
				
			L2Npc npc = spawn.getNpc();
			if (npc != null)
				npc.deleteMe();	
		}
	}
	
	private void fillSpawnTable()
	{
		int count = 0;
		for (L2NpcTemplate t : NpcTable.getInstance().getAllTemplates())
		{
			for (SpawnData sp : t.getSpawns())
			{
				try
				{
					L2Spawn spawn = new L2Spawn(t);
					spawn.setX(sp.X);
					spawn.setY(sp.Y);
					spawn.setZ(sp.Z);
					spawn.setHeading(sp.Heading);
					spawn.setRespawnDelay(sp.Respawn);
					spawn.setRandomRespawnDelay(sp.RandomRespawn);
					spawn.setDbName(sp.DbName);
					if (sp.Respawn <= 0)
						spawn.stopRespawn();
					else
						spawn.startRespawn();
					
					spawn.doSpawn();
					_spawnTable.add(spawn);
					count++;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		Log.info("Template spawns: Loaded " + count + " Npc Spawn Locations.");
		
		if (Config.DEBUG)
			Log.fine("SpawnTable: Spawning completed, total number of NPCs in the world: " + (_spawnTable.size() + count));

		File dir = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "spawns");
		if (!dir.exists())
		{
			Log.warning("Dir " + dir.getAbsolutePath() + " doesn't exist");
			return;
		}
		
		count = 0;
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (!f.getName().endsWith(".xml"))
				continue;
			
			try
			{
				XmlDocument doc = new XmlDocument(f);
				List<L2Spawn> spawns = loadSpawns(doc.getFirstChild(), true);
				for (L2Spawn spawn : spawns)
					spawn.doSpawn();
				_spawnTable.addAll(spawns);
				count += spawns.size();
			}
			catch(Exception e)
			{
				Log.log(Level.WARNING, "Could not parse " + f.getName() + " file.", e);
			}
		}

		Log.info("SpawnTable: Loaded " + count + " global spawns!");
		Log.info("SpawnTable: Loaded " + _specificSpawnTable.size() + " specific spawn tables!");
	}
	
	private List<L2Spawn> loadSpawns(XmlNode node, boolean isRoot)
	{
		List<L2Spawn> spawns = new ArrayList<L2Spawn>();
		for (XmlNode npcNode : node.getChildren())
		{
			if (npcNode.getName().equalsIgnoreCase("specificSpawnList") && isRoot)
			{
				String name = npcNode.getString("name");
				_specificSpawnTable.put(name, loadSpawns(npcNode, false));
			}
			else if (npcNode.getName().equalsIgnoreCase("group") && isRoot)
			{
				_spawnGroups.add(new SpawnGroup(npcNode));
			}
			else if (npcNode.getName().equalsIgnoreCase("spawn"))
			{
				int npcId = npcNode.getInt("npcId");
				List<int[]> randomCoords = null;
				int x = 0;
				int y = 0;
				int z = 0;
				int heading = 0;
				if (!npcNode.hasAttribute("x"))
				{
					randomCoords = new ArrayList<int[]>();
					for (XmlNode coordNode : npcNode.getChildren())
					{
						if (!coordNode.getName().equalsIgnoreCase("randomCoord"))
							continue;
						
						x = coordNode.getInt("x");
						y = coordNode.getInt("y");
						z = coordNode.getInt("z");
						heading = coordNode.getInt("heading");
						int chance = coordNode.getInt("chance");
						randomCoords.add(new int[]{x, y, z, heading, chance});
					}
				}
				else
				{
					x = npcNode.getInt("x");
					y = npcNode.getInt("y");
					z = npcNode.getInt("z");
					heading = npcNode.getInt("heading");
				}
				
				int respawn = npcNode.getInt("respawn");
				int randomRespawn = npcNode.getInt("randomRespawn", 0);
				String dbName = npcNode.getString("dbName", null);
				
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
				if (template == null)
					continue;
				
				L2Spawn newSpawn = null;
				try
				{
					newSpawn = new L2Spawn(template);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				
				if (newSpawn == null)
					continue;
				
				newSpawn.setX(x);
				newSpawn.setY(y);
				newSpawn.setZ(z);
				newSpawn.setHeading(heading);
				newSpawn.setRandomCoords(randomCoords);
				
				newSpawn.setRespawnDelay(respawn);
				newSpawn.setRandomRespawnDelay(randomRespawn);
				newSpawn.setDbName(dbName);
				if (respawn <= 0)
					newSpawn.stopRespawn();
				else
					newSpawn.startRespawn();
				spawns.add(newSpawn);
			}
		}
		
		return spawns;
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		_spawnTable.add(spawn);
		
		if (storeInDb)
		{
			SpawnData sp = new SpawnData(spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading(), spawn.getRespawnDelay(), spawn.getRandomRespawnDelay());
			sp.DbName = spawn.getDbName();
			spawn.getTemplate().getSpawns().add(sp);
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!_spawnTable.remove(spawn))
			return;
		
		if (updateDb)
		{
			for (SpawnData sp : spawn.getTemplate().getSpawns())
			{
				if (sp.X == spawn.getX() && sp.Y == spawn.getY() && sp.Z == spawn.getZ())
				{
					spawn.getTemplate().getSpawns().remove(sp);
					break;
				}
			}
		}
	}
	
	//just wrapper
	public void reloadAll()
	{
		fillSpawnTable();
	}
	
	/**
	 * Get all the spawn of a NPC<BR><BR>
	 *
	 * @param npcId : ID of the NPC to find.
	 * @return
	 */
	public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex, boolean showposition)
	{
		int index = 0;
		for (L2Spawn spawn : _spawnTable)
		{
			if (npcId == spawn.getNpcId())
			{
				index++;
				L2Npc _npc = spawn.getNpc();
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
					{
						if (showposition && _npc != null)
							activeChar.teleToLocation(_npc.getX(), _npc.getY(), _npc.getZ(), true);
						else
							activeChar.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ(), true);
					}
				}
				else
				{
					if (showposition && _npc != null)
						activeChar.sendMessage(index + " - " + spawn.getTemplate().Name + " (" + spawn + "): " + _npc.getX() + " "+ _npc.getY() + " " + _npc.getZ());
					else
						activeChar.sendMessage(index + " - " + spawn.getTemplate().Name + " (" + spawn + "): " + spawn.getX() + " "+ spawn.getY() + " " + spawn.getZ());
				}
			}
		}
		
		if (index == 0)
			activeChar.sendMessage("No current spawns found.");
	}
	
	public L2Spawn findFirst(String name)
	{
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable())
		{
			if (spawn.getTemplate().getName().equalsIgnoreCase(name) && spawn.getNpc() != null)
				return spawn;
		}

		for (SpawnGroup group : _spawnGroups)
		{
			for (L2Spawn spawn : group.getSpawns())
			{
				if (spawn.getTemplate().getName().equalsIgnoreCase(name) && spawn.getNpc() != null)
					return spawn;
			}
		}
		
		return null;
	}
	
	public L2Spawn getRandomMonsterSpawn()
	{
		L2Spawn spawn = null;
		while (spawn == null || spawn.getNpc() == null
				|| !(spawn.getNpc() instanceof L2MonsterInstance))
		{
			int randomId = Rnd.get(_spawnTable.size());
			int i = 0;
			for (L2Spawn s : _spawnTable)
			{
				if (i == randomId)
				{
					spawn = s;
					break;
				}
				i++;
			}
		}
		return spawn;
	}

	private double _totalDistributedSpawnWeight = 0.0;
	private Map<L2Spawn, Double> _distributedSpawnWeights = new LinkedHashMap<L2Spawn, Double>();
	
	public L2Spawn getRandomDistributedSpawn()
	{
		if (_distributedSpawnWeights.isEmpty())
			calculateDistributedSpawnWeights();

		double random = Rnd.get() * _totalDistributedSpawnWeight;
		double current = 0.0;
		for (Entry<L2Spawn, Double> entry : _distributedSpawnWeights.entrySet())
		{
			current += entry.getValue();
			if (current > random)
				return entry.getKey();
		}
		
		return null;
	}
	
	private void calculateDistributedSpawnWeights()
	{
		_totalDistributedSpawnWeight = 0.0;
		_distributedSpawnWeights.clear();
		for (L2Spawn spawn : _spawnTable)
		{
			if (spawn == null)
				continue;
			
			L2Npc npc = spawn.getNpc();
			if (npc == null)
				continue;
			
			int knownChars = npc.getKnownList().getKnownCharactersInRadius(1000).size() + 1;
			double weight = 1.0 / knownChars;
			
			_totalDistributedSpawnWeight += weight;
			_distributedSpawnWeights.put(spawn, weight);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SpawnTable _instance = new SpawnTable();
	}
}
