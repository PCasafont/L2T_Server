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

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.instancemanager.SearchDropManager;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.xml.XmlNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class SpawnGroup
{
	private final int minZ;
	private final int maxZ;
	private final L2Territory territory = new L2Territory(0);
	private final List<L2Spawn> spawns = new ArrayList<>();

	public SpawnGroup(XmlNode node)
	{
		minZ = node.getInt("minZ");
		maxZ = node.getInt("maxZ");
		for (XmlNode subNode : node.getChildren())
		{
			if (subNode.getName().equalsIgnoreCase("vertex"))
			{
				int x = subNode.getInt("x");
				int y = subNode.getInt("y");
				territory.add(x, y, minZ, maxZ, 0);
			}
			else if (subNode.getName().equalsIgnoreCase("spawn"))
			{
				int npcId = subNode.getInt("npcId");
				int amount = subNode.getInt("amount");
				int respawn = subNode.getInt("respawn");
				int randomRespawn = subNode.getInt("randomRespawn", 0);
				String dbName = subNode.getString("dbName", null);

				L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
				if (t == null)
				{
					Log.warning("Spawn group: no npc template with id " + npcId);
					continue;
				}

				for (int i = 0; i < amount; i++)
				{
					try
					{
						L2Spawn spawn = new L2Spawn(t);
						spawn.setRespawnDelay(respawn);
						spawn.setRandomRespawnDelay(randomRespawn);
						spawn.startRespawn();
						spawn.setGroup(this);
						spawn.setDbName(dbName);

						spawn.doSpawn();

						spawns.add(spawn);

						if (t.Type.equals("L2Monster"))
						{
							t.addKnownSpawn(spawn);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				SearchDropManager.getInstance().addLootInfo(t, true);
			}
		}
	}

	public int[] getRandomPoint()
	{
		return territory.getRandomPoint();
	}

	public L2Territory getTerritory()
	{
		return territory;
	}

	public List<L2Spawn> getSpawns()
	{
		return spawns;
	}
}
