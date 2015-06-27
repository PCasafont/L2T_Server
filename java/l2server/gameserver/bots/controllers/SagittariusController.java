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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class SagittariusController extends ArcherController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		19, // Double Shot
		771, // Flame Hawk
		990, // Death Shot
		343, // Lethal Shot
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		99, // Rapid Shot
		303, // Soul of Sagittarius
		415, // Spirit of Sagittarius
		416, // Blessing of Sagittarius
	};
	
	private static final int DASH_ID = 4;
	private static final int HAWK_EYE_ID = 131;
	private static final int HAMSTRING_SHOT_ID = 354;
	private static final int BURST_SHOT_ID = 24;
	private static final int MULTIPLE_SHOT_ID = 987;
	private static final int SNIPE_ID = 313;
	private static final int STUN_SHOT_ID = 101;
	private static final int ULTIMATE_EVASION_ID = 111;
	
	public SagittariusController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		int distance = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), target.getX(), target.getY(), target.getZ(), false);
		
		if (Rnd.get(0, 1) == 0 && !_player.isSkillDisabled(DASH_ID) && distance < 150)
			return DASH_ID;
		
		int totalPhysicalAttackDamages = getTotalDamagesByType(DamageType.PHYSICAL_ATTACK);
		
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
				if (isSkillAvailable(HAMSTRING_SHOT_ID) && target.getFirstEffect(HAMSTRING_SHOT_ID) == null)
					return HAMSTRING_SHOT_ID;
			}
		}
		
		final L2Abnormal dashEffect = _player.getFirstEffect(DASH_ID);
		if (!target.isStunned() && Rnd.get(0, 1) == 0 && isSkillAvailable(STUN_SHOT_ID) && distance < 200 && (dashEffect == null || dashEffect.getTime() < 5))
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
