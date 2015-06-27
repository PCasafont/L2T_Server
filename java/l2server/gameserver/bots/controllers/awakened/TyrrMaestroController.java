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

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author LittleHakor
 */
public class TyrrMaestroController extends TyrrController
{
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
	
	public TyrrMaestroController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
}
