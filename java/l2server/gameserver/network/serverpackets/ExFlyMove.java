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
	private int _objectId;
	private L2FlyMoveType _type;
	private int _id;
	Map<Integer, Point3D> _moves;

	public ExFlyMove(L2PcInstance activeChar, int id, Map<Integer, Point3D> options)
	{
		_objectId = activeChar.getObjectId();
		_type = L2FlyMoveType.CHOOSE;
		_id = id;
		_moves = options;
		if (_moves.containsKey(-1))
		{
			_type = L2FlyMoveType.START;
			activeChar.setXYZ(_moves.get(-1).getX(), _moves.get(-1).getY(), _moves.get(-1).getZ());
		}
	}

	public ExFlyMove(L2PcInstance activeChar, int id, int ordinal, int x, int y, int z)
	{
		_objectId = activeChar.getObjectId();
		_type = L2FlyMoveType.MOVE;
		_id = id;
		_moves = new HashMap<>();
		_moves.put(ordinal, new Point3D(x, y, z));
		activeChar.setXYZ(x, y, z);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeD(_type.ordinal());
		writeD(0x00); // GoD ???
		writeD(_id);
		writeD(_moves.size());
		for (int moveId : _moves.keySet())
		{
			writeD(moveId);
			writeD(0x00); // GoD ???
			writeD(_moves.get(moveId).getX());
			writeD(_moves.get(moveId).getY());
			writeD(_moves.get(moveId).getZ());
		}
	}
}
