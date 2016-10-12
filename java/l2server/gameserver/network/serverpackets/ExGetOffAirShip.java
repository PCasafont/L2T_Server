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

	private final int _playerId, _airShipId, _x, _y, _z;

	public ExGetOffAirShip(L2Character player, L2Character ship, int x, int y, int z)
	{
		_playerId = player.getObjectId();
		_airShipId = ship.getObjectId();
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_playerId);
		writeD(_airShipId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
