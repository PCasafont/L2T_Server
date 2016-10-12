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
import l2server.gameserver.model.actor.instance.L2TrapInstance;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;

import java.util.ArrayList;

/**
 * Used by all skills that are used by Traps.
 *
 * @author ZaKaX.
 */
public class TrapTarget implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		L2TrapInstance aTrap = activeChar instanceof L2TrapInstance ? (L2TrapInstance) activeChar : null;

		if (aTrap == null)
		{
			return null;
		}

		final ArrayList<L2Character> result = new ArrayList<L2Character>();

		for (L2Character o : aTrap.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
		{
			if (o == aTrap.getOwner() || o.isDead())
			{
				continue;
			}

			if (!aTrap.getOwner().isAbleToCastOnTarget(o, skill, skill.isUseableWithoutTarget()))
			{
				continue;
			}
			else if (!GeoEngine.getInstance().canSeeTarget(aTrap, o))
			{
				continue;
			}

			if (skill.getTargetDirection() == L2SkillTargetDirection.SINGLE)
			{
				return new L2Character[]{o};
			}

			result.add(o);
		}

		return result.toArray(new L2Character[result.size()]);
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TRAP_TARGET;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TrapTarget());
	}
}
