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
import l2server.util.Point3D;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class ExFlyMove extends L2GameServerPacket
{
	private int objectId;
	private L2FlyMoveType type;
	private int id;
	Map<Integer, Point3D> moves;

	public ExFlyMove(L2PcInstance activeChar, int id, Map<Integer, Point3D> options)
	{
		objectId = activeChar.getObjectId();
		type = L2FlyMoveType.CHOOSE;
		this.id = id;
		moves = options;
		if (moves.containsKey(-1))
		{
			type = L2FlyMoveType.START;
			activeChar.setXYZ(moves.get(-1).getX(), moves.get(-1).getY(), moves.get(-1).getZ());
		}
	}

	public ExFlyMove(L2PcInstance activeChar, int id, int ordinal, int x, int y, int z)
	{
		objectId = activeChar.getObjectId();
		type = L2FlyMoveType.MOVE;
		this.id = id;
		moves = new HashMap<>();
		moves.put(ordinal, new Point3D(x, y, z));
		activeChar.setXYZ(x, y, z);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(objectId);
		writeD(type.ordinal());
		writeD(0x00); // GoD ???
		writeD(id);
		writeD(moves.size());
		for (int moveId : moves.keySet())
		{
			writeD(moveId);
			writeD(0x00); // GoD ???
			writeD(moves.get(moveId).getX());
			writeD(moves.get(moveId).getY());
			writeD(moves.get(moveId).getZ());
		}
	}
}
