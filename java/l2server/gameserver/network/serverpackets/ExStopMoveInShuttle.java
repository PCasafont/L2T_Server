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
	private int charObjId;
	private int shuttleId;
	private Point3D pos;
	private int heading;

	public ExStopMoveInShuttle(L2PcInstance player, int boatId)
	{
		this.charObjId = player.getObjectId();
		this.shuttleId = boatId;
		this.pos = player.getInVehiclePosition();
		this.heading = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.charObjId);
		writeD(this.shuttleId);
		if (this.pos != null)
		{
			writeD(this.pos.getX());
			writeD(this.pos.getY());
			writeD(this.pos.getZ());
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		writeD(this.heading);
	}
}
