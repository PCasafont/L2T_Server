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
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.L2TradeList.L2TradeItem;
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
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * RequestBuyItem client packet class.
 */
public final class RequestBuyItem extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 12; // length of the one item

	private int _listId;
	private Item[] _items = null;

	@Override
	protected void readImpl()
	{
		_listId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			if (itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, cnt);
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

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			player.sendMessage("You buying too fast.");
			return;
		}

		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getReputation() < 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object target = player.getTarget();
		L2Character merchant = null;
		if (!player.isGM())
		{
			if (target == null || !player.isInsideRadius(target, DEFAULT_INTERACTION_DISTANCE, true, false)
					// Distance is too far)
					||
					player.getInstanceId() != target.getInstanceId() && player.getObjectId() != target.getInstanceId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (target instanceof L2MerchantInstance || target instanceof L2MerchantSummonInstance)
			{
				merchant = (L2Character) target;
			}
			else
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		L2TradeList list = null;

		double castleTaxRate = 0;
		double baseTaxRate = 0;

		if (merchant != null)
		{
			List<L2TradeList> lists;
			if (merchant instanceof L2MerchantInstance)
			{
				lists = TradeController.getInstance().getBuyListByNpcId(((L2MerchantInstance) merchant).getNpcId());
				castleTaxRate = ((L2MerchantInstance) merchant).getMpc().getCastleTaxRate();
				baseTaxRate = ((L2MerchantInstance) merchant).getMpc().getBaseTaxRate();
			}
			else
			{
				lists = TradeController.getInstance()
						.getBuyListByNpcId(((L2MerchantSummonInstance) merchant).getNpcId());
				baseTaxRate = 50;
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

		_listId = list.getListId();

		long subTotal = 0;

		// Check for buylist validity and calculates summary values
		long slots = 0;
		long weight = 0;
		for (Item i : _items)
		{
			long price = -1;

			L2TradeItem tradeItem = list.getItemById(i.getItemId());
			if (tradeItem == null)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" sent a false BuyList list_id " + _listId + " and item_id " + i.getItemId(),
						Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(i.getItemId());
			if (template == null)
			{
				continue;
			}

			if (!template.isStackable() && i.getCount() > 1)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to purchase invalid quantity of items at the same time.",
						Config.DEFAULT_PUNISH);
				SystemMessage sm =
						SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}

			price = list.getPriceForItemId(i.getItemId());
			if (i.getItemId() >= 3960 && i.getItemId() <= 4026)
			{
				price *= Config.RATE_SIEGE_GUARDS_PRICE;
			}

			if (price < 0)
			{
				Log.warning("ERROR, no price found .. wrong buylist ??");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (price == 0 && !player.isGM() && Config.ONLY_GM_ITEMS_FREE)
			{
				player.sendMessage("Ohh Cheat dont work? You have a problem now!");
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
				return;
			}

			if (tradeItem.hasLimitedStock())
			{
				// trying to buy more then available
				if (i.getCount() > tradeItem.getCurrentCount())
				{
					return;
				}
			}

			if (MAX_ADENA / i.getCount() < price)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to purchase over " + MAX_ADENA + " adena worth of goods.",
						Config.DEFAULT_PUNISH);
				return;
			}
			// first calculate price per item with tax, then multiply by count
			price = (long) (price * (1 + castleTaxRate + baseTaxRate));
			subTotal += i.getCount() * price;
			if (subTotal > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to purchase over " + MAX_ADENA + " adena worth of goods.",
						Config.DEFAULT_PUNISH);
				return;
			}

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

		if (!player.isGM() &&
				(weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int) weight)))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!player.isGM() &&
				(slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity((int) slots)))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if (subTotal < 0 || !player.reduceAdena("Buy", subTotal, player.getLastFolkNPC(), false))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Proceed the purchase
		for (Item i : _items)
		{
			L2TradeItem tradeItem = list.getItemById(i.getItemId());
			if (tradeItem == null)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" sent a false BuyList list_id " + _listId + " and item_id " + i.getItemId(),
						Config.DEFAULT_PUNISH);
				continue;
			}

			if (tradeItem.hasLimitedStock())
			{
				if (tradeItem.decreaseCount(i.getCount()))
				{
					player.getInventory().addItem("Buy", i.getItemId(), i.getCount(), player, merchant);
				}
			}
			else
			{
				player.getInventory().addItem("Buy", i.getItemId(), i.getCount(), player, merchant);
			}
		}

		// add to castle treasury
		if (merchant instanceof L2MerchantInstance)
		{
			((L2MerchantInstance) merchant).getCastle().addToTreasury((long) (subTotal * castleTaxRate));
		}

		StatusUpdate su = new StatusUpdate(player);
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		player.sendPacket(new ExSellList(player, list, castleTaxRate + baseTaxRate, true));
	}

	private static class Item
	{
		private final int _itemId;
		private final long _count;

		public Item(int id, long num)
		{
			_itemId = id;
			_count = num;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public long getCount()
		{
			return _count;
		}
	}
}
