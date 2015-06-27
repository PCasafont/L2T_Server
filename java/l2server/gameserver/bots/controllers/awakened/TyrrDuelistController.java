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
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
public class TyrrDuelistController extends TyrrController
{
	// You have reached your physical limit. Increases debuff resistance, Speed, P. Atk., Accuracy, P. Def., Atk. Spd., and Critical Damage.
	// When the target is killed, recovers HP by 20%. When HP falls below 60%, increases P. Atk. by 10% and Critical Rate by 100.
	// When HP falls below 30%, increases P. Atk. by 30% and Critical Rate by an additional 300. Can be used when HP is below 70%.
	// Cooldown: 300s
	@SuppressWarnings("unused")
	private static final int BERSERKER_ID = 10274;
	
	// Attacks near the enemy with 21771 Power added to P. Atk. Requires a sword, blunt, spear, fist weapon, dual blunt, or dualsword. Over-hit. Critical hit.
	// Cooldown: 10s
	private static final int SONIC_STORM_ID = 7;

	// Gathers Momentum to attack the target with 33377 power. Damage increases as Momentum is used up. Damage + 30% when the maximum Momentum 3 are consumed. Requires a sword, blunt, spear, fist weapon, dual blunt, or dualsword.
	// Cooldown: 10s
	private static final int TRIPLE_SONIC_SLASH_ID = 261;

	// Attacks the target with a deadly energy storm with 757 Power, lowers P. Def., and absorbs Momentum. Requires a sword, blunt, fist weapon, dual blunt, or dualsword.
	// No Cooldown
	private static final int SONIC_RAGE_ID = 345;
	
	// Launches a wave of sword energy to inflict 25700 damage added to P. Atk., increasing the damage as sword energy is consumed.
	// Can consume up to 3. Requires a dual sword, sword, or blunt. Over-hit. Critical.
	// Cooldown: 10s
	private static final int SONIC_FLASH_ID = 10318;
	
	// Engages the target in 1:1 combat. Ignores all normal attacks, skills, and debuffs, except those of the opponent.
	// Increases your P. Atk., Spd., and debuff resistance.
	// Cooldown: 600s
	@SuppressWarnings("unused")
	private static final int FACEOFF_ID = 10320;
	
	// From the least powerful to the most powerful, always.
	// The index influences the priority.
	private final int[] ATTACK_SKILL_IDS =
	{
		SONIC_STORM_ID,
		SONIC_FLASH_ID,
		TRIPLE_SONIC_SLASH_ID,
		ARMOR_DESTRUCTION_ID,
		MEGA_STRIKE_ID,
		POWER_BOMBER_ID,
		INFINITY_STRIKE_ID
	};
	
	public TyrrDuelistController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		if (isSkillAvailable(SONIC_RAGE_ID) && Rnd.get(0, 3) == 0 && _player.getCharges() < 15)
			return SONIC_RAGE_ID;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
}
