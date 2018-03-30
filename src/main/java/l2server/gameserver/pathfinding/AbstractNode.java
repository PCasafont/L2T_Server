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

package l2server.gameserver.pathfinding;

public abstract class AbstractNode {
	private AbstractNodeLoc loc;
	private AbstractNode parent;

	public AbstractNode(AbstractNodeLoc loc) {
		this.loc = loc;
	}

	public void setParent(AbstractNode p) {
		parent = p;
	}

	public AbstractNode getParent() {
		return parent;
	}

	public AbstractNodeLoc getLoc() {
		return loc;
	}

	public void setLoc(AbstractNodeLoc l) {
		loc = l;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (loc == null ? 0 : loc.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AbstractNode)) {
			return false;
		}
		final AbstractNode other = (AbstractNode) obj;
		if (loc == null) {
			if (other.loc != null) {
				return false;
			}
		} else if (!loc.equals(other.loc)) {
			return false;
		}
		return true;
	}
}
