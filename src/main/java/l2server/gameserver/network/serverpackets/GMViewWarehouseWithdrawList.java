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

import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.Player;

/**
 * Sdh(h dddhh [dhhh] d)
 * Sdh ddddd ddddd ddddd ddddd
 *
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList extends L2ItemListPacket {
	private Item[] items;
	private String playerName;
	private Player activeChar;
	private long money;
	
	public GMViewWarehouseWithdrawList(Player cha) {
		activeChar = cha;
		items = activeChar.getWarehouse().getItems();
		playerName = activeChar.getName();
		money = activeChar.getWarehouse().getAdena();
	}
	
	public GMViewWarehouseWithdrawList(L2Clan clan) {
		playerName = clan.getLeaderName();
		items = clan.getWarehouse().getItems();
		money = clan.getWarehouse().getAdena();
	}
	
	@Override
	protected final void writeImpl() {
		writeS(playerName);
		writeQ(money);
		writeH(items.length);
		writeD(0x00); // GoD ???
		
		for (Item item : items) {
			writeItem(item);
		}
	}
}
