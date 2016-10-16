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

import l2server.gameserver.model.actor.instance.L2DoorInstance;

/**
 * 61
 * d6 6d c0 4b		door id
 * 8f 14 00 00 		x
 * b7 f1 00 00 		y
 * 60 f2 ff ff 		z
 * 00 00 00 00 		??
 * <p>
 * format  dddd	rev 377  ID:%d X:%d Y:%d Z:%d
 * ddddd   rev 419
 *
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class DoorStatusUpdate extends L2GameServerPacket
{
	private L2DoorInstance door;

	public DoorStatusUpdate(L2DoorInstance door)
	{
		this.door = door;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.door.getObjectId());
		writeD(this.door.getOpen() ? 0 : 1);
		writeD(this.door.getDamage());
		writeD(this.door.isEnemy() ? 1 : 0);
		writeD(this.door.getDoorId());
		writeD((int) this.door.getCurrentHp());
		writeD(this.door.getMaxVisibleHp());
	}
}
