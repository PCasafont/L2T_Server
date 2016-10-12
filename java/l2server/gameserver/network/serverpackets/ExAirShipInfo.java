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

import l2server.gameserver.model.actor.instance.L2AirShipInstance;

public class ExAirShipInfo extends L2GameServerPacket
{

	// store some parameters, because they can be changed during broadcast
	private final L2AirShipInstance _ship;
	private final int _x, _y, _z, _heading, _moveSpeed, _rotationSpeed, _captain, _helm;

	public ExAirShipInfo(L2AirShipInstance ship)
	{
		_ship = ship;
		_x = ship.getX();
		_y = ship.getY();
		_z = ship.getZ();
		_heading = ship.getHeading();
		_moveSpeed = (int) ship.getStat().getMoveSpeed();
		_rotationSpeed = ship.getStat().getRotationSpeed();
		_captain = ship.getCaptainId();
		_helm = ship.getHelmObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_ship.getObjectId());
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);

		writeD(_captain);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_helm);
		if (_helm != 0)
		{
			writeD(0x16e); // Controller X
			writeD(0x00); // Controller Y
			writeD(0x6b); // Controller Z
			writeD(0x15c); // Captain X
			writeD(0x00); // Captain Y
			writeD(0x69); // Captain Z
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}

		writeD(_ship.getFuel());
		writeD(_ship.getMaxFuel());
	}
}
