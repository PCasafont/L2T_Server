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
import l2server.gameserver.model.actor.instance.Player;

import java.util.ArrayList;

/**
 * @author Erlandys
 */
public class ExChangeAttributeItemList extends L2ItemListPacket {
	
	private ArrayList<Item> itemsList;
	private int itemOID;
	
	public ExChangeAttributeItemList(Player player, int itemOID) {
		itemsList = new ArrayList<>();
		for (Item item : player.getInventory().getItems()) {
			if (item.isWeapon()) {
				if (item.getAttackElementPower() > 0) {
					itemsList.add(item);
				}
			}
		}
		this.itemOID = itemOID;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(itemOID);
		writeD(itemsList.size());
		for (Item item : itemsList) {
			writeItem(item);
		}
	}
}
