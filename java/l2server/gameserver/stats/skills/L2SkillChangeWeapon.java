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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.InventoryUpdate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2Weapon;

/**
 * @author nBd
 */
public class L2SkillChangeWeapon extends L2Skill
{

	/**
	 * @param set
	 */
	public L2SkillChangeWeapon(StatsSet set)
	{
		super(set);
	}

	/**
	 * @see l2server.gameserver.model.L2Skill#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}

		if (!(caster instanceof L2PcInstance))
		{
			return;
		}

		L2PcInstance player = (L2PcInstance) caster;

		if (player.isEnchanting())
		{
			return;
		}

		L2Weapon weaponItem = player.getActiveWeaponItem();

		if (weaponItem == null)
		{
			return;
		}

		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		}

		if (wpn != null)
		{
			if (wpn.isAugmented())
			{
				return;
			}

			int newItemId = 0;
			int enchantLevel = 0;
			Elementals elementals = null;

			if (weaponItem.getChangeWeaponId() != 0)
			{
				newItemId = weaponItem.getChangeWeaponId();
				enchantLevel = wpn.getEnchantLevel();
				elementals = wpn.getElementals() == null ? null : wpn.getElementals()[0];

				if (newItemId == -1)
				{
					return;
				}

				L2ItemInstance[] unequiped =
						player.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
				InventoryUpdate iu = new InventoryUpdate();
				for (L2ItemInstance item : unequiped)
				{
					iu.addModifiedItem(item);
				}

				player.sendPacket(iu);

				if (unequiped.length > 0)
				{
					byte count = 0;

					for (L2ItemInstance item : unequiped)
					{
						if (!(item.getItem() instanceof L2Weapon))
						{
							count++;
							continue;
						}

						SystemMessage sm = null;
						if (item.getEnchantLevel() > 0)
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
							sm.addNumber(item.getEnchantLevel());
							sm.addItemName(item);
						}
						else
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
							sm.addItemName(item);
						}
						player.sendPacket(sm);
					}

					if (count == unequiped.length)
					{
						return;
					}
				}
				else
				{
					return;
				}

				long destroyedItemTime = wpn.getTime();

				L2ItemInstance destroyItem = player.getInventory().destroyItem("ChangeWeapon", wpn, player, null);

				if (destroyItem == null)
				{
					return;
				}

				L2ItemInstance newItem =
						player.getInventory().addItem("ChangeWeapon", newItemId, 1, player, destroyItem);

				if (newItem == null)
				{
					return;
				}

				if (destroyedItemTime != -1)
				{
					newItem.setTime(10080);
				}

				if (elementals != null && elementals.getElement() != -1 && elementals.getValue() != -1)
				{
					newItem.setElementAttr(elementals.getElement(), elementals.getValue());
				}
				newItem.setEnchantLevel(enchantLevel);
				player.getInventory().equipItem(newItem);

				SystemMessage msg = null;

				if (newItem.getEnchantLevel() > 0)
				{
					msg = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					msg.addNumber(newItem.getEnchantLevel());
					msg.addItemName(newItem);
				}
				else
				{
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
