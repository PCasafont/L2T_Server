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

public class IssController extends BotController
{
	private static final int DEATH_STRIKE = 11511;
	private static final int SHADOW_BLADE = 11510;
	private static final int CRIPPLING_ATTACK = 11509;
	private static final int MASS_SHADOW_BLADE = 11514;
	private static final int MASS_CRIPPLING_ATTACK = 11513;
	private static final int ASSAULT_RUSH = 11508;
	
	//Toggle
	private static final int DUAL_MAXIMUM_HP = 1986;
	
	//Buffs
	protected static final int HORN_MELODY = 11517;
	protected static final int DRUM_MELODY = 11518;
	protected static final int PIPE_ORGAN_MELODY = 11519;
	protected static final int GUITAR_MELODY = 11520;
	protected static final int HARP_MELODY = 11521;
	protected static final int LUTE_MELODY = 11522;
	protected static final int KNIGHTS_HARMONY = 11523;
	protected static final int WARRIORS_HARMONY = 11524;
	protected static final int WIZARDS_HARMONY = 11525;
	protected static final int ELEMENTAL_RESISTANCE = 11565;
	protected static final int HOLY_ATTACK_RESISTANCE = 11566;
	protected static final int MENTAL_ATTACK_RESISTANCE = 11567;
	protected static final int HEALING_MELODY = 11570;
	@SuppressWarnings("unused") // TODO
	private static final int RECOVERY_MELODY = 11571;
	
	//Debuffs
	private static final int DIVINE_CANCEL = 11536;
	private static final int TRANSFORM = 11537;
	private static final int PETRIFY = 11539;
	
	//Special Skills
	@SuppressWarnings("unused") // TODO
	private static final int BATTLE_RHAPSODY = 11544;
	@SuppressWarnings("unused") // TODO
	private static final int CRAZY_NOCTURNE = 11545;
	@SuppressWarnings("unused") // TODO
	private static final int BLOOD_REQUIEM = 11533;
	@SuppressWarnings("unused") // TODO
	private static final int ANGELS_TOUCH = 11534;
	@SuppressWarnings("unused") // TODO
	private static final int QUICK_ESCAPE = 11540;
	@SuppressWarnings("unused") // TODO
	private static final int POLYMORPH = 11541;
	@SuppressWarnings("unused") // TODO
	private static final int DISPERSE = 11543;
	@SuppressWarnings("unused") // TODO
	private static final int CELESTIAL_AEGIS = 11560;
	private static final int GIANT_ROOT = 11561;
	@SuppressWarnings("unused") // TODO
	private static final int QUICK_RETURN = 11563;
	@SuppressWarnings("unused") // TODO
	private static final int ANGELS_RESURRECTION = 11564;
	@SuppressWarnings("unused") // TODO
	private static final int DEVILS_MOVEMENT = 11562;
	private static final int CHAOS_SYMPHONY = 11546;
	private static final int MASS_GIANT_ROOT = 11538;
	private static final int ULTIMATE_SCOURGE = 11557;
	private static final int ULTIMATE_OBLIVION = 11558;
	private static final int ULTIMATE_SUSPENSION = 11559;
	
	private final int[] ATTACK_SKILL_IDS =
	{
		MASS_CRIPPLING_ATTACK,
		MASS_SHADOW_BLADE,
		CRIPPLING_ATTACK,
		SHADOW_BLADE,
		DEATH_STRIKE
	};
	
	private final static int[] COMBAT_TOGGLE_IDS =
	{
		DUAL_MAXIMUM_HP
	};
		
	private final int[] DEBUFF_SKILL_IDS =
	{
		DIVINE_CANCEL,
		TRANSFORM,
		PETRIFY,
	};
		
	private static final int[][] AREA_OF_EFFECT_SKILLS =
	{
		// [0] = SkillId
		// [1] = CastChances
		// [2] = The amount of nearby targets for granted cast, if the target is also in range.
		// [3] = The amount of nearby targets for granted cast, even if the target isn't in range.
		{ ULTIMATE_OBLIVION, 5, 2, 4 },
		{ ULTIMATE_SCOURGE, 5, 2, 4 },
		{ MASS_GIANT_ROOT, 5, 2, 4 },
		{ ULTIMATE_SUSPENSION, 5, 2, 5 },
		{ CHAOS_SYMPHONY, 15, 2, 3 },
	};
	
	private enum UsageStyle
	{
		ULTIMATES_USAGE_STYLE_ONE,
		ULTIMATES_USAGE_STYLE_TWO,
		ULTIMATES_USAGE_STYLE_THREE,
	}

	@SuppressWarnings("unused")
	private UsageStyle _ultimatesUsageStyle;
	
	public IssController(final L2PcInstance player)
	{
		super(player);
		
		_ultimatesUsageStyle = UsageStyle.values()
				[Rnd.get(UsageStyle.ULTIMATES_USAGE_STYLE_ONE.ordinal(), UsageStyle.ULTIMATES_USAGE_STYLE_THREE.ordinal())];
	}
	
	@Override
	protected int pickSpecialSkill(final L2Character target)
	{
		final int distanceToTarget = getDistanceTo(target);
		
		if (distanceToTarget > 250)
		{
			if (isSkillAvailable(ASSAULT_RUSH))
				return ASSAULT_RUSH;
		}
		
		if (!isTargetGettingCloser() && isSkillAvailable(GIANT_ROOT))
			return GIANT_ROOT;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public boolean checkIfIsReadyToFight()
	{
		return super.checkIfIsReadyToFight();
	}
	
	@Override
	protected final int getGeneralAttackRate()
	{
		return 0;
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