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

/**
 * @author Pere
 */
public class ExTeleportToLocationActivate extends L2GameServerPacket
{
	private final int _objId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public ExTeleportToLocationActivate(int objId, int x, int y, int z, int heading)
	{
		_objId = objId;
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00);
		writeD(_heading);
		writeD(0x00);
	}
}
