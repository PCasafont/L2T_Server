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
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class SummonerController extends MageController
{
	private static final int SERVITOR_RECHARGE = 1126;
	private static final int SERVITOR_HEAL = 1127;
	private static final int SERVITOR_CURE = 1300;
	private static final int SERVITOR_BLESSING = 1301;
	
	private static final int SERVITOR_MAGIC_SHIELD = 1139;
	private static final int SERVITOR_PHYSICAL_SHIELD = 1140;
	private static final int SERVITOR_HASTE = 1141;
	
	private static final int SERVITOR_EMPOWERMENT = 1299;
	
	private static final int SPIRIT_SHARING = 1547;
	private static final int SERVITOR_SHARE = 1557;
	private static final int BATTLE_HEAL_ID = 1015;
	
	private static final int BETRAY_ID = 1380;
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		338, // Arcane Agility
		1262, // Transfer Pain
	};
	
	public SummonerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected boolean isSummonAllowedToAssist()
	{
		return true;
	}
	
	@Override
	protected boolean isAllowedToSummonInCombat()
	{
		return true;
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		final L2Summon summon = _player.getPet();
		
		if (summon != null && !summon.isDead())
		{
			final int summonHealthPercent = getHealthPercent(summon);
			
			if (summonHealthPercent < 90 && Rnd.nextBoolean() || summonHealthPercent < 50)
			{
				if (summonHealthPercent < 25 && isSkillAvailable(SERVITOR_EMPOWERMENT) && Util.checkIfInRange(100, summon, _focusedTarget, false))
					return SERVITOR_EMPOWERMENT;
				
				if (isSkillAvailable(SERVITOR_HEAL))
					return SERVITOR_HEAL;
				else if (isSkillAvailable(BATTLE_HEAL_ID))
				{
					_player.setTarget(summon);
					
					return BATTLE_HEAL_ID;
				}
			}
			
			if (summon.getAllDebuffs().length != 0 && Rnd.nextBoolean())
			{
				int skillIdToUse = -1;
				
				if (isSkillAvailable(SERVITOR_CURE))
					skillIdToUse = SERVITOR_CURE;
				
				if ((Rnd.nextBoolean() || skillIdToUse == -1) && isSkillAvailable(SERVITOR_BLESSING))
					skillIdToUse = SERVITOR_BLESSING;
				
				if (skillIdToUse != -1)
					return skillIdToUse;
			}
		}
		
		final int healthPercent = getHealthPercent(_player);
		
		if (healthPercent < 90 && isSkillAvailable(BATTLE_HEAL_ID))
			return BATTLE_HEAL_ID;
		
		if (targetedPlayer != null)
		{
			final L2Summon enemySummon = targetedPlayer.getPet();
			
			if (enemySummon != null && Util.checkIfInRange(400, _player, enemySummon, false) && isSkillAvailable(BETRAY_ID))
				return BETRAY_ID;
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}