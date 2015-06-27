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
public class FeohSoultakerController extends FeohController
{
	private static final int SUMMON_REANIMATED_MAN_ID = 1129;
	private static final int TRANSFER_PAIN_ID	= 1262;
	@SuppressWarnings("unused") // TODO
	private static final int CURSE_GLOOM_ID = 1263;
	@SuppressWarnings("unused") // TODO
	private static final int SACRIFICIAL_SOUL_ID = 11152;
	
	private final static int[] ESSENTIAL_BUFF_SKILL_IDS =
	{
		TRANSFER_PAIN_ID
	};
	
	private final static int[] SUMMON_SKILL_IDS =
	{
		SUMMON_REANIMATED_MAN_ID
	};
	
	public FeohSoultakerController(final L2PcInstance player)
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
	
	@Override
	public final int[] getSummonSkillIds()
	{
		return SUMMON_SKILL_IDS;
	}
}