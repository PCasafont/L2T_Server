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

package l2server.gameserver.pathfinding.geonodes;

import l2server.gameserver.pathfinding.AbstractNode;
import l2server.gameserver.pathfinding.AbstractNodeLoc;

/**
 * @author -Nemesiss-
 */
public class GeoNode extends AbstractNode
{
	private final int _neighborsIdx;
	private short _cost;
	private GeoNode[] _neighbors;

	public GeoNode(AbstractNodeLoc Loc, int Neighbors_idx)
	{
		super(Loc);
		_neighborsIdx = Neighbors_idx;
	}

	public short getCost()
	{
		return _cost;
	}

	public void setCost(int cost)
	{
		_cost = (short) cost;
	}

	public GeoNode[] getNeighbors()
	{
		return _neighbors;
	}

	public void attachNeighbors()
	{
		if (getLoc() == null)
		{
			_neighbors = null;
		}
		else
		{
			_neighbors = GeoPathFinding.getInstance().readNeighbors(this, _neighborsIdx);
		}
	}
}
