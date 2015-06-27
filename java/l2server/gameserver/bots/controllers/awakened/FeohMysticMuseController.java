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
public class FeohMysticMuseController extends FeohController
{
	// Detonates an Elemental Destruction to inflict damage on nearby enemies with 683 Power. For 30 sec., M. Def. - 20%.
	// Cooldown: 6s
	@SuppressWarnings("unused")
	private static final int ELEMENTAL_BURST = 11106;
	
	// Inflicts damage with 1939Power to the target. Applies a stacking debuff that decreases Speed. At 5 stacks the target will be frozen for 2 seconds. When Double Casting is active, the debuff will not stack. Instead, there is a chance the target will be frozen for 3 seconds. Over-hit.
	// No Cooldown
	private static final int AQUA_CRASH_ID = 11177;
	
	// For 20 seconds, decreases incoming damage - 90% and defends against the rest by consuming MP. When attacked, reflects 10% of incoming damage. Ineffective when MP drops to 0.
	// Cooldown: 120s
	@SuppressWarnings("unused")
	private static final int ARCANE_BARRIER = 11065;
	
	public FeohMysticMuseController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		if (Rnd.get(0, 5) == 0 && isSkillAvailable(AQUA_CRASH_ID))
			return AQUA_CRASH_ID;
		
		return super.pickSpecialSkill(target);
	}
}