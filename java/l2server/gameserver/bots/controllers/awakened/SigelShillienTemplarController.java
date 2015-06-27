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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class SigelShillienTemplarController extends SigelController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		401, // Judgment
		984, // Shield Strike
	};

	@SuppressWarnings("unused")
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		// General Buffs
		112, // Deflect Arrow
		982, // Combat Aura
		351, // Magical Mirror
		527, // Iron Shield
		
		// Cubics
		// 22, // Summon Vampiric Cubic | Stuck casting it somehow, gotta check
		33, // Summon Phantom Cubic
		278, // Summon Viper Cubic
		780, // Summon Smart Cubic
	};

	private static final int SHIELD_BASH_ID = 352;
	private static final int FREEZING_STRIKE_ID = 105;
	private static final int ARREST_ID = 402;
	@SuppressWarnings("unused")
	private static final int POWER_BREAK_ID = 115;
	@SuppressWarnings("unused")
	private static final int HEX_ID = 122;
	@SuppressWarnings("unused")
	private static final int LIGHTNING_STRIKE_ID = 279;
	@SuppressWarnings("unused")
	private static final int TOUCH_OF_DEATH_ID = 342;
	
	public SigelShillienTemplarController(final L2PcInstance player)
	{
		super(player);
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
			if (targetedPlayer.isFighter() && isSkillAvailable(SHIELD_BASH_ID) && targetedPlayer.getFirstEffect(SHIELD_BASH_ID) == null)
				return SHIELD_BASH_ID;
			
			if (targetedPlayer.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				final int[] movementTrace = targetedPlayer.getMovementTrace();
				final int[] previousMovementTrace = targetedPlayer.getPreviousMovementTrace();
				
				final int distanceAfterMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), movementTrace[0], movementTrace[1], movementTrace[2], false);
				final int distanceBeforeMove = (int) Util.calculateDistance(_player.getX(), _player.getY(), _player.getZ(), previousMovementTrace[0], previousMovementTrace[1], previousMovementTrace[2], false);
				
				// Player is running away...
				if (distanceAfterMove > distanceBeforeMove)
				{
					final boolean isTargetSlownDown = target.getFirstEffect(FREEZING_STRIKE_ID) != null || target.getFirstEffect(ARREST_ID) != null;
					
					// Let's slow him down with either Shadow Bind or Soul Web.
					if (!isTargetSlownDown)
					{
						int randomSlowSkillId = Rnd.nextBoolean() ? FREEZING_STRIKE_ID : ARREST_ID;
						
						if (!isSkillAvailable(randomSlowSkillId))
							randomSlowSkillId = randomSlowSkillId == FREEZING_STRIKE_ID ? ARREST_ID : FREEZING_STRIKE_ID;
						
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
}
