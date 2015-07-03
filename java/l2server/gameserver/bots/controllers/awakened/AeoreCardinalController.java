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
package l2server.gameserver.bots.controllers.awakened;

import java.util.ArrayList;

import l2server.gameserver.bots.controllers.MageController;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 * 
 * @author LittleHakor
 */
public class AeoreCardinalController extends MageController
{
	private static final int MASS_BLOCK_SHIELD_ID = 1360;
	private static final int MASS_BLOCK_WIND_WALK_ID = 1361;
	private static final int MANA_STORM_ID = 1399;
	
	private final static int[] AOE_DEBUFF_SKILL_IDS =
	{
		MASS_BLOCK_SHIELD_ID,
		MASS_BLOCK_WIND_WALK_ID,
		MANA_STORM_ID
	};
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		336, // Arcane Wisdom
	};
	
	// LLALALALA
	
	private static final int BODY_OF_AVATAR_ID = 1311;
	private static final int DIVINE_POWER_ID = 1459;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		BODY_OF_AVATAR_ID,
		DIVINE_POWER_ID
	};
	
	private static final int BATTLE_HEAL_ID = 1015;
	
	private static final int GREATER_HEAL_ID = 1217;
	private static final int GREATER_BATTLE_HEAL_ID = 1218;
	private static final int GREATER_GROUP_HEAL_ID = 1219;
	private static final int RESTORE_LIFE_ID = 1258;
	private static final int BENEDICTION_ID = 1271;
	
	private static final int ERASE_ID = 1395;
	private static final int MANA_BURN_ID = 1398;
	private static final int MAJOR_HEAL_ID = 1401;
	private static final int MAJOR_GROUP_HEAL_ID = 1402;
	private static final int BALANCE_LIFE_ID = 1335;
	private static final int CLEANSE_ID = 1409;
	private static final int CHAIN_HEAL_ID = 1553;
	private static final int MAGICAL_BACKFIRE_ID = 1396;
	private static final int TRANCE_ID = 1394;
	
	public AeoreCardinalController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		L2Character targetToHeal = null;
		
		int amountOfTargetsNeedingHelp = 0;
		
		// If we're under 75% HP, we prioritize healing ourself over anything else...
		if (getHealthPercent(_player) < 75)
		{
			targetToHeal = _player;
			
			amountOfTargetsNeedingHelp++;
		}
		
		// If not, we look for nearby friendly players needing an hand.
		if (targetToHeal == null)
		{
			ArrayList<L2PcInstance> allTargetsNeedingHelp = getTargetsNeedingHelp(900, 75, 0);
			
			if (allTargetsNeedingHelp.size() != 0)
			{
				targetToHeal = allTargetsNeedingHelp.get(0);
				
				amountOfTargetsNeedingHelp += allTargetsNeedingHelp.size();
			}
		}
		
		_assistedTarget = targetToHeal;
		
		// We've got a target to heal.
		if (targetToHeal != null)
		{
			_player.setTarget(targetToHeal);
			
			// Or maybe more than that. If so, use a massive heal skill.
			if (amountOfTargetsNeedingHelp > Rnd.get(1, 3))
			{
				if (isSkillAvailable(GREATER_GROUP_HEAL_ID))
					return GREATER_GROUP_HEAL_ID;
				else if (isSkillAvailable(BALANCE_LIFE_ID))
					return BALANCE_LIFE_ID;
				else if (isSkillAvailable(CHAIN_HEAL_ID))
					return CHAIN_HEAL_ID;
				else if (isSkillAvailable(MAJOR_GROUP_HEAL_ID))
					return MAJOR_GROUP_HEAL_ID;
			}
			
			final int targetHealthPercent = getHealthPercent(targetToHeal);
			
			// If the actual target isn't in an alarming state, and has debuffs... let's cleanse.
			if (isSkillAvailable(CLEANSE_ID) && targetHealthPercent > 50 && targetToHeal.getAllDebuffs().length != 0 && Rnd.nextBoolean())
				return CLEANSE_ID;
			else if (isSkillAvailable(BENEDICTION_ID) && getPlayerManaPercent() < 25 && targetHealthPercent < 25)
				return BENEDICTION_ID;
			else if (Rnd.get(0, 3) == 00 && isSkillAvailable(RESTORE_LIFE_ID))
				return RESTORE_LIFE_ID;
			else if (isSkillAvailable(GREATER_BATTLE_HEAL_ID))
				return GREATER_BATTLE_HEAL_ID;
			else if (isSkillAvailable(GREATER_HEAL_ID))
				return GREATER_HEAL_ID;
			else if (isSkillAvailable(MAJOR_HEAL_ID))
				return MAJOR_HEAL_ID;
			else if (isSkillAvailable(BATTLE_HEAL_ID))
				return BATTLE_HEAL_ID;
		}
		else
		{
			// No target to heal.
			// Let's fuck around with an enemy.
			if (Rnd.get(0, 3) == 0 && isSkillAvailable(TRANCE_ID) && target.getFirstEffect(TRANCE_ID) == null)
				return TRANCE_ID;
			
			if (Rnd.get(0, 3) == 0 && isSkillAvailable(MANA_BURN_ID) && target.getFirstEffect(MANA_BURN_ID) == null)
				return MANA_BURN_ID;
			
			if (Rnd.get(0, 3) == 0 && isSkillAvailable(MAGICAL_BACKFIRE_ID) && target.getFirstEffect(MAGICAL_BACKFIRE_ID) == null)
				return MAGICAL_BACKFIRE_ID;
			
			if (target instanceof L2Summon && isSkillAvailable(ERASE_ID))
				return ERASE_ID;
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
	
	@Override
	public final int[] getAoeDebuffSkillIds()
	{
		return AOE_DEBUFF_SKILL_IDS;
	}
	

	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}