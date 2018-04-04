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

import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.util.Point3D;

public class ExGetOnAirShip extends L2GameServerPacket {
	
	private final int playerId, airShipId;
	private final Point3D pos;
	
	public ExGetOnAirShip(Player player, Creature ship) {
		playerId = player.getObjectId();
		airShipId = ship.getObjectId();
		pos = player.getInVehiclePosition();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(playerId);
		writeD(airShipId);
		writeD(pos.getX());
		writeD(pos.getY());
		writeD(pos.getZ());
	}
}
