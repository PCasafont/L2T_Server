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

import l2server.gameserver.model.actor.instance.L2ShuttleInstance;

/**
 * @author Pere
 */
public class ExShuttleMove extends L2GameServerPacket
{
	private final int _objId, _x, _y, _z, _moveSpeed, _rotationSpeed;

	public ExShuttleMove(L2ShuttleInstance shut)
	{
		_objId = shut.getObjectId();
		_x = shut.getXdestination();
		_y = shut.getYdestination();
		_z = shut.getZdestination();
		_moveSpeed = (int) shut.getStat().getMoveSpeed();
		_rotationSpeed = shut.getStat().getRotationSpeed();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
