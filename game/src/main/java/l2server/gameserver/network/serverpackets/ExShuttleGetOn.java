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

/**
 * @author Pere
 */
public class ExShuttleGetOn extends L2GameServerPacket {
	private final int playerId, shuttleId;
	private final Point3D pos;
	
	public ExShuttleGetOn(Player player, Creature shuttle) {
		playerId = player.getObjectId();
		shuttleId = shuttle.getObjectId();
		pos = player.getInVehiclePosition();
		player.gotOnOffShuttle();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(playerId);
		writeD(shuttleId);
		writeD(pos.getX());
		writeD(pos.getY());
		writeD(pos.getZ());
	}
}
