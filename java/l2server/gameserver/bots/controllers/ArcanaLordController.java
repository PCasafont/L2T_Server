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
public class ArcanaLordController extends SummonerController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1177, // Wind Strike
		1274, // Energy Bolt
		1147, // Vampiric Touch
		1220, // Blaze
		1558, // Dimension Spiral
	};
	
	private final int[] LOW_RANGE_ATTACK_SKILL_IDS =
	{
		1172, // Aura Burn
		1181, // Flame Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		10, // Summon Storm Cubic
		1279, // Summon Binding Cubic
		1328, // Mass Summon Storm Cubic
		781, // Summon Smart Cubic
	};
	
	private static final int SUMMON_KAT_THE_CAT_ID = 1111;
	private static final int SUMMON_MEW_THE_CAT_ID = 1225;
	private static final int SUMMON_KAI_THE_CAT_ID = 1276;
	private static final int SUMMON_FELINE_KING_ID = 1406;
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_KAT_THE_CAT_ID,
		SUMMON_MEW_THE_CAT_ID,
		SUMMON_KAI_THE_CAT_ID,
		SUMMON_FELINE_KING_ID
	};

	private static final int MASS_SURRENDER_TO_FIRE = 1383;
	private static final int ARCANE_DISRUPTION = 1386;
	private static final int WARRIOR_BANE = 1350;
	private static final int MAGE_BANE = 1351;
	
	public ArcanaLordController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (targetedPlayer != null)
		{
			if (targetedPlayer.isMageClass())
			{
				if (isSkillAvailable(ARCANE_DISRUPTION) && target.getFirstEffect(ARCANE_DISRUPTION) == null)
					return ARCANE_DISRUPTION;
				
				if (Rnd.get(0, 3) == 0 && isSkillAvailable(MAGE_BANE) && target.getFirstEffect(MAGE_BANE) == null)
					return MAGE_BANE;
			}
			else if (Rnd.get(0, 3) == 0 && isSkillAvailable(WARRIOR_BANE) && target.getFirstEffect(WARRIOR_BANE) == null)
				return WARRIOR_BANE;
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