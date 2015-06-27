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
public class FeohStormScreamerController extends FeohController
{
	// Detonates an Elemental Destruction to inflict damage on nearby enemies with 683 Power. For 30 sec., M. Def. - 20%.
	// Cooldown: 6s
	@SuppressWarnings("unused")
	private static final int ELEMENTAL_BURST = 11106;
	
	// For 30 minutes, increases your magic MP Consumption + 35% and M. Atk. + 25%.
	// Cooldown: 15s
	@SuppressWarnings("unused")
	private static final int EMPOWERING_ECHO_ID = 1457;
	
	// For 20 seconds, decreases incoming damage - 90% and defends against the rest by consuming MP. When attacked, reflects 10% of incoming damage. Ineffective when MP drops to 0.
	// Cooldown: 120s
	@SuppressWarnings("unused")
	private static final int ARCANE_BARRIER = 11065;
	
	public FeohStormScreamerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
}