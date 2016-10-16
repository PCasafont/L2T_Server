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
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;
import lombok.Getter;

import static l2server.gameserver.model.actor.L2Npc.DEFAULT_INTERACTION_DISTANCE;

@Deprecated
public class RequestBuyProcure extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	@SuppressWarnings("unused") private int listId;
	private Procure[] items = null;

	@Override
	protected void readImpl()
	{
		listId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != buf.remaining())
		{
			return;
		}

		items = new Procure[count];
		for (int i = 0; i < count; i++)
		{
			readD(); //service
			int itemId = readD();
			long cnt = readQ();
			if (itemId < 1 || cnt < 1)
			{
				items = null;
				return;
			}
			items[i] = new Procure(itemId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getManor().tryPerformAction("BuyProcure"))
		{
			return;
		}

		if (items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getReputation() < 0)
		{
			return;
		}

		L2Object manager = player.getTarget();

		if (!(manager instanceof L2ManorManagerInstance))
		{
			manager = player.getLastFolkNPC();
		}

		if (!(manager instanceof L2ManorManagerInstance))
		{
			return;
		}

		if (!player.isInsideRadius(manager, DEFAULT_INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		Castle castle = ((L2ManorManagerInstance) manager).getCastle();
		int slots = 0;
		int weight = 0;

		for (Procure i : items)
		{
			i.setReward(castle);

			L2Item template = ItemTable.getInstance().getTemplate(i.getReward());
			weight += i.getCount() * template.getWeight();

			if (!template.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(i.getItemId()) == null)
			{
				slots++;
			}
		}

		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}

		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();

		for (Procure i : items)
		{
			// check if player have correct items count
			L2ItemInstance item = player.getInventory().getItemByItemId(i.getItemId());
			if (item == null || item.getCount() < i.getCount())
			{
				continue;
			}

			L2ItemInstance iteme =
					player.getInventory().destroyItemByItemId("Manor", i.getItemId(), i.getCount(), player, manager);
			if (iteme == null)
			{
				continue;
			}

			// Add item to Inventory and adjust update packet
			item = player.getInventory().addItem("Manor", i.getReward(), i.getCount(), player, manager);
			if (item == null)
			{
				continue;
			}

			playerIU.addRemovedItem(iteme);
			if (item.getCount() > i.getCount())
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}

			// Send Char Buy Messages
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(item);
			sm.addItemNumber(i.getCount());
			player.sendPacket(sm);
			sm = null;

			//manor.getCastle().setCropAmount(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getAmount() - count);
		}

		// Send update packets
		player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	private static class Procure
	{
		@Getter private final int itemId;
		@Getter private final long count;
		@Getter private int reward;

		public Procure(int id, long num)
		{
			itemId = id;
			count = num;
		}

		public void setReward(Castle c)
		{
			reward = L2Manor.getInstance()
					.getRewardItem(itemId, c.getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());
		}
	}
}
