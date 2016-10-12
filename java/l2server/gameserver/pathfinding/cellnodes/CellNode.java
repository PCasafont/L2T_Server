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

package l2server.gameserver.pathfinding.cellnodes;

import l2server.gameserver.pathfinding.AbstractNode;
import l2server.gameserver.pathfinding.AbstractNodeLoc;

public class CellNode extends AbstractNode
{
	private CellNode _next = null;
	private boolean _isInUse = true;
	private float _cost = -1000;

	public CellNode(AbstractNodeLoc loc)
	{
		super(loc);
	}

	public boolean isInUse()
	{
		return _isInUse;
	}

	public void setInUse()
	{
		_isInUse = true;
	}

	public CellNode getNext()
	{
		return _next;
	}

	public void setNext(CellNode next)
	{
		_next = next;
	}

	public float getCost()
	{
		return _cost;
	}

	public void setCost(double cost)
	{
		_cost = (float) cost;
	}

	public void free()
	{
		setParent(null);
		_cost = -1000;
		_isInUse = false;
		_next = null;
	}
}
