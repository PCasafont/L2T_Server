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

package l2server.gameserver.model;

import l2server.Config;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class CombatFlag {
	//

	protected Player player = null;
	public int playerId = 0;

	public Item itemInstance;

	public Location location;
	public int itemId;
	private Item item = null;

	// =========================================================
	// Constructor
	public CombatFlag(int x, int y, int z, int heading, int item_id) {
		location = new Location(x, y, z, heading);
		itemId = item_id;
	}

	public synchronized void spawnMe() {
		// Init the dropped Item and add it in the world as a visible object at the position where mob was last
		Item i = ItemTable.getInstance().createItem("Combat", itemId, 1, null, null);
		// Remove it from the world because spawnme will insert it again
		World.getInstance().removeObject(i);
		i.spawnMe(location.getX(), location.getY(), location.getZ());
		itemInstance = i;
	}

	public synchronized void unSpawnMe() {
		if (player != null) {
			dropIt();
		}

		if (itemInstance != null) {
			itemInstance.decayMe();
		}
	}

	public boolean activate(Player player, Item item) {
		if (player.isMounted()) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
			return false;
		}

		// Player holding it data
		this.player = player;
		playerId = player.getObjectId();
		itemInstance = null;

		// Equip with the weapon
		this.item = item;
		player.getInventory().equipItem(item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(item);
		player.sendPacket(sm);

		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE) {
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			player.sendPacket(iu);
		} else {
			player.sendPacket(new ItemList(player, false));
		}

		// Refresh player stats
		player.broadcastUserInfo();
		player.setCombatFlagEquipped(true);
		return true;
	}

	public void dropIt() {
		// Reset player stats
		player.setCombatFlagEquipped(false);
		int slot = player.getInventory().getSlotFromItem(item);
		player.getInventory().unEquipItemInBodySlot(slot);
		player.destroyItem("CombatFlag", item, null, true);
		item = null;
		player.broadcastUserInfo();
		player = null;
		playerId = 0;
	}
}
