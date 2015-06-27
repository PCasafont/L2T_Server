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
public class IssSwordMuseController extends IssController
{
	// For 30 min., P. Def. + 5%, Attribute Def. + 20, Resistance to bows/crossbows + 10%, and Debuff Resistance + 10%.
	// Cooldown: 30s
	private static final int SHIELD_SONG_ID = 11605;

	// For 5 minutes, increases party members' Atk. Spd. + 15%, Casting Spd. + 30%, Max HP + 30%, and MP recovery bonus + 20%. Decreases magic skill MP Consumption - 10% and the magic cancel rate.
	// Cooldown: 2s
	private static final int PREVAILING_SONG_ID = 11607;

	// For 5 minutes, increases party members' P. Atk. + 8%, M. Atk. + 16%, Atk. Spd. and Casting Spd. + 8%. Decreases P. Def. - 3%, M. Def. - 11%, and P. Evasion - 4. Chance of applying 8% Vampiric Rage effect. Reflects 10% of incurred damage back to the enemy.
	// Cooldown: 2s
	private static final int DARING_SONG_ID = 11608;

	// For 5 minutes, increases party members' HP Recovery Bonus + 20%, P. Accuracy + 4, P. Evasion + 3, and Spd. + 20. Decreases skill MP Consumption - 20% and skill Cooldown - 10%.
	// Cooldown: 2s
	private static final int REFRESHING_SONG_ID = 11609;

	// For 30 sec., increases your P. Def. + 5000 and M. Def. + 5000. Restores 5000 HP.
	// Cooldown: 600s
	@SuppressWarnings("unused")
	private static final int SONG_OF_PROTECTION_ID = 11613;
	
	// Sings a Song of Silence. Blocks all the enemies' physical/magic skills for 10 seconds.
	// Cooldown: 120s
	@SuppressWarnings("unused")
	private static final int SONG_OF_SILENCE_ID = 437;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		HORN_MELODY,
		DRUM_MELODY,
		PIPE_ORGAN_MELODY,
		GUITAR_MELODY,
		HARP_MELODY,
		LUTE_MELODY,
		
		// Spectral Dancer Dances
		SHIELD_SONG_ID,
		PREVAILING_SONG_ID,
		DARING_SONG_ID,
		REFRESHING_SONG_ID
	};
	
	public IssSwordMuseController(final L2PcInstance player)
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