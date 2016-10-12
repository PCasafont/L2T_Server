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
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author One
 */
public class TargetCorpseClan implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (activeChar instanceof L2Playable)
		{
			int radius = skill.getSkillRadius();

			L2PcInstance player = null;

			if (activeChar instanceof L2Summon)
			{
				player = ((L2Summon) activeChar).getOwner();
			}
			else
			{
				player = (L2PcInstance) activeChar;
			}

			if (player == null)
			{
				return null;
			}

			L2Clan clan = player.getClan();

			if (player.isInOlympiadMode())
			{
				return new L2Character[]{player};
			}

			if (clan != null)
			{
				// Get all visible objects in a spheric area near the L2Character
				// Get Clan Members
				for (L2ClanMember member : clan.getMembers())
				{
					L2PcInstance newTarget = member.getPlayerInstance();

					if (newTarget == null || newTarget == player)
					{
						continue;
					}

					if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() ||
							player.getParty() != null && !player.getParty().isInParty(newTarget)))
					{
						continue;
					}

					if (!newTarget.isDead())
					{
						continue;
					}

					if (skill.getSkillType() == L2SkillType.RESURRECT)
					{
						// check target is not in a active siege zone
						if (newTarget.isInsideZone(L2Character.ZONE_SIEGE))
						{
							continue;
						}
					}

					if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
					{
						continue;
					}

					// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
					if (!player.checkPvpSkill(newTarget, skill))
					{
						continue;
					}

					if (!onlyFirst)
					{
						targetList.add(newTarget);
					}
					else
					{
						return new L2Character[]{newTarget};
					}
				}
			}
		}
		else if (activeChar instanceof L2Npc)
		{
			// for buff purposes, returns one unbuffed friendly mob nearby or mob itself?
			L2Npc npc = (L2Npc) activeChar;
			Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
			//synchronized (activeChar.getKnownList().getKnownObjects())
			{
				for (L2Object newTarget : objs)
				{
					if (newTarget instanceof L2Npc && ((L2Npc) newTarget).getFactionId() == npc.getFactionId())
					{
						if (!Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true))
						{
							continue;
						}
						if (((L2Npc) newTarget).getFirstEffect(skill) != null)
						{
							targetList.add((L2Npc) newTarget);
							break;
						}
					}
				}
			}
			if (targetList.isEmpty())
			{
				targetList.add(activeChar);
			}
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		// TODO Auto-generated method stub
		return L2SkillTargetType.TARGET_CORPSE_CLAN;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetCorpseClan());
	}
}
