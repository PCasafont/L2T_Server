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

/**
 *
 * @author LittleHakor
 */
@SuppressWarnings("unused")
public class DominatorController extends MageController
{
	// Best skills on the bottom.
	private final int[] ATTACK_SKILL_IDS =
	{
		1090, // Life Drain
		1245, // Steal Essence
	};
	
	private final int[] LOW_RANGE_ATTACK_SKILL_IDS =
	{
		927, // Burning Chop
	};
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		1414, // Victory of Pagrio
	};
	
	private final static int[] DEBUFF_SKILL_IDS =
	{
		100, // Stun Attack
		260, // Hammer Crush
		
	};
	
	private final static int[] AOE_DEBUFF_SKILL_IDS =
	{
		1096, // Seal of Chaos
		1099, // Seal of Slow
		1208, // Seal of Binding
		1209, // Seal of Poison
		1104, // Seal of Winter
		1246, // Seal of Silence
		1247, // Seal of Scourge
		1248, // Seal of Suspension
		1366, // Seal of Despair
		1462, // Seal of Blockade
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		1129, // Summon Reanimated Man
		1334, // Summon Cursed Man
	};
	
	private static final int SOUL_GUARD_ID = 1283;
	private static final int SOUL_CRY_ID = 1001;
	
	// CP Recovery Skills
	private static final int HONOR_OF_PAGRIO_ID = 1305;
	private static final int RITUAL_OF_LIFE_ID = 1306;
	
	// HP Recovery Skills
	private static final int HEART_OF_PAGRIO_ID = 1256;
	
	private static final int VICTORY_OF_PAGRIO_ID = 1414;
	
	private static final int ARCHANE_POWER_ID = 337;
	
	public DominatorController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		int selectedSkillId = -1;

		if (selectedSkillId != -1)
			return selectedSkillId;
		
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
