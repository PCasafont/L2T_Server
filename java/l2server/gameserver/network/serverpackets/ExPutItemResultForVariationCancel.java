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

/**
 * Format: (ch)ddd
 */
public class ExPutItemResultForVariationCancel extends L2GameServerPacket
{

	private int _itemObjId;
	private int _itemId;
	private int _itemAug1;
	private int _itemAug2;
	private int _price;

	public ExPutItemResultForVariationCancel(L2ItemInstance item, int price)
	{
		_itemObjId = item.getObjectId();
		_itemId = item.getItemId();
		_price = price;
		_itemAug1 = item.getAugmentation().getAugment1().getId();
		_itemAug2 = item.getAugmentation().getAugment2().getId();
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(_itemAug1);
		writeD(_itemAug2);
		writeQ(_price);
		writeD(0x01);
	}
}
