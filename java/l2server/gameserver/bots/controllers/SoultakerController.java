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
public class SoultakerController extends NukerController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1177, // Wind Strike
		1274, // Energy Bolt
		1147, // Vampiric Touch
		1220, // Blaze
		1159, // Curse Death Link
		1234, // Vampiric Claw
		1148, // Death Spike
		1343, // Dark Vortex
	};
	
	private final int[] LOW_RANGE_ATTACK_SKILL_IDS =
	{
		1172, // Aura Burn
		1181, // Flame Strike
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		1287, // Seed of Wind
		1457, // Empowering Echo
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		1298, // Mass Slow
		1381, // Mass Fear
		1382, // Mass Gloom
		1169, // Curse Fear
		1170, // Anchor
		1337, // Curse of Abyss
	};
	
	private final static int[] AOE_DEBUFF_SKILL_IDS =
	{
		1345, // Mass Mage Bane
		1344, // Mass Warrior Bane
	};
	
	private static final int SUMMON_REANIMATED_MAN_ID = 1129;
	private static final int SUMMON_CURSED_MAN_ID = 1334;
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_REANIMATED_MAN_ID,
		SUMMON_CURSED_MAN_ID
	};
	
	private static final int CURSE_CHAOS_ID = 1222;
	private static final int CURSE_GLOOM_ID = 1263;
	private static final int SILENCE_ID = 1064;
	private static final int CURSE_OF_DOOM_ID = 1336;
	private static final int CURSE_DEATH_LINK_ID = 1159;
	
	private static final int ARCHANE_POWER_ID = 337;
	
	private final boolean _isSummonAllowedToAssist;
	private final boolean _isAllowedToSpawnSummonInCombat;
	
	public SoultakerController(final L2PcInstance player)
	{
		super(player);
		
		_isSummonAllowedToAssist = Rnd.nextBoolean();
		_isAllowedToSpawnSummonInCombat = Rnd.nextBoolean();
	}
	
	@Override
	protected final void spawnMySummon()
	{
		int summonSkillId = Rnd.nextBoolean() ? SUMMON_REANIMATED_MAN_ID : SUMMON_CURSED_MAN_ID;
		
		if (!isSkillAvailable(summonSkillId))
			summonSkillId = summonSkillId == SUMMON_REANIMATED_MAN_ID ? SUMMON_CURSED_MAN_ID : SUMMON_REANIMATED_MAN_ID;
		
		if (!isSkillAvailable(summonSkillId))
			summonSkillId = 0;
		
		if (summonSkillId != 0)
			useSkill(summonSkillId);
	}
	
	@Override
	protected boolean isSummonAllowedToAssist()
	{
		return _isSummonAllowedToAssist;
	}
	
	@Override
	protected boolean isAllowedToSummonInCombat()
	{
		return _isAllowedToSpawnSummonInCombat;
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		if (isSkillAvailable(CURSE_GLOOM_ID) && target.getFirstEffect(CURSE_GLOOM_ID) == null)
			return CURSE_GLOOM_ID;
		
		final int healthPercent = getPlayerHealthPercent();
		
		if (healthPercent < 25 && Rnd.nextBoolean())
			return CURSE_DEATH_LINK_ID;
		
		if (healthPercent < 15)
			return CURSE_DEATH_LINK_ID;
		
		final L2PcInstance targetedPlayer = target instanceof L2PcInstance ? (L2PcInstance) target : null;
		
		if (targetedPlayer != null)
		{
			if (targetedPlayer.isMage())
			{
				if (Rnd.get(0, 2) == 0 && !targetedPlayer.isMuted() && isSkillAvailable(SILENCE_ID))
					return SILENCE_ID;
			}
			else
			{
				if (Rnd.get(0, 2) == 0 && !targetedPlayer.isPhysicalMuted() && isSkillAvailable(CURSE_OF_DOOM_ID))
					return CURSE_OF_DOOM_ID;
			}
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
	public final int[] getDebuffSkillIds()
	{
		return DEBUFF_SKILL_IDS;
	}
	
	@Override
	public final int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
}