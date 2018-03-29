/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model;

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;

import gnu.trove.TIntIntHashMap;

/**
 * @author Luno
 */
public final class L2ArmorSet
{
	private final int id;
	private final int parts;
	private final TIntIntHashMap skills;
	private final int shieldSkillId;
	private final int enchant6Skill;

	public L2ArmorSet(int id, int parts, TIntIntHashMap skills, int enchant6skill, int shield_skill_id)
	{
		this.id = id;
		this.parts = parts;
		this.skills = skills;

		shieldSkillId = shield_skill_id;

		this.enchant6Skill = enchant6skill;
	}

	/**
	 * Checks if player have equiped all items from set (not checking shield)
	 *
	 * @param player whose inventory is being checked
	 * @return True if player equips whole set
	 */
	public boolean containsAll(L2PcInstance player)
	{
		return countMissingParts(player) == 0;
	}

	public int countMissingParts(L2PcInstance player)
	{
		return parts - countParts(player);
	}

	private int countParts(L2PcInstance player)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		L2ItemInstance greaterItem1 = inv.getPaperdollItem(Inventory.PAPERDOLL_JEWELRY1);
		L2ItemInstance greaterItem2 = inv.getPaperdollItem(Inventory.PAPERDOLL_JEWELRY2);
		L2ItemInstance greaterItem3 = inv.getPaperdollItem(Inventory.PAPERDOLL_JEWELRY3);
		L2ItemInstance greaterItem4 = inv.getPaperdollItem(Inventory.PAPERDOLL_JEWELRY4);
		L2ItemInstance greaterItem5 = inv.getPaperdollItem(Inventory.PAPERDOLL_JEWELRY5);
		L2ItemInstance greaterItem6 = inv.getPaperdollItem(Inventory.PAPERDOLL_JEWELRY6);
		int count = 0;
		if (chestItem != null && chestItem.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (legsItem != null && legsItem.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (glovesItem != null && glovesItem.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (headItem != null && headItem.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (feetItem != null && feetItem.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (greaterItem1 != null && greaterItem1.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (greaterItem2 != null && greaterItem2.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (greaterItem3 != null && greaterItem3.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (greaterItem4 != null && greaterItem4.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (greaterItem5 != null && greaterItem5.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}
		if (greaterItem6 != null && greaterItem6.getArmorItem().isArmorSetPart(id))
		{
			count++;
		}

		return count;
	}

	public boolean containsShield(L2PcInstance player)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance shieldItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		return shieldItem != null && shieldItem.getArmorItem() != null && shieldItem.getArmorItem().isArmorSetPart(id);

	}

	public TIntIntHashMap getSkills()
	{
		return skills;
	}

	public int getShieldSkillId()
	{
		return shieldSkillId;
	}

	public int getEnchant6skillId()
	{
		return enchant6Skill;
	}

	/**
	 * Returns the minimum enchant level of the set for the given player
	 *
	 * @param player
	 * @return
	 */
	public int getEnchantLevel(L2PcInstance player)
	{
		if (!containsAll(player))
		{
			return 0;
		}

		Inventory inv = player.getInventory();

		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);

		int enchant = Integer.MAX_VALUE;
		if (chestItem != null && chestItem.getArmorItem().isArmorSetPart(id) && chestItem.getEnchantLevel() < enchant)
		{
			enchant = chestItem.getEnchantLevel();
		}
		if (legsItem != null && legsItem.getArmorItem().isArmorSetPart(id) && legsItem.getEnchantLevel() < enchant)
		{
			enchant = legsItem.getEnchantLevel();
		}
		if (glovesItem != null && glovesItem.getArmorItem().isArmorSetPart(id) &&
				glovesItem.getEnchantLevel() < enchant)
		{
			enchant = glovesItem.getEnchantLevel();
		}
		if (headItem != null && headItem.getArmorItem().isArmorSetPart(id) && headItem.getEnchantLevel() < enchant)
		{
			enchant = headItem.getEnchantLevel();
		}
		if (feetItem != null && feetItem.getArmorItem().isArmorSetPart(id) && feetItem.getEnchantLevel() < enchant)
		{
			enchant = feetItem.getEnchantLevel();
		}

		return enchant;
	}
}
