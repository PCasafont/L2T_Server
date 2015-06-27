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
public class WynnArcanaLordController extends WynnController
{
	@SuppressWarnings("unused") // TODO
	private static final int SUMMON_PROTECTION_STONE = 11359;
	private static final int SUMMON_KAI_THE_CAT = 11320;
	private static final int SUMMON_FELINE_KING = 11321;
	private static final int SUMMON_FELINE_QUEEN = 11322;
	
	private static final int ARCANE_RAGE = 11350;
	@SuppressWarnings("unused") // TODO
	private static final int GREATER_SERVITOR_HASTE = 11347;
	
	private final int[] ATTACK_SKILL_IDS =
	{
		ARCANE_RAGE
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_KAI_THE_CAT,
		SUMMON_FELINE_KING,
		SUMMON_FELINE_QUEEN
	};
	
	public WynnArcanaLordController(final L2PcInstance player)
	{
		super(player);
	}
	
	@Override
	protected final int pickSpecialSkill(final L2Character target)
	{
		return super.pickSpecialSkill(target);
	}
	
	@Override
	public final int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
	
	@Override
	public int[] getAttackSkillIds()
	{
		return ATTACK_SKILL_IDS;
	}
}