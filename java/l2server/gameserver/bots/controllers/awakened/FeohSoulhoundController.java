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
public class FeohSoulhoundController extends FeohController
{
	// Increases P. Def. + 6000 for 20 seconds.
	// Cooldown: 200s
	@SuppressWarnings("unused")
	private static final int PHYSICAL_SOUL_BARRIER_ID = 11225;
	
	// Sends an electric current to attack nearby enemies with 188 Power added to M. Atk. Paralyzes for 3 seconds. Decreases received HP Recovery Rate - 50%.
	// Cooldown: 5s
	@SuppressWarnings("unused")
	private static final int LIGHTNING_SHOCK_ID = 791;
	
	public FeohSoulhoundController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
}