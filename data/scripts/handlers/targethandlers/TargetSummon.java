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

package handlers.targethandlers;

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nBd
 */
public class TargetSummon implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if (skill.getTargetDirection() == L2SkillTargetDirection.ALL_SUMMONS)
		{
			if (!(activeChar instanceof L2PcInstance))
			{
				return null;
			}

			List<L2Character> targetList = new ArrayList<L2Character>();

			//LasTravel: Servitor Balance Life should balance owner too
			if (skill.getId() == 11299)
			{
				targetList.add(activeChar);
			}

			for (L2Summon summon : ((L2PcInstance) activeChar).getSummons())
			{
				if (!summon.isDead())
				{
					targetList.add(summon);
				}
			}

			return targetList.toArray(new L2Character[targetList.size()]);
		}
		else
		{
			if (!(target instanceof L2Summon))
			{
				target = ((L2PcInstance) activeChar).getSummon(0);
			}
			if (target != null && !target.isDead() && target instanceof L2Summon &&
					((L2PcInstance) activeChar).getSummons().contains(target))
			{
				return new L2Character[]{target};
			}
		}

		return null;
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_SUMMON;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetSummon());
	}
}
