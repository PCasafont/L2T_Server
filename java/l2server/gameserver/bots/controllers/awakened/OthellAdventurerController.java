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
public class OthellAdventurerController extends OthellController
{
	// For 15 seconds, increases Speed by 66, P. Evasion by 10, and HP Recovery Bonus by 40.
	// Cooldown: 60s
	@SuppressWarnings("unused")
	private static final int SHADOW_DASH_ID = 10525;
	
	// For 30 sec., has a chance of cancelling the target of the enemy that attacked you. Requires a dagger or dual dagger.
	// Cooldown: 300s
	@SuppressWarnings("unused")
	private static final int ELUSIVE_MIRAGE_ID = 10558;
	
	// For 20 sec., Vital Spot Attack Rate + 10%. Requires a dagger or dual dagger.
	// Cooldown: 300s
	@SuppressWarnings("unused")
	private static final int MAXIMUM_BLOW_ID = 10560;
	
	// P. Def. + 500, Shield Defense + 1000, and shield defense + 50%. MP is continuously consumed.
	// Cooldown: 30s
	protected static final int CRITICAL_ADVENTURES_ID = 10562;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		CRITICAL_ADVENTURES_ID
	};
	
	public OthellAdventurerController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}