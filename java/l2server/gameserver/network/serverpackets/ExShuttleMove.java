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

import l2server.gameserver.model.actor.instance.L2ShuttleInstance;

/**
 * @author Pere
 */
public class ExShuttleMove extends L2GameServerPacket
{
	private final int objId, x, y, z, moveSpeed, rotationSpeed;

	public ExShuttleMove(L2ShuttleInstance shut)
	{
		this.objId = shut.getObjectId();
		this.x = shut.getXdestination();
		this.y = shut.getYdestination();
		this.z = shut.getZdestination();
		this.moveSpeed = (int) shut.getStat().getMoveSpeed();
		this.rotationSpeed = shut.getStat().getRotationSpeed();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.objId);
		writeD(this.moveSpeed);
		writeD(this.rotationSpeed);
		writeD(this.x);
		writeD(this.y);
		writeD(this.z);
	}
}
