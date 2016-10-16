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
import lombok.Getter;

/**
 * @author -Nemesiss-
 */
public class GeoNode extends AbstractNode
{
	private final int neighborsIdx;
	@Getter private short cost;
	@Getter private GeoNode[] neighbors;

	public GeoNode(AbstractNodeLoc Loc, int Neighbors_idx)
	{
		super(Loc);
		neighborsIdx = Neighbors_idx;
	}


	public void setCost(int cost)
	{
		this.cost = (short) cost;
	}


	public void attachNeighbors()
	{
		if (getLoc() == null)
		{
			neighbors = null;
		}
		else
		{
			neighbors = GeoPathFinding.getInstance().readNeighbors(this, neighborsIdx);
		}
	}
}
