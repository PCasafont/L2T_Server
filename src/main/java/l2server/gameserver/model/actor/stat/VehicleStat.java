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

package l2server.gameserver.model.actor.stat;

import l2server.gameserver.model.actor.Vehicle;

public class VehicleStat extends CharStat {
	private float moveSpeed = 0;
	private int rotationSpeed = 0;
	
	public VehicleStat(Vehicle activeChar) {
		super(activeChar);
	}
	
	@Override
	public float getMoveSpeed() {
		return moveSpeed;
	}
	
	public final void setMoveSpeed(float speed) {
		moveSpeed = speed;
	}
	
	public final int getRotationSpeed() {
		return rotationSpeed;
	}
	
	public final void setRotationSpeed(int speed) {
		rotationSpeed = speed;
	}
}
