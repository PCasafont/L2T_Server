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

package l2server.gameserver.model.zone;

import l2server.Config;
import l2server.gameserver.model.Location;
import l2server.util.Rnd;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract zone with spawn locations
 *
 * @author DS
 */
public abstract class L2SpawnZone extends L2ZoneType
{
	private List<Location> spawnLocs = null;
	private List<Location> chaoticSpawnLocs = null;

	public L2SpawnZone(int id)
	{
		super(id);
	}

	public final void addSpawn(int x, int y, int z)
	{
		if (this.spawnLocs == null)
		{
			this.spawnLocs = new ArrayList<>();
		}

		this.spawnLocs.add(new Location(x, y, z));
	}

	public final void addChaoticSpawn(int x, int y, int z)
	{
		if (this.chaoticSpawnLocs == null)
		{
			this.chaoticSpawnLocs = new ArrayList<>();
		}

		this.chaoticSpawnLocs.add(new Location(x, y, z));
	}

	public final List<Location> getSpawns()
	{
		return this.spawnLocs;
	}

	public final Location getSpawnLoc()
	{
		if (Config.RANDOM_RESPAWN_IN_TOWN_ENABLED)
		{
			return this.spawnLocs.get(Rnd.get(this.spawnLocs.size()));
		}
		else
		{
			return this.spawnLocs.get(0);
		}
	}

	public final Location getChaoticSpawnLoc()
	{
		if (this.chaoticSpawnLocs != null)
		{
			return this.chaoticSpawnLocs.get(Rnd.get(this.chaoticSpawnLocs.size()));
		}
		else
		{
			return getSpawnLoc();
		}
	}
}
