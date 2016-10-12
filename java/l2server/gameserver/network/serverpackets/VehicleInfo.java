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
public class VehicleInfo extends L2GameServerPacket
{
	// Store some parameters here because they can be changed during broadcast
	private final int _objId, _x, _y, _z, _heading;

	public VehicleInfo(L2BoatInstance boat)
	{
		_objId = boat.getObjectId();
		_x = boat.getX();
		_y = boat.getY();
		_z = boat.getZ();
		_heading = boat.getHeading();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
