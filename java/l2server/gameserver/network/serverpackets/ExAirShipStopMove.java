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

import l2server.gameserver.model.actor.instance.L2AirShipInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public class ExAirShipStopMove extends L2GameServerPacket
{

	private final int playerId, airShipId, x, y, z;

	public ExAirShipStopMove(L2PcInstance player, L2AirShipInstance ship, int x, int y, int z)
	{
		playerId = player.getObjectId();
		airShipId = ship.getObjectId();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(airShipId);
		writeD(playerId);
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
