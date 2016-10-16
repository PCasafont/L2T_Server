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
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Used by all skills that affects nearby players around the target.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetChainHeal implements ISkillTargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		final L2PcInstance aPlayer = activeChar.getActingPlayer();
		final ArrayList<L2Character> result = new ArrayList<L2Character>();

		// Check for null target or any other invalid target
		if (target == null || target.isDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}
		//add self only when targeted self
		if (target == aPlayer)
		{
			result.add(aPlayer);
		}
		else
		{
			if (isReachableTarget(aPlayer, target))
			{
				result.add(target);
			}
			else
			{
				return null;
			}
		}

		//get objects in radius of target
		for (L2Character o : target.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
		{
			if (!GeoEngine.getInstance().canSeeTarget(target, o))
			{
				continue;
			}

			final L2PcInstance kTarget = o.getActingPlayer();
			if (kTarget != null)
			{
				//dont add self when not targeted self (worked this way on retail))
				if (kTarget == aPlayer)
				{
					continue;
				}
				final L2Summon kPet = kTarget.getPet();
				if (kPet != null)
				{
					if (!isReachableTarget(aPlayer, kPet))
					{
						continue;
					}

					result.add(kPet);
				}
			}
			if (isReachableTarget(aPlayer, o))
			{
				result.add(o);
			}
		}
		if (result.size() <= 11) //target + 10 allies
		{
			return result.toArray(new L2Character[result.size()]);
		}
		else
		{
			SortedMap<Double, L2Character> map = new TreeMap<Double, L2Character>();
			for (L2Character obj : result)
			{
				double percentlost = obj.getCurrentHp() / obj.getMaxHp();
				map.put(percentlost, obj);
			}
			result.clear();
			Iterator<Double> iterator = map.keySet().iterator();
			int i = 0;
			while (iterator.hasNext() && i < 11)
			{
				Object key = iterator.next();
				i++;
				result.add(map.get(key));
			}
			return result.toArray(new L2Character[result.size()]);
		}
	}

	private final boolean isReachableTarget(final L2Character activeChar, final L2Character target)
	{
		if (target.isDead())
		{
			return false;
		}

		if (target instanceof L2Playable)
		{
			final L2PcInstance pTarget = target.getActingPlayer();

			if (pTarget.isPlayingEvent())
			{
				if (!((L2PcInstance) activeChar).isPlayingEvent())
				{
					return false;
				}

				if (pTarget.getEvent().getConfig().isAllVsAll())
				{
					return false;
				}

				if (pTarget.getEvent().getParticipantTeamId(pTarget.getObjectId()) !=
						pTarget.getEvent().getParticipantTeamId(activeChar.getObjectId()))
				{
					return false;
				}
			}

			if (activeChar instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) activeChar;
				if (player.getDuelId() != 0)
				{
					if (((L2PcInstance) activeChar).getDuelId() != pTarget.getDuelId())
					{
						return false;
					}
				}

				if (((L2PcInstance) activeChar).isInSameClanWar(pTarget) ||
						((L2PcInstance) activeChar).isInOlympiadMode())
				{
					return false;
				}

				if (player.isInSameParty(pTarget) || player.isInSameChannel(pTarget) || player.isInSameClan(pTarget) ||
						player.isInSameAlly(pTarget))
				{
					return true;
				}
			}
			else if (pTarget.getDuelId() != 0)
			{
				return false;
			}
			if (pTarget.isAvailableForCombat() || pTarget.isInsidePvpZone())
			{
				return false;
			}
			if (target.isInsideZone(L2Character.ZONE_TOWN))
			{
				return true;
			}
		}
		else if (target instanceof L2NpcInstance)
		{
			final L2NpcInstance npc = (L2NpcInstance) target;
			if (!npc.isInsideZone(L2Character.ZONE_TOWN))
			{
				return false;
			}
		}
		else
		{
			return false;
		}

		return true;
	}

	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_CHAIN_HEAL;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetChainHeal());
	}
}
