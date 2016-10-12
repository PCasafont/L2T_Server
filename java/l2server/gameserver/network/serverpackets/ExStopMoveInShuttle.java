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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Point3D;

/**
 * @author Pere
 */
public class ExStopMoveInShuttle extends L2GameServerPacket
{
	private int _charObjId;
	private int _shuttleId;
	private Point3D _pos;
	private int _heading;

	public ExStopMoveInShuttle(L2PcInstance player, int boatId)
	{
		_charObjId = player.getObjectId();
		_shuttleId = boatId;
		_pos = player.getInVehiclePosition();
		_heading = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId);
		writeD(_shuttleId);
		if (_pos != null)
		{
			writeD(_pos.getX());
			writeD(_pos.getY());
			writeD(_pos.getZ());
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		writeD(_heading);
	}
}
