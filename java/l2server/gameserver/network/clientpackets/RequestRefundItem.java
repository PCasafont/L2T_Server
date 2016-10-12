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
import l2server.gameserver.TradeController;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2MerchantInstance;
import l2server.gameserver.model.actor.instance.L2MerchantSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ExSellList;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.List;

import static l2server.gameserver.model.actor.L2Npc.DEFAULT_INTERACTION_DISTANCE;

/**
 * RequestRefundItem client packet class.
 */
public final class RequestRefundItem extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 4; // length of the one item

	private int _listId;
	private int[] _items = null;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		final int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new int[count];
		for (int i = 0; i < count; i++)
		{
			_items[i] = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("refund"))
		{
			player.sendMessage("You using refund too fast.");
			return;
		}

		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!player.hasRefund())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object target = player.getTarget();
		if (!player.isGM() && (target == null ||
				!(target instanceof L2MerchantInstance || target instanceof L2MerchantSummonInstance) ||
				player.getInstanceId() != target.getInstanceId() ||
				!player.isInsideRadius(target, DEFAULT_INTERACTION_DISTANCE, true, false))) // Distance is too far
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Character merchant = null;
		if (target instanceof L2MerchantInstance || target instanceof L2MerchantSummonInstance)
		{
			merchant = (L2Character) target;
		}
		else if (!player.isGM())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2TradeList list = null;
		double taxRate = 0;

		if (merchant != null)
		{
			List<L2TradeList> lists;
			if (merchant instanceof L2MerchantInstance)
			{
				lists = TradeController.getInstance().getBuyListByNpcId(((L2MerchantInstance) merchant).getNpcId());
				taxRate = ((L2MerchantInstance) merchant).getMpc().getTotalTaxRate();
			}
			else
			{
				lists = TradeController.getInstance()
						.getBuyListByNpcId(((L2MerchantSummonInstance) merchant).getNpcId());
				taxRate = 50;
			}

			if (!player.isGM())
			{
				if (lists == null)
				{
					Util.handleIllegalPlayerAction(player,
							"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
									" sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
					return;
				}
				for (L2TradeList tradeList : lists)
				{
					if (tradeList.getListId() == _listId)
					{
						list = tradeList;
					}
				}
			}
			else
			{
				list = TradeController.getInstance().getBuyList(_listId);
			}
		}
		else
		{
			list = TradeController.getInstance().getBuyList(_listId);
		}

		if (list == null)
		{
			Util.handleIllegalPlayerAction(player,
					"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
							" sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}

		long weight = 0;
		long adena = 0;
		long slots = 0;

		L2ItemInstance[] refund = player.getRefund().getItems();
		int[] objectIds = new int[_items.length];

		for (int i = 0; i < _items.length; i++)
		{
			int idx = _items[i];
			if (idx < 0 || idx >= refund.length)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" sent invalid refund index", Config.DEFAULT_PUNISH);
				return;
			}

			// check for duplicates - indexes
			for (int j = i + 1; j < _items.length; j++)
			{
				if (idx == _items[j])
				{
					Util.handleIllegalPlayerAction(player,
							"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
									" sent duplicate refund index", Config.DEFAULT_PUNISH);
					return;
				}
			}

			final L2ItemInstance item = refund[idx];
			final L2Item template = item.getItem();
			objectIds[i] = item.getObjectId();

			// second check for duplicates - object ids
			for (int j = 0; j < i; j++)
			{
				if (objectIds[i] == objectIds[j])
				{
					Util.handleIllegalPlayerAction(player,
							"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
									" has duplicate items in refund list", Config.DEFAULT_PUNISH);
					return;
				}
			}

			long count = item.getCount();
			weight += count * template.getWeight();
			adena += count * template.getSalePrice();
			if (!template.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(template.getItemId()) == null)
			{
				slots++;
			}
		}

		if (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int) weight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity((int) slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (adena < 0 || !player.reduceAdena("Refund", adena, player.getLastFolkNPC(), false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		for (int i = 0; i < _items.length; i++)
		{
			L2ItemInstance item = player.getRefund()
					.transferItem("Refund", objectIds[i], Long.MAX_VALUE, player.getInventory(), player,
							player.getLastFolkNPC());
			if (item == null)
			{
				Log.warning("Error refunding object for char " + player.getName() + " (newitem == null)");
			}
		}

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ExSellList(player, list, taxRate, true));
	}
}
