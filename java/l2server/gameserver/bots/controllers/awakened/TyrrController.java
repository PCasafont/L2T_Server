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

import l2server.gameserver.bots.controllers.BotController;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class TyrrController extends BotController
{
	//Attack Skills
	protected static final int INFINITY_STRIKE_ID = 10275;
	protected static final int POWER_BOMBER_ID = 10262;
	protected static final int MEGA_STRIKE_ID = 10260;
	protected static final int ARMOR_DESTRUCTION_ID = 10258;
	
	//AEO Skills
	protected static final int ERUPTION_ID = 10265;
	protected static final int GIANT_PUNCH_ID = 10266;
	protected static final int SONIC_STAR_ID = 10281;
	protected static final int POWER_PROVOKE_ID = 10271;
	protected static final int LIGHTNING_DISARM_ID = 10273;
	
	//Buff Skills
	private static final int BERSERKER_ID = 10274;
	@SuppressWarnings("unused") // TODO
	private static final int SPIRIT_OF_THE_HUNTER_ID = 10296;
	@SuppressWarnings("unused") // TODO
	private static final int SPIRIT_OF_THE_SLAYER_ID = 10297;
	
	//Special Skills
	private static final int JUMP_ATTACK_ID = 10269;
	private static final int HURRICANE_RUSH_ID = 10267;
	protected static final int SECOND_WIND_ID = 10270;
	private static final int FORCE_OF_NATURE_ID = 10276;
	@SuppressWarnings("unused") // TODO
	private static final int REDUCE_ANGER_ID = 10278;
	private static final int GIANT_BARRIER_ID = 10279;
	protected static final int BOOST_ID = 10289;
	private static final int FERAL_BEAR_CRY_ID = 10291;
	private static final int FERAL_OGRE_CRY_ID = 10292;
	private static final int FERAL_PUMA_CRY_ID = 10293;
	private static final int FERAL_RABBIT_CRY_ID = 10294;
	private static final int HAWK_CRY_ID = 10295;
	protected static final int POWER_REVIVAL_ID = 10298;
	private static final int LAST_ATTACK_ID = 10300;
	
	//Toggle Skills
	private static final int TYRR_FORCE_ID = 1929;
	private static final int DUAL_MAXIMUM_HP = 1986;
	private static final int ROLLING_THUNDER = 10268;
	private static final int MOMENTUM_CHARGE = 10280;
	
	protected int _favoriteFeral;
	
	private final int[] ATTACK_SKILL_IDS =
	{
		ARMOR_DESTRUCTION_ID,
		MEGA_STRIKE_ID,
		POWER_BOMBER_ID,
		INFINITY_STRIKE_ID
	};
	
	private static final int[][] AREA_OF_EFFECT_SKILLS =
	{
		// [0] = SkillId
		// [1] = CastChances
		// [2] = The amount of nearby targets for granted cast, if the target is also in range.
		// [3] = The amount of nearby targets for granted cast, even if the target isn't in range.
		{ POWER_PROVOKE_ID, 15, 2, 3 },
		{ ERUPTION_ID, 25, 2, 3 },
		{ SONIC_STAR_ID, 20, 2, 3 },
		{ GIANT_PUNCH_ID, 15, 2, 4 },
		{ LIGHTNING_DISARM_ID, 5, 2, 5 },
	};
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		// Toggles
		TYRR_FORCE_ID,
		ROLLING_THUNDER,
		MOMENTUM_CHARGE,	//We should use it only when the player don't have enoff charges but always atm..
		DUAL_MAXIMUM_HP
	};
	
	private static final int[] FERAL_SKILL_IDS =
	{
		FERAL_BEAR_CRY_ID,
		FERAL_OGRE_CRY_ID,
		FERAL_PUMA_CRY_ID,
		FERAL_RABBIT_CRY_ID,
		HAWK_CRY_ID,
	};
	
	protected enum UsageStyle
	{
		ULTIMATES_USAGE_STYLE_ONE,
		ULTIMATES_USAGE_STYLE_TWO,
		ULTIMATES_USAGE_STYLE_THREE,
	}

	protected UsageStyle _ultimatesUsageStyle;

	public TyrrController(final L2PcInstance player)
	{
		super(player);
		
		// Just choose which Feral skill you like best and stick to that one...
		_favoriteFeral = FERAL_SKILL_IDS[Rnd.get(FERAL_SKILL_IDS.length)];
		
		// TODO m0ar!
		_ultimatesUsageStyle = UsageStyle.values()
				[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal())];
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		// Make sure the Aura is always up...
		if (!isFeralActive() && isSkillAvailable(_favoriteFeral))
			return _favoriteFeral;
		
		if (!(this instanceof TyrrTitanController))
		{
			// Every class, except the Titans, use Power Revival whenever they go very low on HP.
			if (isSkillAvailable(POWER_REVIVAL_ID) && getPlayerHealthPercent() < Rnd.get(5, 15))
				return POWER_REVIVAL_ID;
			
			// Every class, except the Titans, use Berserker  whenever they go very low on HP.
			if (isSkillAvailable(BERSERKER_ID) && getPlayerHealthPercent() < Rnd.get(20, 30))
				return BERSERKER_ID;
		}
		
		if (Rnd.nextBoolean() && isSkillAvailable(GIANT_BARRIER_ID)  && _player.getAllDebuffs().length > Rnd.get(0, 3))
			return GIANT_BARRIER_ID;
				
		if (Rnd.nextBoolean() && isSkillAvailable(LAST_ATTACK_ID) && getTargetHealthPercent() < 10)
			return LAST_ATTACK_ID;
		
		if (Rnd.nextBoolean() && isSkillAvailable(FORCE_OF_NATURE_ID) && _player.getCharges() < Rnd.get(3, 9))
			return FORCE_OF_NATURE_ID;
		
		final int distanceToTarget = getDistanceTo(target);
		
		if (distanceToTarget > 250)
		{
			if (Rnd.nextBoolean() && isSkillAvailable(HURRICANE_RUSH_ID))
				return HURRICANE_RUSH_ID;
			
			if (isSkillAvailable(JUMP_ATTACK_ID))
				return JUMP_ATTACK_ID;
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 20;
	}
	
	protected final boolean isFeralActive()
	{
		if (_player.getFirstEffect(_favoriteFeral) == null)
			return false;
		
		 return true;
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public int[][] getAreaOfEffectSkills()
	{
		return AREA_OF_EFFECT_SKILLS;
	}
	
	@Override
	public final int[] getCombatToggles()
	{
		return COMBAT_TOGGLE_IDS;
	}
}