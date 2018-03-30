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

import l2server.gameserver.GeoData;
import l2server.gameserver.model.L2World;
import l2server.gameserver.pathfinding.AbstractNodeLoc;

/**
 * @author -Nemesiss-
 */
public class NodeLoc extends AbstractNodeLoc {
	private int x;
	private int y;
	private short geoHeightAndNSWE;

	public NodeLoc(int x, int y, short z) {
		this.x = x;
		this.y = y;
		geoHeightAndNSWE = GeoData.getInstance().getHeightAndNSWE(x, y, z);
	}

	public void set(int x, int y, short z) {
		this.x = x;
		this.y = y;
		geoHeightAndNSWE = GeoData.getInstance().getHeightAndNSWE(x, y, z);
	}

	public short getNSWE() {
		return (short) (geoHeightAndNSWE & 0x0f);
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getX()
	 */
	@Override
	public int getX() {
		return (x << 4) + L2World.MAP_MIN_X;
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getY()
	 */
	@Override
	public int getY() {
		return (y << 4) + L2World.MAP_MIN_Y;
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getZ()
	 */
	@Override
	public short getZ() {
		short height = (short) (geoHeightAndNSWE & 0x0fff0);
		return (short) (height >> 1);
	}

	@Override
	public void setZ(short z) {
		//
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getNodeX()
	 */
	@Override
	public int getNodeX() {
		return x;
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getNodeY()
	 */
	@Override
	public int getNodeY() {
		return y;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + geoHeightAndNSWE;
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
		if (!(obj instanceof NodeLoc)) {
			return false;
		}
		final NodeLoc other = (NodeLoc) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return geoHeightAndNSWE == other.geoHeightAndNSWE;
	}
}
