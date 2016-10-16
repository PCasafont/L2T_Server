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
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sandro
 */
public class TargetAreaSummon implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();
		// FIXME target = activeChar.getPet();
		if (target == null || !(target instanceof L2SummonInstance) || target.isDead())
		{
			return null;
		}

		if (onlyFirst)
		{
			return new L2Character[]{target};
		}

		final Collection<L2Character> objs = target.getKnownList().getKnownCharacters();
		final int radius = skill.getSkillRadius();

		for (L2Character obj : objs)
		{
			if (obj == null || obj == target || obj == activeChar)
			{
				continue;
			}

			if (!Util.checkIfInRange(radius, target, obj, true))
			{
				continue;
			}

			if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
			{
				continue;
			}

			targetList.add(obj);
		}

		if (targetList.isEmpty())
		{
			return null;
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_AREA_SUMMON;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAreaSummon());
	}
}
