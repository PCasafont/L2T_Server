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
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.MercManagerInstance;
import l2server.gameserver.model.actor.instance.MerchantInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.ShopPreviewInfo;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.gameserver.templates.item.*;
import l2server.gameserver.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * @author Gnacik
 */
public final class RequestPreviewItem extends L2GameClientPacket {

	private Player activeChar;
	private Map<Integer, Integer> _item_list;
	@SuppressWarnings("unused")
	private int unk;
	private int listId;
	private int count;
	private int[] items;

	private class RemoveWearItemsTask implements Runnable {
		@Override
		public void run() {
			try {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_LONGER_TRYING_ON));
				activeChar.sendPacket(new UserInfo(activeChar));
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	protected void readImpl() {
		unk = readD();
		listId = readD();
		count = readD();

		if (count < 0) {
			count = 0;
		}
		if (count > 100) {
			return; // prevent too long lists
		}

		// Create items table that will contain all ItemID to Wear
		items = new int[count];

		// Fill items table with all ItemID to Wear
		for (int i = 0; i < count; i++) {
			items[i] = readD();
		}
	}

	@Override
	protected void runImpl() {
		if (items == null) {
			return;
		}

		// Get the current player and return if null
		activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("buy")) {
			activeChar.sendMessage("You buying too fast.");
			return;
		}

		// If Alternate rule Karma punishment is set to true, forbid Wear to player with Karma
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.getReputation() < 0) {
			return;
		}

		// Check current target of the player and the INTERACTION_DISTANCE
		WorldObject target = activeChar.getTarget();
		if (!activeChar.isGM() && (target == null || !(target instanceof MerchantInstance || target instanceof MercManagerInstance)
				// Target not a merchant and not mercmanager
				|| !activeChar.isInsideRadius(target, Npc.DEFAULT_INTERACTION_DISTANCE, false, false) // Distance is too far
		)) {
			return;
		}

		if (count < 1 || listId >= 4000000) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2TradeList list = null;

		// Get the current merchant targeted by the player
		MerchantInstance merchant = target instanceof MerchantInstance ? (MerchantInstance) target : null;

		List<L2TradeList> lists = TradeController.INSTANCE.getBuyListByNpcId(merchant.getNpcId());

		if (lists == null) {
			Util.handleIllegalPlayerAction(activeChar,
					"Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " sent a false BuyList list_id " +
							listId,
					Config.DEFAULT_PUNISH);
			return;
		}

		for (L2TradeList tradeList : lists) {
			if (tradeList.getListId() == listId) {
				list = tradeList;
			}
		}

		if (list == null) {
			Util.handleIllegalPlayerAction(activeChar,
					"Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " sent a false BuyList list_id " +
							listId,
					Config.DEFAULT_PUNISH);
			return;
		}

		long totalPrice = 0;
		listId = list.getListId();
		_item_list = new HashMap<>();

		for (int i = 0; i < count; i++) {
			int itemId = items[i];

			if (!list.containsItemId(itemId)) {
				Util.handleIllegalPlayerAction(activeChar,
						"Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() +
								" sent a false BuyList list_id " + listId + " and item_id " + itemId,
						Config.DEFAULT_PUNISH);
				return;
			}

			ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
			if (template == null) {
				continue;
			}

			int slot = Inventory.getPaperdollIndex(template.getBodyPart());
			if (slot < 0) {
				continue;
			}

			if (template instanceof WeaponTemplate) {
				if (activeChar.getRace().ordinal() == 5) {
					if (template.getItemType() == WeaponType.NONE) {
						continue;
					} else if (template.getItemType() == WeaponType.RAPIER || template.getItemType() == WeaponType.CROSSBOWK ||
							template.getItemType() == WeaponType.ANCIENTSWORD) {
						continue;
					}
				}
			} else if (template instanceof ArmorTemplate) {
				if (activeChar.getRace().ordinal() == 5) {
					if (template.getItemType() == ArmorType.HEAVY || template.getItemType() == ArmorType.MAGIC) {
						continue;
					}
				}
			}

			if (_item_list.containsKey(slot)) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME));
				return;
			} else {
				_item_list.put(slot, itemId);
			}

			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > PcInventory.MAX_ADENA) {
				Util.handleIllegalPlayerAction(activeChar,
						"Warning!! Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to purchase over " +
								PcInventory.MAX_ADENA + " adena worth of goods.",
						Config.DEFAULT_PUNISH);
				return;
			}
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if (totalPrice < 0 || !activeChar.reduceAdena("Wear", totalPrice, activeChar.getLastFolkNPC(), true)) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		if (!_item_list.isEmpty()) {
			activeChar.sendPacket(new ShopPreviewInfo(_item_list));
			// Schedule task
			ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(), Config.WEAR_DELAY * 1000);
		}
	}
}
