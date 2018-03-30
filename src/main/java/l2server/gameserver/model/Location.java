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

package l2server.gameserver.model;

import l2server.gameserver.model.actor.L2Character;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.1 $ $Date: 2005/03/27 15:29:33 $
 */

public final class Location {
	private int x;
	private int y;
	private int z;
	private int heading;
	
	public Location(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Location(L2Object obj) {
		x = obj.getX();
		y = obj.getY();
		z = obj.getZ();
	}
	
	public Location(L2Character obj) {
		x = obj.getX();
		y = obj.getY();
		z = obj.getZ();
		heading = obj.getHeading();
	}
	
	public Location(int x, int y, int z, int heading) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public int getHeading() {
		return heading;
	}
}
