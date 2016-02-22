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

import l2server.gameserver.model.TradeList;

/**
 * This class ...
 *
 * @author Yme
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class TradeOwnAdd extends L2GameServerPacket
{
	private TradeList.TradeItem _item;
	
	public TradeOwnAdd(TradeList.TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeH(1); // items added count
		writeH(0);
		writeD(_item.getObjectId());
		writeD(_item.getItem().getItemId());
		writeQ(_item.getCount());
		writeH(_item.getItem().getType2()); // item type2
		
		writeQ(_item.getItem().getBodyPart()); // rev 415  slot	0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
		writeH(_item.getEnchantLevel()); // enchant level
		writeH(0x00);
		
		writeD(_item.getAppearance());
		//writeH(0x01); // GoD ???
		
		// T1
		writeH(_item.getAttackElementType());
		writeH(_item.getAttackElementPower());
		
		writeH(0x00); // If will be > 0 you will see only fire attribute
		
		for (byte i = 0; i < 6; i++)
			writeH(_item.getElementDefAttr(i));
		
		writeH(0x00); // Enchant effect 1
		writeH(0x00); // Enchant effect 2
		writeH(0x00); // Enchant effect 3
	}
}
