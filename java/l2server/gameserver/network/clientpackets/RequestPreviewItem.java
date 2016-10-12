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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.TradeController;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2MercManagerInstance;
import l2server.gameserver.model.actor.instance.L2MerchantInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ShopPreviewInfo;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * * @author Gnacik
 */
public final class RequestPreviewItem extends L2GameClientPacket
{

	private L2PcInstance _activeChar;
	private Map<Integer, Integer> _item_list;
	@SuppressWarnings("unused")
	private int _unk;
	private int _listId;
	private int _count;
	private int[] _items;

	private class RemoveWearItemsTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_LONGER_TRYING_ON));
				_activeChar.sendPacket(new UserInfo(_activeChar));
			}
			catch (Exception e)
			{
				Log.log(Level.SEVERE, "", e);
			}
		}
	}

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_listId = readD();
		_count = readD();

		if (_count < 0)
		{
			_count = 0;
		}
		if (_count > 100)
		{
			return; // prevent too long lists
		}

		// Create _items table that will contain all ItemID to Wear
		_items = new int[_count];

		// Fill _items table with all ItemID to Wear
		for (int i = 0; i < _count; i++)
		{
			_items[i] = readD();
		}
	}

	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}

		// Get the current player and return if null
		_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy"))
		{
			_activeChar.sendMessage("You buying too fast.");
			return;
		}

		// If Alternate rule Karma punishment is set to true, forbid Wear to player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && _activeChar.getReputation() < 0)
		{
			return;
		}

		// Check current target of the player and the INTERACTION_DISTANCE
		L2Object target = _activeChar.getTarget();
		if (!_activeChar.isGM() &&
				(target == null || !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance)
						// Target not a merchant and not mercmanager
						|| !_activeChar
						.isInsideRadius(target, L2Npc.DEFAULT_INTERACTION_DISTANCE, false, false) // Distance is too far
				))
		{
			return;
		}

		if (_count < 1 || _listId >= 4000000)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2TradeList list = null;

		// Get the current merchant targeted by the player
		L2MerchantInstance merchant = target instanceof L2MerchantInstance ? (L2MerchantInstance) target : null;

		List<L2TradeList> lists = TradeController.getInstance().getBuyListByNpcId(merchant.getNpcId());

		if (lists == null)
		{
			Util.handleIllegalPlayerAction(_activeChar,
					"Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() +
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

		if (list == null)
		{
			Util.handleIllegalPlayerAction(_activeChar,
					"Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() +
							" sent a false BuyList list_id " + _listId, Config.DEFAULT_PUNISH);
			return;
		}

		long totalPrice = 0;
		_listId = list.getListId();
		_item_list = new HashMap<>();

		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];

			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(_activeChar,
						"Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() +
								" sent a false BuyList list_id " + _listId + " and item_id " + itemId,
						Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(itemId);
			if (template == null)
			{
				continue;
			}

			int slot = Inventory.getPaperdollIndex(template.getBodyPart());
			if (slot < 0)
			{
				continue;
			}

			if (template instanceof L2Weapon)
			{
				if (_activeChar.getRace().ordinal() == 5)
				{
					if (template.getItemType() == L2WeaponType.NONE)
					{
						continue;
					}
					else if (template.getItemType() == L2WeaponType.RAPIER ||
							template.getItemType() == L2WeaponType.CROSSBOWK ||
							template.getItemType() == L2WeaponType.ANCIENTSWORD)
					{
						continue;
					}
				}
			}
			else if (template instanceof L2Armor)
			{
				if (_activeChar.getRace().ordinal() == 5)
				{
					if (template.getItemType() == L2ArmorType.HEAVY || template.getItemType() == L2ArmorType.MAGIC)
					{
						continue;
					}
				}
			}

			if (_item_list.containsKey(slot))
			{
				_activeChar.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME));
				return;
			}
			else
			{
				_item_list.put(slot, itemId);
			}

			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > PcInventory.MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(_activeChar,
						"Warning!! Character " + _activeChar.getName() + " of account " + _activeChar.getAccountName() +
								" tried to purchase over " + PcInventory.MAX_ADENA + " adena worth of goods.",
						Config.DEFAULT_PUNISH);
				return;
			}
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if (totalPrice < 0 || !_activeChar.reduceAdena("Wear", totalPrice, _activeChar.getLastFolkNPC(), true))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		if (!_item_list.isEmpty())
		{
			_activeChar.sendPacket(new ShopPreviewInfo(_item_list));
			// Schedule task
			ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
		}
	}
}
