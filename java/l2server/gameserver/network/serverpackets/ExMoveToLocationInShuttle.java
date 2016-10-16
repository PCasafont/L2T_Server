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

public class ExMoveToLocationInShuttle extends L2GameServerPacket
{
	private int charObjId;
	private int shuttleId;
	private Point3D destination;
	@SuppressWarnings("unused")
	private int heading;

	public ExMoveToLocationInShuttle(L2PcInstance player)
	{
		charObjId = player.getObjectId();
		shuttleId = player.getShuttle().getObjectId();
		destination = player.getInVehiclePosition();
		heading = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(charObjId);
		writeD(shuttleId);
		writeD(destination.getX());
		writeD(destination.getY());
		writeD(destination.getZ());
		writeD(0x00); // ???
		writeD(0x00); // ???
		writeD(0x00); // ???
	}
}
