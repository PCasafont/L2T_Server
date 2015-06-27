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
import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class OthellController extends BotController
{
	//Self Buffs
	protected static final int OTHELL_AURA_ID = 1931;
	
	//Toggle
	private static final int DUAL_MAXIMUM_HP = 1986;
	
	//Area Attack Skills
	private static final int RAZOR_RAIN_ID = 10513;
	protected static final int DAGGER_EXPLOSION_ID = 10512;
	
	//Attack Skills
	private static final int BLOOD_STAB_ID = 10508;
	protected static final int REVERSE_ID = 10511;
	protected static final int CHAIN_BLOW_ID = 10510;
	protected static final int HEART_BREAKER_ID = 10509;
	
	//Others
	private static final int CRITICAL_WOUND_ID = 531;
	private static final int DARK_PARALYSIS_ID = 531;
	private static final int SHADOW_CHASE_ID = 10516;
	private static final int POISON_ZONE_ID = 10522;
	private static final int MISCHIEF_ID = 10526;
	private static final int MASS_TRICK_ID = 10527;
	@SuppressWarnings("unused") // TODO
	private static final int SHADOW_FLASH_ID = 10529;
	private static final int THROW_DAGGER_ID = 10539;
	@SuppressWarnings("unused") // TODO
	private static final int THROW_SAND_ID = 10540;
	private static final int THROW_POISON_NEEDLE_ID = 10541;
	private static final int UPPERCUT_ID = 10546;
	private static final int KICK_ID = 10549;
	private static final int POWER_BLUFF_ID = 10554;
	
	//Special Skills
	private static final int SHADOW_HIDE_ID = 10517;
	private static final int FINAL_ULTIMATE_EVASION_ID = 10520;
	private static final int EVASIVE_COUNTERSTRIKE_ID = 10524;
	private static final int ANGEL_OF_DEATH_ID = 10531;
	@SuppressWarnings("unused") // TODO
	private static final int SHADOW_FAKE_DEATH_ID = 10528;
	
	private final int[] ATTACK_SKILL_IDS =
	{
		HEART_BREAKER_ID,
		CHAIN_BLOW_ID,
		REVERSE_ID
	};

	private final int[] DEBUFF_SKILL_IDS =
	{
		DARK_PARALYSIS_ID,
		SHADOW_CHASE_ID,
		POISON_ZONE_ID,
		MISCHIEF_ID,
		THROW_POISON_NEEDLE_ID,
		UPPERCUT_ID,
		KICK_ID,
		POWER_BLUFF_ID
	};
		
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		OTHELL_AURA_ID,
		DUAL_MAXIMUM_HP
	};
		
	private static final int[][] AREA_OF_EFFECT_SKILLS =
	{
		// [0] = SkillId
		// [1] = CastChances
		// [2] = The amount of nearby targets for granted cast, if the target is also in range.
		// [3] = The amount of nearby targets for granted cast, even if the target isn't in range.
		{ RAZOR_RAIN_ID, 25, 2, 4 },
		{ MASS_TRICK_ID, 25, 2, 4 },
	};
	
	protected enum UsageStyle
	{
		ULTIMATES_USAGE_STYLE_ONE,
		ULTIMATES_USAGE_STYLE_TWO,
		ULTIMATES_USAGE_STYLE_THREE,
	}

	protected UsageStyle _ultimatesUsageStyle;
	
	public OthellController(final L2PcInstance player)
	{
		super(player);
		
		_ultimatesUsageStyle = UsageStyle.values()
				[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal())];
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final int distanceToTarget = getDistanceTo(target);
		
		switch (_ultimatesUsageStyle)
		{
			// Style 1:
			// Use Final Ultimate Evasion whenever possible.
			// Use Evasive Counterstrike whenever possible.
			// Use Angel of Death whenever possible.
			case ULTIMATES_USAGE_STYLE_ONE:
			{
				if (Rnd.nextBoolean() && isSkillAvailable(FINAL_ULTIMATE_EVASION_ID))
					return FINAL_ULTIMATE_EVASION_ID;
				
				if (Rnd.nextBoolean() && isSkillAvailable(EVASIVE_COUNTERSTRIKE_ID))
					return EVASIVE_COUNTERSTRIKE_ID;
				
				if (Rnd.nextBoolean() && isSkillAvailable(ANGEL_OF_DEATH_ID))
					return ANGEL_OF_DEATH_ID;
				
				if (isSkillAvailable(SHADOW_HIDE_ID))
				{
					if (getPlayerHealthPercent() < 15 || (_player.isRooted() && Rnd.get(0, 5) == 0))
					{
						_lockActionsTill = System.currentTimeMillis() + Rnd.get(5000, 15000);
						
						return SHADOW_HIDE_ID;
					}
				}
				
				/*
				if (isSkillAvailable(SHADOW_FAKE_DEATH_ID))
				{
					if (getPlayerHealthPercent() < 15)
					{
						_lockActionsTill = System.currentTimeMillis() + Rnd.get(5000, 15000);
						
						return SHADOW_FAKE_DEATH_ID;
					}
				}*/
				break;
			}
		}
		
		// If we're behind the target and Bloodstab is available... Bloodstab!
		if (distanceToTarget < 100)
		{
			if (_player.isBehind(target) && isSkillAvailable(BLOOD_STAB_ID))
				return BLOOD_STAB_ID;
		}
		else if (distanceToTarget > 250)
		{
			if (isSkillAvailable(SHADOW_CHASE_ID))
				return SHADOW_CHASE_ID;
		}
		
		if (!_player.isBehind(target) && isSkillAvailable(POWER_BLUFF_ID))
			return POWER_BLUFF_ID;
		
		if (Rnd.nextBoolean() && isSkillAvailable(CRITICAL_WOUND_ID) && target.getFirstEffect(CRITICAL_WOUND_ID) == null)
			return CRITICAL_WOUND_ID;
		
		if (Rnd.nextBoolean() && isSkillAvailable(DARK_PARALYSIS_ID) && target.getFirstEffect(DARK_PARALYSIS_ID) == null)
			return DARK_PARALYSIS_ID;
		
		if (!isTargetGettingCloser() && isSkillAvailable(THROW_DAGGER_ID))
			return THROW_DAGGER_ID;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 15;
	}
	
	@Override
	protected boolean maybeMoveToBestPosition(final L2Character targetedCharacter)
	{
		return (!_player.isBehind(targetedCharacter) && (targetedCharacter.isStunned() || (targetedCharacter.getTarget() == null && Rnd.get(0, 3) == 0))) ? true : Rnd.get(0, 10) == 0;
	}
	
	@Override
	protected void moveToBestPosition(final L2Character targetedCharacter)
	{
		float headingAngle = (float) (targetedCharacter.getHeading() * Math.PI) / Short.MAX_VALUE;
		
		float x = targetedCharacter.getX() - 50 * (float) Math.cos(headingAngle);
		float y = targetedCharacter.getY() - 50 * (float) Math.sin(headingAngle);
		float z = targetedCharacter.getZ() + 1;
		
		_player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition((int) x, (int) y, (int) z, targetedCharacter.getHeading()));
		
		_refreshRate = 500;
	}
	
	@Override
	public final int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
	
	@Override
	public final int[][] getAreaOfEffectSkills()
	{
		return AREA_OF_EFFECT_SKILLS;
	}
	
	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}