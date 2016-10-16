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
	private int objectId;
	private L2FlyMoveType type;
	private int originX;
	private int originY;
	private int originZ;
	private int targetX;
	private int targetY;
	private int targetZ;

	public ExFlyMoveBroadcast(L2PcInstance activeChar, int x, int y, int z)
	{
		objectId = activeChar.getObjectId();
		type = L2FlyMoveType.MOVE;
		originX = activeChar.getX();
		originY = activeChar.getY();
		originZ = activeChar.getZ();
		targetX = x;
		targetY = y;
		targetZ = z;
	}

	public ExFlyMoveBroadcast(L2PcInstance activeChar, boolean start)
	{
		objectId = activeChar.getObjectId();
		type = start ? L2FlyMoveType.START : L2FlyMoveType.CHOOSE;
		originX = activeChar.getX();
		originY = activeChar.getY();
		originZ = activeChar.getZ();
		targetX = activeChar.getX();
		targetY = activeChar.getY();
		targetZ = activeChar.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(objectId);
		writeD(type.ordinal());
		writeD(0);
		writeD(originX);
		writeD(originY);
		writeD(originZ);
		writeD(0);
		writeD(targetX);
		writeD(targetY);
		writeD(targetZ);
	}
}
