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

import l2server.gameserver.model.L2FlyMove.L2FlyMoveType;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Pere
 */
public class ExFlyMoveBroadcast extends L2GameServerPacket
{
	private int _objectId;
	private L2FlyMoveType _type;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _targetX;
	private int _targetY;
	private int _targetZ;

	public ExFlyMoveBroadcast(L2PcInstance activeChar, int x, int y, int z)
	{
		_objectId = activeChar.getObjectId();
		_type = L2FlyMoveType.MOVE;
		_originX = activeChar.getX();
		_originY = activeChar.getY();
		_originZ = activeChar.getZ();
		_targetX = x;
		_targetY = y;
		_targetZ = z;
	}

	public ExFlyMoveBroadcast(L2PcInstance activeChar, boolean start)
	{
		_objectId = activeChar.getObjectId();
		_type = start ? L2FlyMoveType.START : L2FlyMoveType.CHOOSE;
		_originX = activeChar.getX();
		_originY = activeChar.getY();
		_originZ = activeChar.getZ();
		_targetX = activeChar.getX();
		_targetY = activeChar.getY();
		_targetZ = activeChar.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_type.ordinal());
		writeD(0);
		writeD(_originX);
		writeD(_originY);
		writeD(_originZ);
		writeD(0);
		writeD(_targetX);
		writeD(_targetY);
		writeD(_targetZ);
	}
}
