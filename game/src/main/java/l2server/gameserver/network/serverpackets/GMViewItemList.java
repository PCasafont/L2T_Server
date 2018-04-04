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
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;

/**
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewItemList extends L2ItemListPacket {
	//
	private Item[] items;
	private int limit;
	private String playerName;
	
	public GMViewItemList(Player cha) {
		items = cha.getInventory().getItems();
		playerName = cha.getName();
		limit = cha.getInventoryLimit();
	}
	
	public GMViewItemList(PetInstance cha) {
		items = cha.getInventory().getItems();
		playerName = cha.getName();
		limit = cha.getInventoryLimit();
	}
	
	@Override
	protected final void writeImpl() {
		writeS(playerName);
		writeD(limit); // inventory limit
		writeH(0x01); // show window ??
		writeH(items.length);
		
		for (Item item : items) {
			writeItem(item);
		}
		
		writeH(0x00);
	}
}
