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

import l2server.gameserver.GeoEngine;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetAreaUndead implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		L2Character cha;

		int radius = skill.getSkillRadius();

		if (skill.getCastRange() >= 0 && (target instanceof L2Npc || target instanceof L2SummonInstance) &&
				target.isUndead() && !target.isAlikeDead())
		{
			cha = target;

			if (onlyFirst == false)
			{
				targetList.add(cha); // Add target to target list
			}
			else
			{
				return new L2Character[]{cha};
			}
		}
		else
		{
			cha = activeChar;
		}

		Collection<L2Object> objs = cha.getKnownList().getKnownObjects().values();
		//synchronized (cha.getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if (obj instanceof L2Npc)
				{
					target = (L2Npc) obj;
				}
				else if (obj instanceof L2SummonInstance)
				{
					target = (L2SummonInstance) obj;
				}
				else
				{
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, target))
				{
					continue;
				}

				if (!target.isAlikeDead()) // If target is not dead/fake death and not self
				{
					if (!target.isUndead())
					{
						continue;
					}

					if (!Util.checkIfInRange(radius, cha, obj, true))
					{
						continue;
					}

					if (onlyFirst == false)
					{
						targetList.add((L2Character) obj);
					}
					else
					{
						return new L2Character[]{(L2Character) obj};
					}
				}
			}
		}

		if (targetList.size() == 0)
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
		// TODO Auto-generated method stub
		return L2SkillTargetType.TARGET_AREA_UNDEAD;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAreaUndead());
	}
}
