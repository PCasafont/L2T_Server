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

package l2server.gameserver.util;

import l2server.gameserver.datatables.NpcTable;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.Rnd;

import java.util.logging.Level;

public class NpcUtil
{
	public static L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
				// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the
				// default spawn location, which is at the player's loc.
				if (x == 0 && y == 0)
				{
					Log.log(Level.SEVERE, "Failed to adjust bad coords for quest spawn! Spawn aborted!");
					return null;
				}
				if (randomOffset)
				{
					int offset;

					offset = Rnd.get(2); // Get the direction of the offset
					if (offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;

					offset = Rnd.get(2); // Get the direction of the offset
					if (offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				spawn.stopRespawn();
				spawn.doSpawn(isSummonSpawn);
				result = spawn.getNpc();

				if (despawnDelay > 0)
				{
					result.scheduleDespawn(despawnDelay);
				}

				return result;
			}
		}
		catch (Exception e1)
		{
			Log.warning("Could not spawn Npc " + npcId);
		}

		return null;
	}

	public static L2Spawn newSpawn(int npcId, int x, int y, int z, int heading, int respawn, int instanceId)
	{
		L2NpcTemplate spawnTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (spawnTemplate == null)
		{
			return null;
		}
		try
		{
			L2Spawn newSpawn = new L2Spawn(spawnTemplate);
			newSpawn.setX(x);
			newSpawn.setY(y);
			newSpawn.setZ(z);
			newSpawn.setHeading(heading);
			newSpawn.setRespawnDelay(respawn);
			newSpawn.setInstanceId(instanceId);
			if (respawn > 0)
			{
				newSpawn.startRespawn();
			}
			else
			{
				newSpawn.stopRespawn();
			}
			return newSpawn;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
