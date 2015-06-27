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
public class IssSpectralDancerController extends IssController
{
	// For 30 min., P. Atk. + 5%, Atk. Spd. + 5%, P. Critical Rate + 10, and Speed + 10.
	// Cooldown: 30s
	private static final int DANCE_OF_MIGHT_ID = 11606;

	// For 5 minutes, increases party members' Atk. Spd. + 15%, Casting Spd. + 30%, Max HP + 30%, and MP recovery bonus + 20%. Decreases magic skill MP Consumption - 10% and the magic cancel rate.
	// Cooldown: 2s
	private static final int PREVAILING_DANCE_ID = 11610;

	// For 5 minutes, increases party members' P. Atk. + 8%, M. Atk. + 16%, Atk. Spd. and Casting Spd. + 8%. Decreases P. Def. - 3%, M. Def. - 11%, and P. Evasion - 4. Chance of applying 8% Vampiric Rage effect. Reflects 10% of incurred damage back to the enemy.
	// Cooldown: 2s
	private static final int DARING_DANCE_ID = 11611;

	// For 5 minutes, increases party members' HP Recovery Bonus + 20%, P. Accuracy + 4, P. Evasion + 3, and Spd. + 20. Decreases skill MP Consumption - 20% and skill Cooldown - 10%.
	// Cooldown: 2s
	private static final int REFRESHING_DANCE_ID = 11612;

	// For 30 sec., increases your P. Atk./M. Atk./Atk. Spd./Casting Spd. + 100%.
	// Cooldown: 600s
	@SuppressWarnings("unused")
	private static final int BATTLE_RHAPSODY_DANCE_ID = 11614;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		HORN_MELODY,
		DRUM_MELODY,
		PIPE_ORGAN_MELODY,
		GUITAR_MELODY,
		HARP_MELODY,
		LUTE_MELODY,
		
		// Spectral Dancer Dances
		DANCE_OF_MIGHT_ID,
		PREVAILING_DANCE_ID,
		DARING_DANCE_ID,
		REFRESHING_DANCE_ID
	};
	
	public IssSpectralDancerController(final L2PcInstance player)
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