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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.Item;
import l2server.gameserver.model.Item.ItemLocation;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Format:(ch) d[dd]
 *
 * @author -Wooden-
 */
public final class RequestSaveInventoryOrder extends L2GameClientPacket {
	private List<InventoryOrder> order;
	
	/**
	 * client limit
	 */
	private static final int LIMIT = 125;
	
	/**
	 * @see L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl() {
		int sz = readD();
		sz = Math.min(sz, LIMIT);
		order = new ArrayList<>(sz);
		for (int i = 0; i < sz; i++) {
			int objectId = readD();
			int order = readD();
			this.order.add(new InventoryOrder(objectId, order));
		}
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player != null) {
			Inventory inventory = player.getInventory();
			for (InventoryOrder order : order) {
				Item item = inventory.getItemByObjectId(order.objectID);
				if (item != null && item.getLocation() == ItemLocation.INVENTORY) {
					item.setLocation(ItemLocation.INVENTORY, order.order);
				}
			}
		}
	}
	
	private static class InventoryOrder {
		int order;
		
		int objectID;
		
		/**
		 *
		 */
		public InventoryOrder(int id, int ord) {
			objectID = id;
			order = ord;
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest() {
		return false;
	}
}
