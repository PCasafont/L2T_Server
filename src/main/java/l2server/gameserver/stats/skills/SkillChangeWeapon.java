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

package l2server.gameserver.stats.skills;

import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.WeaponTemplate;

/**
 * @author nBd
 */
public class SkillChangeWeapon extends Skill {

	/**
	 * @param set
	 */
	public SkillChangeWeapon(StatsSet set) {
		super(set);
	}

	/**
	 * @see Skill#useSkill(Creature, WorldObject[])
	 */
	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		if (caster.isAlikeDead()) {
			return;
		}

		if (!(caster instanceof Player)) {
			return;
		}

		Player player = (Player) caster;

		if (player.isEnchanting()) {
			return;
		}

		WeaponTemplate weaponItem = player.getActiveWeaponItem();

		if (weaponItem == null) {
			return;
		}

		Item wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null) {
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		}

		if (wpn != null) {
			if (wpn.isAugmented()) {
				return;
			}

			int newItemId = 0;
			int enchantLevel = 0;
			Elementals elementals = null;

			if (weaponItem.getChangeWeaponId() != 0) {
				newItemId = weaponItem.getChangeWeaponId();
				enchantLevel = wpn.getEnchantLevel();
				elementals = wpn.getElementals() == null ? null : wpn.getElementals()[0];

				if (newItemId == -1) {
					return;
				}

				Item[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
				InventoryUpdate iu = new InventoryUpdate();
				for (Item item : unequiped) {
					iu.addModifiedItem(item);
				}

				player.sendPacket(iu);

				if (unequiped.length > 0) {
					byte count = 0;

					for (Item item : unequiped) {
						if (!(item.getItem() instanceof WeaponTemplate)) {
							count++;
							continue;
						}

						SystemMessage sm = null;
						if (item.getEnchantLevel() > 0) {
							sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
							sm.addNumber(item.getEnchantLevel());
							sm.addItemName(item);
						} else {
							sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
							sm.addItemName(item);
						}
						player.sendPacket(sm);
					}

					if (count == unequiped.length) {
						return;
					}
				} else {
					return;
				}

				long destroyedItemTime = wpn.getTime();

				Item destroyItem = player.getInventory().destroyItem("ChangeWeapon", wpn, player, null);

				if (destroyItem == null) {
					return;
				}

				Item newItem = player.getInventory().addItem("ChangeWeapon", newItemId, 1, player, destroyItem);

				if (newItem == null) {
					return;
				}

				if (destroyedItemTime != -1) {
					newItem.setTime(10080);
				}

				if (elementals != null && elementals.getElement() != -1 && elementals.getValue() != -1) {
					newItem.setElementAttr(elementals.getElement(), elementals.getValue());
				}
				newItem.setEnchantLevel(enchantLevel);
				player.getInventory().equipItem(newItem);

				SystemMessage msg = null;

				if (newItem.getEnchantLevel() > 0) {
					msg = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					msg.addNumber(newItem.getEnchantLevel());
					msg.addItemName(newItem);
				} else {
					msg = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
					msg.addItemName(newItem);
				}
				player.sendPacket(msg);

				InventoryUpdate u = new InventoryUpdate();
				u.addRemovedItem(destroyItem);
				u.addItem(newItem);
				player.sendPacket(u);

				player.broadcastUserInfo();
			}
		}
	}
}
