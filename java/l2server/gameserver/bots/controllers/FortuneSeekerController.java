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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2ArmorType;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;

/**
 *
 * @author LittleHakor
 */
public class FortuneSeekerController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		260, // Hammer Crush
		362, // Armor Crush
		997, // Crushing Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		94, // Rage
	};
	
	private static final int FRENZY_ID = 176;
	
	private static final int BATTLE_ROAR_ID = 121;
	
	public FortuneSeekerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 30;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.BLUNT;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.HEAVY || (armorItem.getCrystalType() <= L2Item.CRYSTAL_D && armorItem.getItemType() == L2ArmorType.LIGHT);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		// TODO Braveheart
		int hpPercent = getPlayerHealthPercent();
		
		if (hpPercent < 30)
		{
			if (_player.getFirstEffect(FRENZY_ID) == null && isSkillAvailable(FRENZY_ID))
				return FRENZY_ID;
		}
		
		if (isAllowedToUseEmergencySkills())
		{
			if (isSkillAvailable(BATTLE_ROAR_ID))
				return BATTLE_ROAR_ID;
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final boolean isAllowedToUseEmergencySkills()
	{
		final boolean isFrenzyUnavailable = !isSkillAvailable(FRENZY_ID);
		
		final boolean isFrenzyActive = _player.getFirstEffect(FRENZY_ID) != null;
		
		if (isFrenzyUnavailable)
		{
			if (isFrenzyActive && isSkillAvailable(BATTLE_ROAR_ID))
				return true;
			
			return false;
		}

		return false;
	}
	
	@Override
	public final int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}
