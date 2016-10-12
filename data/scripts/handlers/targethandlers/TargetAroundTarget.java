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
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.L2Trap;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillBehaviorType;
import l2server.gameserver.templates.skills.L2SkillTargetDirection;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;

/**
 * Used by all skills that affects nearby players around the target.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetAroundTarget implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final ArrayList<L2Character> result = new ArrayList<L2Character>();
		final L2PcInstance src = activeChar.getActingPlayer();
		boolean isAttackingPlayer = false;

		if (activeChar == target || target == null || src != null &&
				(!isReachableTarget(activeChar, target, skill, false) ||
						!src.isAbleToCastOnTarget(target, skill, false)))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		}
		else
		{
			if (target instanceof L2Playable)
			{
				isAttackingPlayer = true;
			}

			result.add(target);
		}

		if (target instanceof L2Playable)
		{
			for (L2Character obj : target.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
			{
				if (!isReachableTarget(activeChar, obj, skill, true) ||
						!activeChar.isAbleToCastOnTarget(obj, skill, true))
				{
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj))
				{
					continue;
				}

				result.add(obj);
			}
		}
		else
		{
			for (L2Object obj : activeChar.getKnownList().getKnownObjects().values())
			{
				if (activeChar == obj || obj == target || !(obj instanceof L2Character) ||
						!isAttackingPlayer && obj instanceof L2Playable)
				{
					continue;
				}

				if (!Util.checkIfInRange(skill.getSkillRadius(), obj, target, true))
				{
					continue;
				}

				if (!isReachableTarget(target, (L2Character) obj, skill, true) ||
						!activeChar.isAbleToCastOnTarget(obj, skill, true))
				{
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj))
				{
					continue;
				}

				result.add((L2Character) obj);
			}
		}

		return result.toArray(new L2Character[result.size()]);
	}

	private final boolean isReachableTarget(final L2Character activeChar, final L2Character target, final L2Skill skill, final boolean isMassiveCheck)
	{
		if (target instanceof L2Trap)
		{
			return false;
		}

		final L2SkillTargetDirection td = skill.getTargetDirection();

		if (td == L2SkillTargetDirection.DEAD_MONSTER)
		{
			if (target instanceof L2MonsterInstance)
			{
				if (skill.getSkillBehavior() == L2SkillBehaviorType.ATTACK)
				{
					if (!isMassiveCheck && !target.isDead())
					{
						return false;
					}
				}
				else
				{
					if (!target.isDead())
					{
						return false;
					}
				}

				return true;
			}
		}
		else
		{
			if (!target.isDead())
			{
				if (td == L2SkillTargetDirection.UNDEAD)
				{
					if (target.isUndead())
					{
						return true;
					}
				}

				if (isMassiveCheck)
				{
					if (td == L2SkillTargetDirection.FRONT)
					{
						if (activeChar.isFacing(target, 180))
						{
							return true;
						}
						//else
						//	Broadcast.toGameMasters(target.getName() + " was unreachable");
					}
					else if (td == L2SkillTargetDirection.BEHIND)
					{
						if (!target.isFacing(activeChar, 140))
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
					else if (td == L2SkillTargetDirection.ALL_SUMMONS)
					{
						if (target instanceof L2Summon)
						{
							return true;
						}
					}
				}
				else
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
		return L2SkillTargetType.TARGET_AROUND_TARGET;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAroundTarget());
	}
}
