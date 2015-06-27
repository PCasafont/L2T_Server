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
package l2server.gameserver.bots.controllers;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.util.Rnd;

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class SpectralMasterController extends SummonerController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1184, // Ice Bolt
		1177, // Wind Strike
		1147, // Vampiric Touch
		1178, // Twister
		1266, // Shadow Spark
		1530, // Death Spike
		1558, // Dimension Spiral
	};
	
	private final int[] LOW_RANGE_ATTACK_SKILL_IDS =
	{
		1172, // Aura Burn
		1181, // Flame Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		33, // Summon Phantom Cubic
		// 1281, // Summon Spark Cubic TODO Check why it stucks
		1330, // Mass Summon Phantom Cubic
		783, // Summon Smart Cubic
	};
	
	private static final int SUMMON_SHADOW_ID = 1128;
	private static final int SUMMON_SILHOUETTE_ID = 1228;
	private static final int SUMMON_SOULLESS_ID = 1278;
	private static final int SUMMON_NIGHT_SHADE_ID = 1333;
	private static final int SUMMON_SPECTRAL_LORD_ID = 1408;
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_SHADOW_ID,
		SUMMON_SILHOUETTE_ID,
		SUMMON_SOULLESS_ID,
		SUMMON_NIGHT_SHADE_ID,
		SUMMON_SPECTRAL_LORD_ID
	};

	private static final int MASS_SURRENDER_TO_WIND = 1385;
	private static final int MAGE_BANE = 1351;
	
	public SpectralMasterController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (targetedPlayer != null)
		{
			if (targetedPlayer.isMage() && Rnd.get(0, 3) == 0 && isSkillAvailable(MAGE_BANE) && target.getFirstEffect(MAGE_BANE) == null)
				return MAGE_BANE;
		}
		
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
	
	@Override
	public final int[] getEssentialBuffSkillIds()
	{
		return ESSENTIAL_BUFF_SKILL_IDS;
	}
	
	@Override
	public final int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
}