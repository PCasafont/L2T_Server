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

import l2server.gameserver.model.actor.L2Character;

/**
 * @author kerberos
 */
public class ExStopMoveAirShip extends L2GameServerPacket
{
	// store coords here because they can be changed from other threads
	final int _objectId, _x, _y, _z, _heading;

	public ExStopMoveAirShip(L2Character ship)
	{
		_objectId = ship.getObjectId();
		_x = ship.getX();
		_y = ship.getY();
		_z = ship.getZ();
		_heading = ship.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
