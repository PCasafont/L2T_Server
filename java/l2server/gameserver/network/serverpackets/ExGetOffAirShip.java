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

public class ExGetOffAirShip extends L2GameServerPacket
{

	private final int playerId, airShipId, x, y, z;

	public ExGetOffAirShip(L2Character player, L2Character ship, int x, int y, int z)
	{
		this.playerId = player.getObjectId();
		this.airShipId = ship.getObjectId();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.playerId);
		writeD(this.airShipId);
		writeD(this.x);
		writeD(this.y);
		writeD(this.z);
	}
}
