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
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class HellKnightController extends TankController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		401, // Judgment
		984, // Shield Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		82, // Majesty
		112, // Deflect Arrow
		86, // Reflect Damage
		761, // Shield of Revenge
		982, // Combat Aura
		350, // Physical Mirror
		527, // Iron Shield
		761, // Seed of Revenge
		913, // Deflect Magic
	};
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		318, // Aegis Stance
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		283, // Summon Dark Panther
	};
	
	private static final int SHIELD_SLAM_ID = 353;
	private static final int TOUCH_OF_DEATH_ID = 342;
	private static final int HAMSTRING_ID = 127;
	private static final int SHACKLE_ID = 403;
	
	public HellKnightController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected boolean isSummonAllowedToAssist()
	{
		return true;
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final int selectedSkillId = super.pickSpecialSkill(target);
		
		if (selectedSkillId != -1)
			return selectedSkillId;
		
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;

		if (targetedPlayer != null)
		{
			if (targetedPlayer.isFighter() && isSkillAvailable(SHIELD_SLAM_ID) && targetedPlayer.getFirstEffect(SHIELD_SLAM_ID) == null)
				return SHIELD_SLAM_ID;
			
			if (Rnd.get(0, 3) == 0 && isSkillAvailable(TOUCH_OF_DEATH_ID))
				return TOUCH_OF_DEATH_ID;
			
			if (targetedPlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				final int[] movementTrace = targetedPlayer.getMovementTrace();
				final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
				
				final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
				final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);
				
				// Player is running away...
				if (distanceAfterMove > distanceBeforeMove)
				{
					final boolean isTargetSlownDown = target.getFirstEffect(HAMSTRING_ID) != null || target.getFirstEffect(SHACKLE_ID) != null;
					
					// Let's slow him down with either Shadow Bind or Soul Web.
					if (!isTargetSlownDown)
					{
						int randomSlowSkillId = Rnd.nextBoolean() ? HAMSTRING_ID : SHACKLE_ID;
						
						if (!isSkillAvailable(randomSlowSkillId))
							randomSlowSkillId = randomSlowSkillId == HAMSTRING_ID ? SHACKLE_ID : HAMSTRING_ID;
						
						if (!isSkillAvailable(randomSlowSkillId))
							randomSlowSkillId = -1;
						
						if (randomSlowSkillId != -1)
							return randomSlowSkillId;
					}
				}
			}
		}
		
		return -1;
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
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
	
	@Override
	public final int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
}