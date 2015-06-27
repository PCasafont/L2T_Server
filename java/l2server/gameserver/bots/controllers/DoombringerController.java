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
import l2server.gameserver.templates.item.L2Weapon;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class DoombringerController extends BotController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		476, // Dark Strike
		477, // Dark Smash
		526, // Enuma Elish
		497, // Crush of Pain
		//498, // Contagion
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		834, // Blood Pact
		475, // Strike Back
		479, // Hard March
		500, // True Berserker
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		1435, // Death Mark
		485, // Disarm
		501, // Violent Temper
	};
	
	private static final int BODY_RECONSTRUCTION_ID = 833;
	private static final int FINAL_FORM_ID = 538;
	private static final int SOUL_GATHERING_ID = 625;
	private static final int LIFE_TO_SOUL_ID = 953;
	private static final int SOUL_CLEANSE_ID = 1510;
	private static final int SPREAD_WING_ID = 492;
	
	public DoombringerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		if (_player.isTransformed())
			return 100;
		
		return 50;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Weapon weaponItem)
	{
		return weaponItem.getItemType() == L2WeaponType.ANCIENTSWORD;
	}
	
	@Override
	protected final boolean isOkToEquip(L2Armor armorItem)
	{
		return armorItem.getItemType() == L2ArmorType.LIGHT;
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			// TODO
			// Maybe find an use to Scorn?
			
			final int healthPercent = getPlayerHealthPercent();
			
			if (healthPercent < 15)
			{
				if (isSkillAvailable(BODY_RECONSTRUCTION_ID))
					return BODY_RECONSTRUCTION_ID;
			}
			else if (healthPercent < 50)
			{
				if (!isSkillAvailable(BODY_RECONSTRUCTION_ID))
				{
					if (!_player.isTransformed() && isSkillAvailable(FINAL_FORM_ID))
					{
						// We have to make sure the player has enough souls to transform...
						if (_player.getSouls() < 20)
						{
							if (isSkillAvailable(SOUL_GATHERING_ID))
								return SOUL_GATHERING_ID;
							else if (isSkillAvailable(LIFE_TO_SOUL_ID))
								return LIFE_TO_SOUL_ID;
						}
						else
							return FINAL_FORM_ID;
					}
				}
			}
			
			if (isSkillAvailable(SOUL_CLEANSE_ID) &&  _player.getAllDebuffs().length != 0 && Rnd.nextBoolean())
				return SOUL_CLEANSE_ID;
			else if (isSkillAvailable(SPREAD_WING_ID) && Rnd.get(0, 5) == 0 && Util.checkIfInRange(200, _player, target, false))
				return SPREAD_WING_ID;
			else if (isSkillAvailable(LIFE_TO_SOUL_ID) && Rnd.get(0, 3) == 0)
				return LIFE_TO_SOUL_ID;
		}
		
		return super.pickSpecialSkill(target);
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
	
	@Override
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
}
