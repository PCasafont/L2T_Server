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

import l2server.gameserver.model.actor.instance.ShuttleInstance;

/**
 * @author Pere
 */
public class ExShuttleMove extends L2GameServerPacket {
	private final int objId, x, y, z, moveSpeed, rotationSpeed;
	
	public ExShuttleMove(ShuttleInstance shut) {
		objId = shut.getObjectId();
		x = shut.getXdestination();
		y = shut.getYdestination();
		z = shut.getZdestination();
		moveSpeed = (int) shut.getStat().getMoveSpeed();
		rotationSpeed = shut.getStat().getRotationSpeed();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(objId);
		writeD(moveSpeed);
		writeD(rotationSpeed);
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
