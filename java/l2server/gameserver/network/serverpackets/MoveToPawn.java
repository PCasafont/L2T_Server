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
 * 0000: 75  7a 07 80 49  63 27 00 4a  ea 01 00 00  c1 37 fe	uz..Ic'.J.....7. <p>
 * 0010: ff 9e c3 03 00 8f f3 ff ff						 .........<p>
 * <p>
 * <p>
 * format   dddddd		(player id, target id, distance, startx, starty, startz)<p>
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/04/06 16:13:46 $
 */
public class MoveToPawn extends L2GameServerPacket
{
	private int _charObjId;
	private int _targetId;
	private int _distance;
	private int _x, _y, _z, _tx, _ty, _tz;

	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);

		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_tx);
		writeD(_ty);
		writeD(_tz);
	}
}
