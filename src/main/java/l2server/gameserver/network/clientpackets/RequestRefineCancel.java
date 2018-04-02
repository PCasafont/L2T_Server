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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExVariationCancelResult;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.util.Util;

/**
 * Format(ch) d
 *
 * @author -Wooden-
 */
public final class RequestRefineCancel extends L2GameClientPacket {
	private int targetItemObjId;
	
	@Override
	protected void readImpl() {
		targetItemObjId = readD();
	}
	
	/**
	 */
	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		Item targetItem = activeChar.getInventory().getItemByObjectId(targetItemObjId);
		if (targetItem == null) {
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}
		if (targetItem.getOwnerId() != activeChar.getObjectId()) {
			Util.handleIllegalPlayerAction(getClient().getActiveChar(),
					"Warning!! Character " + getClient().getActiveChar().getName() + " of account " + getClient().getActiveChar().getAccountName() +
							" tryied to augment item that doesn't own.",
					Config.DEFAULT_PUNISH);
			return;
		}
		// cannot remove augmentation from a not augmented item
		if (!targetItem.isAugmented()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM));
			activeChar.sendPacket(new ExVariationCancelResult(0));
			return;
		}
		
		// get the price
		int price = 0;
		switch (targetItem.getItem().getCrystalType()) {
			case ItemTemplate.CRYSTAL_C:
				if (targetItem.getCrystalCount() < 1720) {
					price = 95000;
				} else if (targetItem.getCrystalCount() < 2452) {
					price = 150000;
				} else {
					price = 210000;
				}
				break;
			case ItemTemplate.CRYSTAL_B:
				if (targetItem.getCrystalCount() < 1746) {
					price = 240000;
				} else {
					price = 270000;
				}
				break;
			case ItemTemplate.CRYSTAL_A:
				if (targetItem.getCrystalCount() < 2160) {
					price = 330000;
				} else if (targetItem.getCrystalCount() < 2824) {
					price = 390000;
				} else {
					price = 420000;
				}
				break;
			case ItemTemplate.CRYSTAL_S:
				price = 480000;
				break;
			case ItemTemplate.CRYSTAL_S80:
			case ItemTemplate.CRYSTAL_S84:
				price = 920000;
				break;
			case ItemTemplate.CRYSTAL_R:
			case ItemTemplate.CRYSTAL_R95:
			case ItemTemplate.CRYSTAL_R99:
				price = 5300000;
				break;
			// any other item type is not augmentable
			default:
				activeChar.sendPacket(new ExVariationCancelResult(0));
				return;
		}
		
		if (Config.isServer(Config.TENKAI_LEGACY)) {
			price = (int) Math.sqrt(price);
		}
		
		// try to reduce the players adena
		if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true)) {
			activeChar.sendPacket(new ExVariationCancelResult(0));
			activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}
		
		// unequip item
		if (targetItem.isEquipped()) {
			Item[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (Item itm : unequiped) {
				iu.addModifiedItem(itm);
			}
			
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}
		
		// remove the augmentation
		targetItem.removeAugmentation();
		
		// send ExVariationCancelResult
		activeChar.sendPacket(new ExVariationCancelResult(1));
		
		// send inventory update
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);
		
		// Update shortcuts
		activeChar.updateItemShortCuts(targetItem.getObjectId());
	}
}
