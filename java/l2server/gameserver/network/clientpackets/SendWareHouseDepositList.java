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

import l2server.Config;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.ItemContainer;
import l2server.gameserver.model.itemcontainer.PcWarehouse;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import static l2server.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

/**
 * This class ...
 * <p>
 * 31  SendWareHouseDepositList  cd (dd)
 *
 * @version $Revision: 1.3.4.5 $ $Date: 2005/04/11 10:06:09 $
 */
public final class SendWareHouseDepositList extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 12; // length of the one item

	private WarehouseItem _items[] = null;

	@Override
	protected void readImpl()
	{
		final int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new WarehouseItem[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			long cnt = readQ();
			if (objId < 1 || cnt < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new WarehouseItem(objId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}

		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}

		final ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
		{
			return;
		}
		final boolean isPrivate = warehouse instanceof PcWarehouse;

		final L2Npc manager = player.getLastFolkNPC();
		if ((manager == null || !manager.isWarehouse() || !manager.canInteract(player)) && !player.isGM())
		{
			return;
		}

		if (!isPrivate && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			return;
		}

		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to use enchant Exploit!",
					Config.DEFAULT_PUNISH);
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getReputation() < 0)
		{
			return;
		}

		// Freight price from config or normal price per item slot (30)
		final long fee = _items.length * 30;
		long currentAdena = player.getAdena();
		int slots = 0;

		for (WarehouseItem i : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "deposit");
			if (item == null)
			{
				Log.warning("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
				return;
			}

			// Calculate needed adena and slots
			if (item.getItemId() == ADENA_ID)
			{
				currentAdena -= i.getCount();
			}
			if (!item.isStackable())
			{
				slots += i.getCount();
			}
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
			{
				slots++;
			}
		}

		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		// Check if enough adena and charge the fee
		if (currentAdena < fee ||
				!player.reduceAdena(warehouse.getOwnerId() + "'s " + warehouse.getName(), fee, manager, false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		// get current tradelist if any
		if (player.getActiveTradeList() != null)
		{
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (WarehouseItem i : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(i.getObjectId(), i.getCount(), "deposit");
			if (oldItem == null)
			{
				Log.warning("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
				return;
			}

			if (!oldItem.isDepositable(isPrivate) || !oldItem.isAvailable(player, true, isPrivate))
			{
				continue;
			}

			final L2ItemInstance newItem = player.getInventory()
					.transferItem(warehouse.getName(), i.getObjectId(), i.getCount(), warehouse, player, manager);
			if (newItem == null)
			{
				Log.warning("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
				continue;
			}

			if (playerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
				{
					playerIU.addModifiedItem(oldItem);
				}
				else
				{
					playerIU.addRemovedItem(oldItem);
				}
			}
		}

		// Send updated item list to the player
		if (playerIU != null)
		{
			player.sendPacket(playerIU);
		}
		else
		{
			player.sendPacket(new ItemList(player, false));
		}

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	private static class WarehouseItem
	{
		private final int _objectId;
		private final long _count;

		public WarehouseItem(int id, long num)
		{
			_objectId = id;
			_count = num;
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public long getCount()
		{
			return _count;
		}
	}
}
