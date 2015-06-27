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
public class IssDoomcryerController extends IssController
{
	// Increases party members' Atk. Spd. by 15%, Casting Spd. by 30%, Max HP by 30%, MP recovery bonus by 20% and decreases MP Consumption for magic skill by 10% and the magic cancel rate for 5 minutes.
	// Cooldown: 2s
	private static final int PREVAILING_SONATA = 11529;

	// Increases party members' P. Atk. by 8%, M. Atk. by 16%, Atk. Spd. and Casting Spd. by 8% and decreases P. Def. by 3%, M. Def. by 11% and P. Evasion by 4 for 5 minutes. Also bestows an 8% Vampiric Rage effect and a chance of reflecting 10% of the damage received.
	// Cooldown: 2s
	private static final int DARING_SONATA = 11530;

	// Increases party members' HP Recovery Bonus by 20%, P. Accuracy by 4, P. Evasion by 3, Speed by 20, and decreases MP Consumption for skills by 20% and skill Cooldown by 10% for 5 minutes.
	// Cooldown: 2s
	private static final int REFRESHING_SONATA = 11532;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		HORN_MELODY,
		DRUM_MELODY,
		PIPE_ORGAN_MELODY,
		GUITAR_MELODY,
		HARP_MELODY,
		LUTE_MELODY,
		
		// Hierophant Buffs
		PREVAILING_SONATA,
		DARING_SONATA,
		REFRESHING_SONATA,
	};
	
	// Strikes the target with a magical cold flame. For 10 seconds, causes - 358 HP per second on the enemy and - 423 during PVP.
	// Cooldown: 5s
	@SuppressWarnings("unused")
	private static final int FREEZING_FLAME_ID = 1244;
	
	public IssDoomcryerController(final L2PcInstance player)
	{
		super(player);
	}

	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		@SuppressWarnings("unused")
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
}