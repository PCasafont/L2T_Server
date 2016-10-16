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

public class ExMoveToLocationAirShip extends L2GameServerPacket
{

	private final int objId, tx, ty, tz, x, y, z;

	public ExMoveToLocationAirShip(L2Character cha)
	{
		this.objId = cha.getObjectId();
		this.tx = cha.getXdestination();
		this.ty = cha.getYdestination();
		this.tz = cha.getZdestination();
		this.x = cha.getX();
		this.y = cha.getY();
		this.z = cha.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.objId);
		writeD(this.tx);
		writeD(this.ty);
		writeD(this.tz);
		writeD(this.x);
		writeD(this.y);
		writeD(this.z);
	}
}
