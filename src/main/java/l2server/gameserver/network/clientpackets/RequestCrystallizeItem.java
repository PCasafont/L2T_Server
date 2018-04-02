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
import l2server.gameserver.model.L2CrystallizeReward;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.base.Race;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestCrystallizeItem extends L2GameClientPacket {
	
	private int objectId;
	private long count;
	
	@Override
	protected void readImpl() {
		objectId = readD();
		count = readQ();
	}
	
	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null) {
			log.debug("RequestCrystalizeItem: activeChar was null");
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("crystallize")) {
			activeChar.sendMessage("You crystallizing too fast.");
			return;
		}
		
		if (activeChar.isInJail()) {
			return;
		}
		
		if (count <= 0) {
			Util.handleIllegalPlayerAction(activeChar,
					"[RequestCrystallizeItem] count <= 0! ban! oid: " + objectId + " owner: " + activeChar.getName(),
					Config.DEFAULT_PUNISH);
			return;
		}
		
		if (activeChar.getPrivateStoreType() != 0 || activeChar.isInCrystallize()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		int skillLevel = activeChar.getSkillLevelHash(Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			if (activeChar.getRace() != Race.Dwarf && activeChar.getCurrentClass().getId() != 117 && activeChar.getCurrentClass().getId() != 55) {
				log.info("Player " + activeChar.getClient() + " used crystalize with classid: " + activeChar.getCurrentClass().getId());
			}
			return;
		}
		
		PcInventory inventory = activeChar.getInventory();
		if (inventory != null) {
			Item item = inventory.getItemByObjectId(objectId);
			if (item == null) {
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (item.isHeroItem()) {
				return;
			}
			
			if (count > item.getCount()) {
				count = activeChar.getInventory().getItemByObjectId(objectId).getCount();
			}
		}
		
		Item itemToRemove = activeChar.getInventory().getItemByObjectId(objectId);
		if (itemToRemove == null || itemToRemove.isShadowItem() || itemToRemove.isTimeLimitedItem()) {
			return;
		}
		
		if (!itemToRemove.getItem().isCrystallizable() || itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_NONE) {
			log.warn(activeChar.getName() + " (" + activeChar.getObjectId() + ") tried to crystallize " + itemToRemove.getItem().getItemId());
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId())) {
			activeChar.sendMessage("Cannot use this item.");
			return;
		}
		
		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;
		
		switch (itemToRemove.getItem().getItemGradePlain()) {
			case ItemTemplate.CRYSTAL_C: {
				if (skillLevel <= 1) {
					canCrystallize = false;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_B: {
				if (skillLevel <= 2) {
					canCrystallize = false;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_A: {
				if (skillLevel <= 3) {
					canCrystallize = false;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_S: {
				if (skillLevel <= 4) {
					canCrystallize = false;
				}
				break;
			}
			case ItemTemplate.CRYSTAL_R: {
				if (skillLevel <= 5) {
					canCrystallize = false;
				}
				break;
			}
		}
		
		if (!canCrystallize) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.setInCrystallize(true);
		
		// unequip if needed
		if (itemToRemove.isEquipped()) {
			Item[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (Item item : unequiped) {
				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);
			
			SystemMessage msg;
			if (itemToRemove.getEnchantLevel() > 0) {
				msg = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				msg.addNumber(itemToRemove.getEnchantLevel());
				msg.addItemName(itemToRemove);
			} else {
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				msg.addItemName(itemToRemove);
			}
			activeChar.sendPacket(msg);
		}
		
		// remove from inventory
		Item removedItem = activeChar.getInventory().destroyItem("Crystalize", objectId, count, activeChar, null);
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(removedItem);
		activeChar.sendPacket(iu);
		
		// add crystals
		int crystalId = itemToRemove.getItem().getCrystalItemId();
		int crystalAmount = itemToRemove.getCrystalCount();
		Item createditem = activeChar.getInventory().addItem("Crystalize", crystalId, crystalAmount, activeChar, activeChar);
		
		SystemMessage sm;
		sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED);
		sm.addItemName(removedItem);
		activeChar.sendPacket(sm);
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(createditem);
		sm.addItemNumber(crystalAmount);
		activeChar.sendPacket(sm);
		
		if (Config.ENABLE_CRYSTALLIZE_REWARDS && itemToRemove.getItem().getCrystallizeRewards() != null) {
			for (L2CrystallizeReward reward : itemToRemove.getItem().getCrystallizeRewards()) {
				if (reward.getChance() * 1000 > Rnd.get(100000)) {
					activeChar.addItem("Crystallize", reward.getItemId(), reward.getCount(), activeChar, true);
				}
			}
		}
		
		activeChar.broadcastUserInfo();
		
		World world = World.getInstance();
		world.removeObject(removedItem);
		
		activeChar.setInCrystallize(false);
	}
}
