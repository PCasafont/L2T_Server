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

import gnu.trove.TIntIntHashMap;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;

/**
 * @author Luno
 */
public final class L2ArmorSet
{
	private final int _id;
	private final int _parts;
	private final TIntIntHashMap _skills;
	private final int _shieldSkillId;
	private final int _enchant6Skill;

	public L2ArmorSet(int id, int parts, TIntIntHashMap skills, int enchant6skill, int shield_skill_id)
	{
		_id = id;
		_parts = parts;
		_skills = skills;

		_shieldSkillId = shield_skill_id;

		_enchant6Skill = enchant6skill;
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
		return _parts - countParts(player);
	}

	private int countParts(L2PcInstance player)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);

		int count = 0;
		if (chestItem != null && chestItem.getArmorItem().isArmorSetPart(_id))
		{
			count++;
		}
		if (legsItem != null && legsItem.getArmorItem().isArmorSetPart(_id))
		{
			count++;
		}
		if (glovesItem != null && glovesItem.getArmorItem().isArmorSetPart(_id))
		{
			count++;
		}
		if (headItem != null && headItem.getArmorItem().isArmorSetPart(_id))
		{
			count++;
		}
		if (feetItem != null && feetItem.getArmorItem().isArmorSetPart(_id))
		{
			count++;
		}

		return count;
	}

	public boolean containsShield(L2PcInstance player)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance shieldItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		return shieldItem != null && shieldItem.getArmorItem() != null && shieldItem.getArmorItem().isArmorSetPart(_id);

	}

	public TIntIntHashMap getSkills()
	{
		return _skills;
	}

	public int getShieldSkillId()
	{
		return _shieldSkillId;
	}

	public int getEnchant6skillId()
	{
		return _enchant6Skill;
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
		if (chestItem != null && chestItem.getArmorItem().isArmorSetPart(_id) && chestItem.getEnchantLevel() < enchant)
		{
			enchant = chestItem.getEnchantLevel();
		}
		if (legsItem != null && legsItem.getArmorItem().isArmorSetPart(_id) && legsItem.getEnchantLevel() < enchant)
		{
			enchant = legsItem.getEnchantLevel();
		}
		if (glovesItem != null && glovesItem.getArmorItem().isArmorSetPart(_id) &&
				glovesItem.getEnchantLevel() < enchant)
		{
			enchant = glovesItem.getEnchantLevel();
		}
		if (headItem != null && headItem.getArmorItem().isArmorSetPart(_id) && headItem.getEnchantLevel() < enchant)
		{
			enchant = headItem.getEnchantLevel();
		}
		if (feetItem != null && feetItem.getArmorItem().isArmorSetPart(_id) && feetItem.getEnchantLevel() < enchant)
		{
			enchant = feetItem.getEnchantLevel();
		}

		return enchant;
	}
}
