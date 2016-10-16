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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetAlly implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (activeChar instanceof L2PcInstance)
		{
			int radius = skill.getSkillRadius();
			L2PcInstance player = (L2PcInstance) activeChar;
			L2Clan clan = player.getClan();

			if (player.isInOlympiadMode())
			{
				return new L2Character[]{player};
			}

			if (!onlyFirst)
			{
				targetList.add(player);
			}
			else
			{
				return new L2Character[]{player};
			}

			if (player.getPet() != null)
			{
				if (!player.getPet().isDead())
				{
					targetList.add(player.getPet());
				}
			}

			if (clan != null)
			{
				// Get all visible objects in a spherical area near the L2Character
				// Get Clan Members
				Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
				//synchronized (activeChar.getKnownList().getKnownObjects())
				{
					for (L2Object newTarget : objs)
					{
						if (!(newTarget instanceof L2PcInstance))
						{
							continue;
						}
						if ((((L2PcInstance) newTarget).getAllyId() == 0 ||
								((L2PcInstance) newTarget).getAllyId() != player.getAllyId()) &&
								(((L2PcInstance) newTarget).getClan() == null ||
										((L2PcInstance) newTarget).getClanId() != player.getClanId()))
						{
							continue;
						}
						if (player.isInDuel() && (player.getDuelId() != ((L2PcInstance) newTarget).getDuelId() ||
								player.getParty() != null && !player.getParty().isInParty(newTarget)))
						{
							continue;
						}

						if (((L2PcInstance) newTarget).getPet() != null)
						{
							if (Util.checkIfInRange(radius, activeChar, ((L2PcInstance) newTarget).getPet(), true))
							{
								if (!((L2PcInstance) newTarget).getPet().isDead() &&
										player.checkPvpSkill(newTarget, skill) && onlyFirst == false)
								{
									targetList.add(((L2PcInstance) newTarget).getPet());
								}
							}
						}

						if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
						{
							continue;
						}

						// Don't add this target if this is a Pc->Pc pvp
						// casting and pvp condition not met
						if (!player.checkPvpSkill(newTarget, skill))
						{
							continue;
						}

						if (!onlyFirst)
						{
							targetList.add((L2Character) newTarget);
						}
						else
						{
							return new L2Character[]{(L2Character) newTarget};
						}
					}
				}
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
		return L2SkillTargetType.TARGET_ALLY;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAlly());
	}
}
