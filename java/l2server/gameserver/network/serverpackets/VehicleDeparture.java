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
	private final int _objId, _x, _y, _z, _moveSpeed, _rotationSpeed;

	/**
	 */
	public VehicleDeparture(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_x = boat.getXdestination();
		_y = boat.getYdestination();
		_z = boat.getZdestination();
		_moveSpeed = (int) boat.getStat().getMoveSpeed();
		_rotationSpeed = boat.getStat().getRotationSpeed();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x6c);
		writeD(_objId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
