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

/**
 *
 * @author Erlandys
 */
public class ExResponseCommissionBuyInfo extends L2GameServerPacket
{
	
	@Override
	protected final void writeImpl()
	{
		writeD(0x01); // Unknown
		
		/*L2ItemInstance item = _auction.getItem();

		writeQ(_auction.getPrice());
		writeD(_auction.getCategory());
		writeD(0x00); // Unkown
		writeD(item.getItemId());
		writeQ(item.getCount());
		writeH(item.getItem().getType2());
		writeQ(item.getItem().getBodyPart());
		//writeH(item.getEnchantLevel());
		writeQ(item.getAugmentation() != null ? item.getAugmentation().getId() : 0x00);
		writeH(item.getAttackElementType());
		writeH(item.getAttackElementPower());
		for (byte d = 0; d < 6; d++)
			writeH(item.getElementDefAttr(d));

		writeH(0); // unknown
		writeH(0); // unknown
		writeH(0); // unknown*/
	}
}
