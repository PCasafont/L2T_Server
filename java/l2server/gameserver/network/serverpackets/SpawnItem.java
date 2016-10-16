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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;

/**
 * 15
 * ee cc 11 43 		object id
 * 39 00 00 00 		item id
 * 8f 14 00 00 		x
 * b7 f1 00 00 		y
 * 60 f2 ff ff 		z
 * 01 00 00 00 		show item count
 * 7a 00 00 00	  count										 .
 * <p>
 * format  dddddddd
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class SpawnItem extends L2GameServerPacket
{
	private int objectId;
	private int itemId;
	private int x, y, z;
	private int stackable;
	private long count;

	public SpawnItem(L2Object obj)
	{
		this.objectId = obj.getObjectId();
		this.x = obj.getX();
		this.y = obj.getY();
		this.z = obj.getZ();

		if (obj instanceof L2ItemInstance)
		{
			L2ItemInstance item = (L2ItemInstance) obj;
			this.itemId = item.getItemId();
			this.stackable = item.isStackable() ? 0x01 : 0x00;
			this.count = item.getCount();
		}
		else
		{
			this.itemId = obj.getPoly().getPolyId();
			this.stackable = 0;
			this.count = 1;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.objectId);
		writeD(this.itemId);

		writeD(this.x);
		writeD(this.y);
		writeD(this.z);
		// only show item count if it is a stackable item
		writeD(this.stackable);
		writeQ(this.count);
		writeH(0x00); // c2
		writeH(0x00); // freya unk
	}
}
