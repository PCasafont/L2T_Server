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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author Maktakien
 */
public class VehicleDeparture extends L2GameServerPacket
{
	// Store parameters because they can be changed during broadcast
	private final int objId, x, y, z, moveSpeed, rotationSpeed;

	/**
	 */
	public VehicleDeparture(L2BoatInstance boat)
	{
		objId = boat.getObjectId();
		x = boat.getXdestination();
		y = boat.getYdestination();
		z = boat.getZdestination();
		moveSpeed = (int) boat.getStat().getMoveSpeed();
		rotationSpeed = boat.getStat().getRotationSpeed();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x6c);
		writeD(objId);
		writeD(moveSpeed);
		writeD(rotationSpeed);
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
