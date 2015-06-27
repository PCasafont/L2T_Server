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
package l2server.gameserver.bots.controllers;

import l2server.gameserver.bots.DamageType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class TankController extends BotController
{
	private final static int[] DEBUFF_SKILL_IDS =
	{
		92, // Shield Stun
		985, // Challenge for Fate
	};

	private static final int ULTIMATE_DEFENSE_ID = 110;
	private static final int SHIELD_DEFLECT_MAGIC_ID = 916;
	private static final int ANTI_MAGIC_ARMOR_ID = 760;
	private static final int VENGEANCE_ID = 368;
	private static final int SHIELD_OF_FAITH_ID = 528;
	
	private static final int AGGRESSION_ID = 28;
	
	private byte _ultimateDefensesUsageStyle;
	
	// Style 1: Use Shield of Faith when hitting < 90% HP.
	// If attacked by a mage, use Shield Deflect Magic when hitting < 75% HP.
	// When Shield Deflect Magic is no longer active, use Anti Magic Armor.
	// When hitting 25% HP, use Ultimate Defense & Vengeance.
	private static final byte ULTIMATE_DEFENSES_USAGE_STYLE_ONE = 0;
	
	// Style 2: Use Shield of Faith when hitting 75% HP.
	// If attacked by a mage, use Shield Deflect Magic when hitting 50% HP.
	// When Shield Deflect Magic is no longer active, use Anti Magic Armor.
	// When hitting 15% HP, use Ultimate Defense & Vengeance.
	private static final byte ULTIMATE_DEFENSES_USAGE_STYLE_TWO = 1;
	
	@SuppressWarnings("unused")
	private static final byte ULTIMATE_DEFENSES_USAGE_STYLE_THREE = 2; // TODO
	
	public TankController(final L2PcInstance player)
	{
		super(player);
		
		_ultimateDefensesUsageStyle = (byte) Rnd.get(ULTIMATE_DEFENSES_USAGE_STYLE_ONE, ULTIMATE_DEFENSES_USAGE_STYLE_TWO);
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.BLUNT || weaponItem.getItemType() == L2WeaponType.SWORD;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.HEAVY || (armorItem.getCrystalType() <= L2Item.CRYSTAL_D && armorItem.getItemType() == L2ArmorType.LIGHT);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final int distanceToTarget = target == null ? 0 : (int) Util.calculateDistance(_player.getX(), _player.getY(), target.getX(), target.getY());
		
		if (distanceToTarget > 100 && Rnd.get(0, 3) == 0 && isSkillAvailable(AGGRESSION_ID))
			return AGGRESSION_ID;

		switch (_ultimateDefensesUsageStyle)
		{
		// Style 1: Use Shield of Faith when hitting < 90% HP.
		// If attacked by a mage, use Shield Deflect Magic when hitting < 75% HP.
		// When Shield Deflect Magic is no longer active, use Anti Magic Armor.
		// When hitting 25% HP, use Ultimate Defense & Vengeance.
			case ULTIMATE_DEFENSES_USAGE_STYLE_ONE:
			{
				final int healthPercent = getHealthPercent(_player);
				
				if (healthPercent < 90 && isSkillAvailable(SHIELD_OF_FAITH_ID))
					return SHIELD_OF_FAITH_ID;
				
				// Use Shield Deflect Magic when hitting < 75% HP.
				// When Shield Deflect Magic is no longer active, use Anti Magic Armor.
				if (healthPercent < 75)
				{
					int totalMagicalAttackDamages = getTotalDamagesByType(DamageType.MAGICAL_ATTACK);
					
					if (totalMagicalAttackDamages > 1000)
					{
						if (isSkillAvailable(SHIELD_DEFLECT_MAGIC_ID))
							return SHIELD_DEFLECT_MAGIC_ID;
						else if (isSkillAvailable(ANTI_MAGIC_ARMOR_ID) && _player.getFirstEffect(SHIELD_DEFLECT_MAGIC_ID) == null)
							return ANTI_MAGIC_ARMOR_ID;
					}
				}
				
				if (healthPercent < 25)
				{
					if (isSkillAvailable(ULTIMATE_DEFENSE_ID))
						return ULTIMATE_DEFENSE_ID;
					else if (isSkillAvailable(VENGEANCE_ID))
						return VENGEANCE_ID;
				}
				
				break;
			}
			// Style 2: Use Shield of Faith when hitting 75% HP.
			// If attacked by a mage, use Shield Deflect Magic when hitting 50% HP.
			// When Shield Deflect Magic is no longer active, use Anti Magic Armor.
			// When hitting 15% HP, use Ultimate Defense & Vengeance.
			case ULTIMATE_DEFENSES_USAGE_STYLE_TWO:
			{
				final int healthPercent = getHealthPercent(_player);
				
				if (healthPercent < 75 && isSkillAvailable(SHIELD_OF_FAITH_ID))
					return SHIELD_OF_FAITH_ID;
				
				// Use Shield Deflect Magic when hitting < 75% HP.
				// When Shield Deflect Magic is no longer active, use Anti Magic Armor.
				if (healthPercent < 50)
				{
					int totalMagicalAttackDamages = getTotalDamagesByType(DamageType.MAGICAL_ATTACK);
					
					if (totalMagicalAttackDamages > 1000)
					{
						if (isSkillAvailable(SHIELD_DEFLECT_MAGIC_ID))
							return SHIELD_DEFLECT_MAGIC_ID;
						else if (isSkillAvailable(ANTI_MAGIC_ARMOR_ID) && _player.getFirstEffect(SHIELD_DEFLECT_MAGIC_ID) == null)
							return ANTI_MAGIC_ARMOR_ID;
					}
				}
				
				if (healthPercent < 15)
				{
					if (isSkillAvailable(ULTIMATE_DEFENSE_ID))
						return ULTIMATE_DEFENSE_ID;
					else if (isSkillAvailable(VENGEANCE_ID))
						return VENGEANCE_ID;
				}
				
				break;
			}
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
}
