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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.bots.DamageType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class WindRiderController extends DaggerController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		16, // Mortal Blow
		321, // Blinding Blow
		263, // Deadly Blow
		344, // Lethal Blow
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		356, // Focus Chance
		410, // Mortal Strike
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		531, // Critical Wound
		358, // Bluff
	};
	
	private static final int SWITCH_ID = 12;
	
	private static final int ENTANGLE_ID = 102;
	
	private static final int DODGE_ID = 446;
	
	private static final int ULTIMATE_EVASION_ID = 111;
	
	private static final int THROWING_DAGGER_ID = 991;
	
	private static final int WIND_RIDING_ID = 769;
	
	public WindRiderController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		final int totalPhysicalSkillDamages = getTotalDamagesByType(DamageType.PHYSICAL_SKILL);
		
		// Use Dodge when receiving too many damages from physical skills.
		if (isSkillAvailable(DODGE_ID) && totalPhysicalSkillDamages > 1000)
			return DODGE_ID;
		
		int totalPhysicalAttackDamages = getTotalDamagesByType(DamageType.PHYSICAL_ATTACK);
		
		// Use Ultimate Evasion when receiving too many damages from regular attacks.
		if (isSkillAvailable(ULTIMATE_EVASION_ID) && totalPhysicalAttackDamages > 1000)
			return ULTIMATE_EVASION_ID;
		
		// Use Wind Riding when we received too many physical damages, or when the target is far.
		if (isSkillAvailable(WIND_RIDING_ID) && (totalPhysicalSkillDamages > 1500 || totalPhysicalAttackDamages > 1500))
			return WIND_RIDING_ID;
		
		// Following skills are used exclusively in PvP situations...
		if (targetedPlayer != null)
		{
			if (targetedPlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				final int[] movementTrace = targetedPlayer.getMovementTrace();
				final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
				
				final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
				final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);
				
				// Player is running away...
				if (distanceAfterMove > distanceBeforeMove)
				{
					// If Throwing Dagger is available, use it.
					if (isSkillAvailable(THROWING_DAGGER_ID))
						return THROWING_DAGGER_ID;
					// Otherwise, from time to time, if Entangle is available and Throwing Dagger/Entangle aren't on target, use that.
					else if (Rnd.nextBoolean() && isSkillAvailable(ENTANGLE_ID) && target.getFirstEffect(THROWING_DAGGER_ID) == null && target.getFirstEffect(ENTANGLE_ID) == null)
						return ENTANGLE_ID;
				}
			}
			
			final int healthPercent = getPlayerHealthPercent();
			
			int switchRate = 25;
			
			if (healthPercent < 25)
				switchRate = 40;
			
			// Switch our target from time to time if worthy...
			if (switchRate > Rnd.get(100) && targetedPlayer.getTarget() == _player && isSkillAvailable(SWITCH_ID))
				return SWITCH_ID;
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
