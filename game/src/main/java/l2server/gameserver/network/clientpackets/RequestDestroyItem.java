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
import l2server.DatabasePool;
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.instancemanager.CursedWeaponsManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.L2CrystallizeReward;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * This class ...
 *
 * @version $Revision: 1.7.2.4.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestDestroyItem extends L2GameClientPacket {

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
			return;
		}

		if (count <= 0) {
			if (count < 0) {
				Util.handleIllegalPlayerAction(activeChar,
						"[RequestDestroyItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() +
								" tried to destroy item with oid " + objectId + " but has count < 0!",
						Config.DEFAULT_PUNISH);
			}
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("destroy")) {
			activeChar.sendMessage("You destroying items too fast.");
			return;
		}

		if (activeChar.isInJail()) {
			return;
		}

		long count = this.count;

		if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		Item itemToRemove = activeChar.getInventory().getItemByObjectId(objectId);
		// if we can't find the requested item, its actually a cheat
		if (itemToRemove == null) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}

		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow()) {
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == itemToRemove.getItemId()) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingSimultaneouslyNow()) {
			if (activeChar.getLastSimultaneousSkillCast() != null &&
					activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == itemToRemove.getItemId()) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}

		int itemId = itemToRemove.getItemId();

		if (!activeChar.isGM() && !itemToRemove.isDestroyable() || CursedWeaponsManager.getInstance().isCursed(itemId)) {
			if (itemToRemove.isHeroItem()) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HERO_WEAPONS_CANT_DESTROYED));
			} else {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			}
			return;
		}

		if (!itemToRemove.isStackable() && count > 1) {
			Util.handleIllegalPlayerAction(activeChar,
					"[RequestDestroyItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() +
							" tried to destroy a non-stackable item with oid " + objectId + " but has count > 1!",
					Config.DEFAULT_PUNISH);
			return;
		}

		if (!activeChar.getInventory().canManipulateWithItemId(itemToRemove.getItemId())) {
			activeChar.sendMessage("Cannot use this item.");
			return;
		}

		if (count > itemToRemove.getCount()) {
			count = itemToRemove.getCount();
		}

		if (itemToRemove.isEquipped()) {
			Item[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (Item item : unequiped) {
				activeChar.checkSShotsMatch(null, item);

				iu.addModifiedItem(item);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
		}

		if (PetDataTable.isPetItem(itemId)) {
			Connection con = null;
			try {
				if (activeChar.getPet() != null && activeChar.getPet().getControlObjectId() == objectId) {
					activeChar.getPet().unSummon(activeChar);
				}

				// if it's a pet control item, delete the pet
				con = DatabasePool.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
				statement.setInt(1, objectId);
				statement.execute();
				statement.close();
			} catch (Exception e) {
				log.warn("could not delete pet objectid: ", e);
			} finally {
				DatabasePool.close(con);
			}
		}
		if (itemToRemove.isTimeLimitedItem()) {
			itemToRemove.endOfLife();
		}

		// Crystallize the item instead of destroying it, if possible
		int skillLevel = activeChar.getSkillLevelHash(Skill.SKILL_CRYSTALLIZE);
		boolean hasBeenCrystallized = false;
		if (skillLevel > 0 && itemToRemove.getItem().isCrystallizable() && itemToRemove.getCrystalCount() > 0 &&
				!(itemToRemove.getItem().getCrystalType() == ItemTemplate.CRYSTAL_NONE) && !itemToRemove.isEquipped()) {
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
			}

			if (canCrystallize) {
				// remove from inventory
				Item removedItem = activeChar.getInventory().destroyItem("Crystalize", objectId, count, activeChar, null);
				if (removedItem == null) {
					return;
				}

				activeChar.setInCrystallize(true);

				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(removedItem);
				activeChar.sendPacket(iu);

				// add crystals
				int crystalId = itemToRemove.getItem().getCrystalItemId();
				int crystalAmount = itemToRemove.getCrystalCount();
				Item createditem = activeChar.getInventory().addItem("Crystallize", crystalId, crystalAmount, activeChar, activeChar);

				SystemMessage sm;
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED);
				sm.addItemName(removedItem);
				activeChar.sendPacket(sm);

				sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(createditem);
				sm.addItemNumber(crystalAmount);
				activeChar.sendPacket(sm);

				if (Config.ENABLE_CRYSTALLIZE_REWARDS) {
					for (L2CrystallizeReward reward : itemToRemove.getItem().getCrystallizeRewards()) {
						if (reward.getChance() * 1000 > Rnd.get(100000)) {
							activeChar.addItem("Crystallize", reward.getItemId(), reward.getCount(), activeChar, true);
						}
					}
				}

				activeChar.broadcastUserInfo();

				World world = World.getInstance();
				world.removeObject(removedItem);

				hasBeenCrystallized = true;

				activeChar.setInCrystallize(false);
			}
		}

		if (hasBeenCrystallized) {
			return;
		}

		Item removedItem = activeChar.getInventory().destroyItem("Destroy", objectId, count, activeChar, null);

		if (removedItem == null) {
			return;
		}

		if (!Config.FORCE_INVENTORY_UPDATE) {
			InventoryUpdate iu = new InventoryUpdate();
			if (removedItem.getCount() == 0) {
				iu.addRemovedItem(removedItem);
			} else {
				iu.addModifiedItem(removedItem);
			}

			//client.getConnection().sendPacket(iu);
			activeChar.sendPacket(iu);
		} else {
			sendPacket(new ItemList(activeChar, true));
		}

		StatusUpdate su = new StatusUpdate(activeChar);
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
	}
}
