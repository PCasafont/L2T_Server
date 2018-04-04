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

import l2server.gameserver.model.actor.instance.Player;
import l2server.util.Point3D;

/**
 * @author Maktakien
 */
public class StopMoveInVehicle extends L2GameServerPacket {
	private int charObjId;
	private int boatId;
	private Point3D pos;
	private int heading;
	
	public StopMoveInVehicle(Player player, int boatId) {
		charObjId = player.getObjectId();
		this.boatId = boatId;
		pos = player.getInVehiclePosition();
		heading = player.getHeading();
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(charObjId);
		writeD(boatId);
		writeD(pos.getX());
		writeD(pos.getY());
		writeD(pos.getZ());
		writeD(heading);
	}
}
