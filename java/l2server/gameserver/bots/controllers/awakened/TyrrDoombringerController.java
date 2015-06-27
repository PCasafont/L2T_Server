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
public class TyrrDoombringerController extends TyrrController
{
	// For 30 sec., increases P. Def. + 7500.
	// Cooldown: 300s
	private static final int SWORD_SHIELD_ID = 483;

	// Disarms the enemy for 5 seconds. Requires an ancient sword, sword, blunt, spear, fist weapon, dual blunt, or dualsword.
	// Cooldown: 60s
	private static final int DISARM_ID = 485;

	// Spreads the soul's defensive barrier to increase your Bow Resistance + 45, Crossbow Resistance + 45, and M. Def. + 150% for 30 seconds.
	// Cooldown: 300s
	private static final int SOUL_BARRIER_ID = 1514;
	
	// Rushes forward to attack enemies at the front with 16747 Power added to P. Atk. Inflicts Stun for 5 seconds. Requires an ancient sword, sword, blunt, spear, fist weapon, dual blunt, or dualsword.
	// No Cooldown
	private static final int RUSH_IMPACT_ID = 793;
	
	// Attacks the target with 693 power, lowers P. Def., and absorbs Momentum. Requires a sword, blunt, spear, fist, dual blunt, or dualsword.
	// No Cooldown
	private static final int HURRICANE_BLASTER_ID = 10263;
	
	// Attacks near the enemy with 16747 Power added to P. Atk. Power - 10% when using a sword, dualsword, blunt, or fist weapon. Power + 50% when using a spear. Requires a sword, blunt, spear, fist weapon, dual blunt, or dualsword. Over-hit. Critical hit.
	// Cooldown: 10s
	private static final int HURRICANE_STORM_ID = 10288;
	
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
	
	public TyrrDoombringerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final int distanceToTarget = getDistanceTo(target);
		
		if (distanceToTarget > 250)
		{
			if (isSkillAvailable(RUSH_IMPACT_ID))
				return RUSH_IMPACT_ID;
		}
		
		switch (_ultimatesUsageStyle)
		{
			// Style 1:
			// Use Sword Shield whenever possible.
			// Use Disarm whenever possible.
			// Use Soul Barrier whenever possible.
			// Use Boost whenever possible.
			// Use Second Wind whenever you hit less than 50 to 75% of HP.
			case ULTIMATES_USAGE_STYLE_ONE:
			{
				if (Rnd.nextBoolean() && isSkillAvailable(SWORD_SHIELD_ID))
					return SWORD_SHIELD_ID;
				
				if (target instanceof L2PcInstance && isSkillAvailable(DISARM_ID))
					return DISARM_ID;
				
				if (Rnd.nextBoolean() && isSkillAvailable(SOUL_BARRIER_ID))
					return SOUL_BARRIER_ID;
				
				if (Rnd.nextBoolean() && isSkillAvailable(BOOST_ID))
					return BOOST_ID;
				
				if (getPlayerHealthPercent() < Rnd.get(50, 75) && isSkillAvailable(SECOND_WIND_ID))
					return SECOND_WIND_ID;
				
				break;
			}
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
}
