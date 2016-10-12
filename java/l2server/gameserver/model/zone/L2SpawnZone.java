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
	private List<Location> _spawnLocs = null;
	private List<Location> _chaoticSpawnLocs = null;

	public L2SpawnZone(int id)
	{
		super(id);
	}

	public final void addSpawn(int x, int y, int z)
	{
		if (_spawnLocs == null)
		{
			_spawnLocs = new ArrayList<>();
		}

		_spawnLocs.add(new Location(x, y, z));
	}

	public final void addChaoticSpawn(int x, int y, int z)
	{
		if (_chaoticSpawnLocs == null)
		{
			_chaoticSpawnLocs = new ArrayList<>();
		}

		_chaoticSpawnLocs.add(new Location(x, y, z));
	}

	public final List<Location> getSpawns()
	{
		return _spawnLocs;
	}

	public final Location getSpawnLoc()
	{
		if (Config.RANDOM_RESPAWN_IN_TOWN_ENABLED)
		{
			return _spawnLocs.get(Rnd.get(_spawnLocs.size()));
		}
		else
		{
			return _spawnLocs.get(0);
		}
	}

	public final Location getChaoticSpawnLoc()
	{
		if (_chaoticSpawnLocs != null)
		{
			return _chaoticSpawnLocs.get(Rnd.get(_chaoticSpawnLocs.size()));
		}
		else
		{
			return getSpawnLoc();
		}
	}
}
