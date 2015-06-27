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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author LittleHakor
 */
public class TyrrDreadnoughtController extends TyrrController
{
	// You have reached your physical limit. Increases debuff resistance, Speed, P. Atk., Accuracy, P. Def., Atk. Spd., and Critical Damage.
	// When the target is killed, recovers HP by 20%. When HP falls below 60%, increases P. Atk. by 10% and Critical Rate by 100.
	// When HP falls below 30%, increases P. Atk. by 30% and Critical Rate by an additional 300. Can be used when HP is below 70%.
	// Cooldown: 300s
	@SuppressWarnings("unused")
	private static final int BERSERKER_ID = 10274;
	
	// Swings a spear to attack nearby enemies with 37680 Power added to P. Atk. and causes Stun for 9 seconds. Requires a spear. Ignores Shield Defense. Over-hit. Critical hit.
	// Cooldown: 5s
	private static final int THUNDER_STORM_ID = 48;
	
	// Swings a spear at nearby enemies and causes - 45% CP. Requires a spear.
	// Cooldown: 30s
	private static final int WRATH_ID = 320;
	
	// Attacks the target with 693 power, lowers P. Def., and absorbs Momentum. Requires a sword, blunt, spear, fist, dual blunt, or dualsword.
	// No Cooldown
	private static final int HURRICANE_BLASTER_ID = 10263;
	
	// Attacks near the enemy with 16747 Power added to P. Atk. Power - 10% when using a sword, dualsword, blunt, or fist weapon. Power + 50% when using a spear. Requires a sword, blunt, spear, fist weapon, dual blunt, or dualsword. Over-hit. Critical hit.
	// Cooldown: 10s
	private static final int HURRICANE_STORM_ID = 10288;
	
	// Attacks near the enemy by throwing a spear with 32656 Power added to P. Atk. Stuns for 9 seconds. Requires a spear. Over-hit. Critical hit. Ignores Shield Defense. Target cancel.
	// Cooldown: 30s
	private static final int THUNDER_SPEAR_ID = 10321;
	
	// Provokes enemies within a wide range and Spear Resistance - 25 for 10 seconds.
	// Cooldown: 4s
	private static final int PROVOKE_ID = 286;
	
	// From the least powerful to the most powerful, always.
	// The index influences the priority.
	private final int[] ATTACK_SKILL_IDS =
	{
		HURRICANE_BLASTER_ID,
		HURRICANE_STORM_ID,
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
		
		// Tyrr Dreadnoughts...
		{ PROVOKE_ID, 25, 2, 4 },
		{ WRATH_ID, 25, 2, -1 },
		{ THUNDER_STORM_ID, 5, 2, -1 },
		{ THUNDER_SPEAR_ID, 15, 2, -1 },
	};
	
	public TyrrDreadnoughtController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[][] getAreaOfEffectSkills()
	{
		return AREA_OF_EFFECT_SKILLS;
	}
}