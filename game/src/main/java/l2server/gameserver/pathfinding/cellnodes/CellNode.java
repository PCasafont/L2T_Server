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

public class CellNode extends AbstractNode {
	private CellNode next = null;
	private boolean isInUse = true;
	private float cost = -1000;

	public CellNode(AbstractNodeLoc loc) {
		super(loc);
	}

	public boolean isInUse() {
		return isInUse;
	}

	public void setInUse() {
		isInUse = true;
	}

	public CellNode getNext() {
		return next;
	}

	public void setNext(CellNode next) {
		this.next = next;
	}

	public float getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = (float) cost;
	}

	public void free() {
		setParent(null);
		cost = -1000;
		isInUse = false;
		next = null;
	}
}
