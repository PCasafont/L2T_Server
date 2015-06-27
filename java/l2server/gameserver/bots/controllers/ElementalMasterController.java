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
public class ElementalMasterController extends SummonerController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1184, // Ice Bolt
		1177, // Wind Strike
		1274, // Energy Bolt
		1175, // Aqua Swirl
		1264, // Solar Spark
		1558, // Dimension Spiral
	};
	
	private final int[] LOW_RANGE_ATTACK_SKILL_IDS =
	{
		1172, // Aura Burn
		1181, // Flame Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		67, // Summon Life Cubic
		1280, // Summon Aqua Cubic
		1329, // Mass Summon Aqua Cubic
		782, // Summon Smart Cubic
	};
	
	private static final int SUMMON_BOXER_THE_UNICORN_ID = 1226;
	private static final int SUMMON_MIRAGE_THE_UNICORN_ID = 1227;
	private static final int SUMMON_MERROW_THE_UNICORN_ID = 1277;
	private static final int SUMMON_SERAPHIM_THE_UNICORN_ID = 1332;
	private static final int SUMMON_MAGNUS_THE_UNICORN_ID = 1407;
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_MIRAGE_THE_UNICORN_ID,
		SUMMON_MERROW_THE_UNICORN_ID,
		SUMMON_SERAPHIM_THE_UNICORN_ID,
		SUMMON_MAGNUS_THE_UNICORN_ID
	};

	private static final int MASS_SURRENDER_TO_WATER = 1384;
	private static final int WARRIOR_BANE = 1350;
	
	public ElementalMasterController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (targetedPlayer != null)
		{
			if (targetedPlayer.isFighter() && Rnd.get(0, 3) == 0 && isSkillAvailable(WARRIOR_BANE) && target.getFirstEffect(WARRIOR_BANE) == null)
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