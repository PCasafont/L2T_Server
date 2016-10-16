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
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;

/**
 * Used by all skills that affects nearby players around the caster.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetAroundCaster implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final ArrayList<L2Character> result = new ArrayList<L2Character>();

		L2Character actualCaster = activeChar;
		if (activeChar instanceof L2NpcInstance && ((L2NpcInstance) activeChar).getOwner() != null)
		{
			actualCaster = ((L2NpcInstance) activeChar).getOwner();
		}
		for (L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
		{
			if (skill.getSkillSafeRadius() != 0)
			{
				int safeRadius = skill.getSkillSafeRadius();
				int distance =
						(int) Util.calculateDistance(activeChar.getX(), activeChar.getY(), obj.getX(), obj.getY());
				if (distance < safeRadius)
				{
					continue;
				}
			}

			if (activeChar == obj)
			{
				continue;
			}
			else if (!isReachableTarget(activeChar, obj, skill.getTargetDirection()) ||
					!actualCaster.isAbleToCastOnTarget(obj, skill, true))
			{
				continue;
			}

			if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj))
			{
				continue;
			}

			if (actualCaster instanceof L2PcInstance && !((L2PcInstance) actualCaster).checkPvpSkill(obj, skill))
			{
				continue;
			}

			if (result.size() > 20 && skill.getSkillType() != L2SkillType.AGGDAMAGE)
			{
				break;
			}

			result.add(obj);
		}

		return result.toArray(new L2Character[result.size()]);
	}

	private final boolean isReachableTarget(final L2Character activeChar, final L2Character target, L2SkillTargetDirection td)
	{
		if (activeChar instanceof L2NpcInstance)
		{
			final L2NpcInstance aNpc = (L2NpcInstance) activeChar;

			if (target instanceof L2Playable)
			{
				final L2PcInstance tPlayer = target.getActingPlayer();

				if (tPlayer == aNpc.getOwner())
				{
					return false;
				}
			}
		}

		if (!target.isDead())
		{
			if (td == L2SkillTargetDirection.UNDEAD)
			{
				if (target.isUndead())
				{
					return true;
				}
			}
			else if (td == L2SkillTargetDirection.FRONT)
			{
				if (target.isInFrontOf(activeChar))
				{
					return true;
				}
			}
			else if (td == L2SkillTargetDirection.BEHIND)
			{
				if (target.isBehind(activeChar))
				{
					return true;
				}
			}
			else if (td == L2SkillTargetDirection.DEFAULT || td == L2SkillTargetDirection.AROUND)
			{
				return true;
			}
			else if (td == L2SkillTargetDirection.PLAYER)
			{
				if (target instanceof L2Playable)
				{
					return true;
				}
			}
		}
		else
		{
			if (td == L2SkillTargetDirection.DEAD_MONSTER)
			{
				if (target instanceof L2MonsterInstance && target.isDead())
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_AROUND_CASTER;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAroundCaster());
	}
}
