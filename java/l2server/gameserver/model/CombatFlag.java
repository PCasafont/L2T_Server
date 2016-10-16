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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class CombatFlag
{
	//

	protected L2PcInstance player = null;
	public int playerId = 0;

	public L2ItemInstance itemInstance;

	public Location location;
	public int itemId;
	private L2ItemInstance item = null;

	// =========================================================
	// Constructor
	public CombatFlag(int x, int y, int z, int heading, int item_id)
	{
		location = new Location(x, y, z, heading);
		itemId = item_id;
	}

	public synchronized void spawnMe()
	{
		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		L2ItemInstance i = ItemTable.getInstance().createItem("Combat", itemId, 1, null, null);
		// Remove it from the world because spawnme will insert it again
		L2World.getInstance().removeObject(i);
		i.spawnMe(location.getX(), location.getY(), location.getZ());
		itemInstance = i;
	}

	public synchronized void unSpawnMe()
	{
		if (player != null)
		{
			dropIt();
		}

		if (itemInstance != null)
		{
			itemInstance.decayMe();
		}
	}

	public boolean activate(L2PcInstance player, L2ItemInstance item)
	{
		if (player.isMounted())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION));
			return false;
		}

		// Player holding it data
		this.player = player;
		playerId = this.player.getObjectId();
		itemInstance = null;

		// Equip with the weapon
		this.item = item;
		this.player.getInventory().equipItem(this.item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(this.item);
		this.player.sendPacket(sm);

		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(this.item);
			this.player.sendPacket(iu);
		}
		else
		{
			this.player.sendPacket(new ItemList(this.player, false));
		}

		// Refresh player stats
		this.player.broadcastUserInfo();
		this.player.setCombatFlagEquipped(true);
		return true;
	}

	public void dropIt()
	{
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
