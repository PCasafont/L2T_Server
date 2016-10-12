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
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nBd
 */
public class TargetParty implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (onlyFirst)
		{
			return new L2Character[]{activeChar};
		}

		targetList.add(activeChar);

		L2PcInstance player = null;

		if (activeChar instanceof L2Summon)
		{
			player = ((L2Summon) activeChar).getOwner();
			targetList.add(player);
		}
		else if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
			for (L2Summon summon : ((L2PcInstance) activeChar).getSummons())
			{
				if (!summon.isDead())
				{
					targetList.add(summon);
				}
			}
		}

		if (activeChar.getParty() != null)
		{
			// Get all visible objects in a spherical area near the L2Character
			// Get a list of Party Members
			List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();

			for (L2PcInstance partyMember : partyList)
			{
				if (partyMember == null)
				{
					continue;
				}
				if (partyMember == player)
				{
					continue;
				}

				if (!partyMember.isDead() && Util.checkIfInRange(skill.getSkillRadius(), activeChar, partyMember, true))
				{
					targetList.add(partyMember);

					if (partyMember.getPet() != null && !partyMember.getPet().isDead())
					{
						targetList.add(partyMember.getPet());
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
		return L2SkillTargetType.TARGET_PARTY;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetParty());
	}
}
