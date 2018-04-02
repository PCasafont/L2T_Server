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

import l2server.gameserver.model.World;
import l2server.gameserver.pathfinding.AbstractNodeLoc;

/**
 * @author -Nemesiss-
 */
public class GeoNodeLoc extends AbstractNodeLoc {
	private final short x;
	private final short y;
	private final short z;

	public GeoNodeLoc(short x, short y, short z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getX()
	 */
	@Override
	public int getX() {
		return World.MAP_MIN_X + x * 128 + 48;
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getY()
	 */
	@Override
	public int getY() {
		return World.MAP_MIN_Y + y * 128 + 48;
	}

	/**
	 * @see l2server.gameserver.pathfinding.AbstractNodeLoc#getZ()
	 */
	@Override
	public short getZ() {
		return z;
	}

	@Override
	public void setZ(short z) {
		//
	}

	@Override
	public int getNodeX() {
		return x;
	}

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
		result = prime * result + z;
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
		if (!(obj instanceof GeoNodeLoc)) {
			return false;
		}
		final GeoNodeLoc other = (GeoNodeLoc) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return z == other.z;
	}
}
