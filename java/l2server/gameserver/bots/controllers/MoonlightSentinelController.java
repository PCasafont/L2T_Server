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
@SuppressWarnings("unused")
public class MoonlightSentinelController extends ArcherController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		19, // Double Shot
		369, // Evade Shot
		990, // Death Shot
		343, // Lethal Shot
		990, // Death Shot
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		99, // Rapid Shot
		413, // Rapid Fire
		303, // Soul of Sagittarius
		415, // Spirit of Sagittarius
		416, // Blessing of Sagittarius
	};

	private static final int EVADE_SHOT_ID = 369;
	private static final int ENTANGLE_ID = 102;
	private static final int HAMSTRING_SHOT_ID = 354;
	private static final int ULTIMATE_EVASION_ID = 111;
	private static final int STUN_SHOT_ID = 101;
	private static final int FATAL_COUNTER_ID = 314;
	
	private static final int ACCURACY_ID = 256;
	private static final int ELEMENTAL_HEAL_ID = 58;
	
	public MoonlightSentinelController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (getPlayerHealthPercent() < 15 && isSkillAvailable(FATAL_COUNTER_ID))
			return FATAL_COUNTER_ID;
		
		int totalPhysicalAttackDamages = getTotalDamagesByType(DamageType.PHYSICAL_ATTACK);
		
		// We use Evade Shot if available and if we received physical attack damages.
		if (isSkillAvailable(EVADE_SHOT_ID) && totalPhysicalAttackDamages != 0)
			return EVADE_SHOT_ID;
		
		// Use Ultimate Evasion when receiving too many damages from regular attacks.
		if (isSkillAvailable(ULTIMATE_EVASION_ID) && totalPhysicalAttackDamages > 1000)
			return ULTIMATE_EVASION_ID;
		
		if (targetedPlayer != null && targetedPlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
		{
			final int[] movementTrace = targetedPlayer.getMovementTrace();
			final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
			
			final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
			final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);

			// Player is running away...
			if (distanceAfterMove > distanceBeforeMove)
			{
				final boolean isTargetSlownDown = target.getFirstEffect(ENTANGLE_ID) != null || target.getFirstEffect(HAMSTRING_SHOT_ID) != null;
				
				// Let's slow him down with either Entangle or Hamstring Shot
				if (!isTargetSlownDown)
				{
					int randomSlowSkillId = Rnd.nextBoolean() ? ENTANGLE_ID : HAMSTRING_SHOT_ID;
					
					if (!isSkillAvailable(randomSlowSkillId))
						randomSlowSkillId = randomSlowSkillId == ENTANGLE_ID ? HAMSTRING_SHOT_ID : ENTANGLE_ID;
					
					if (!isSkillAvailable(randomSlowSkillId))
						randomSlowSkillId = -1;
					
					if (randomSlowSkillId != -1)
						return randomSlowSkillId;
				}
			}
		}
		
		int distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), target.getX(), target.getY(), target.getZ(), false);
			
		if (Rnd.nextBoolean() && !target.isStunned() && isSkillAvailable(STUN_SHOT_ID) && distance < 200)
			return STUN_SHOT_ID;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected int getAttackRange()
	{
		//Log.info(_player.getName() + " Base Attack Range = " + _player.getTemplate().baseAtkRange);
		return _player.getTemplate().baseAtkRange;
	}
	
	@Override
	protected int getMinimumRangeToUseCatchupSkill()
	{
		return getAttackRange() * 5;
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