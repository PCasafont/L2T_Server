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
import l2server.gameserver.model.L2CommandChannel;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Didl
 */
public class TargetFriendNotMe implements ISkillTargetTypeHandler
{

	/**
	 *
	 */
	public TargetFriendNotMe()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		int radius = skill.getSkillRadius();

		if (onlyFirst)
		{
			return new L2Character[]{activeChar};
		}

		L2PcInstance player = null;

		if (activeChar instanceof L2Summon)
		{
			player = ((L2Summon) activeChar).getOwner();
			targetList.add(player);
		}
		/* FIXME
        else if (activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
			if (activeChar.getPet() != null)
				targetList.add(activeChar.getPet());
		}*/

		L2Party playerPt = player.getParty();
		L2CommandChannel cmdChannel = playerPt == null ? null : playerPt.getCommandChannel();

		for (L2PcInstance tempChar : player.getKnownList().getKnownPlayersInRadius(radius))
		{
			if (tempChar == player || tempChar.isDead())
			{
				continue;
			}

			if (tempChar.getClan() != null && player.getClan() != null && player.getClan() == tempChar.getClan() ||
					playerPt != null && (playerPt.isInParty(tempChar) ||
							cmdChannel != null && tempChar.getParty() != null &&
									cmdChannel.isInChannel(tempChar.getParty())) ||
					tempChar.getAllyId() != 0 && tempChar.getAllyId() == player.getAllyId())
			{

				if (tempChar.getPet() != null)
				{
					if (Util.checkIfInRange(radius, activeChar, tempChar.getPet(), true))
					{
						if (!tempChar.getPet().isDead() && player.checkPvpSkill(tempChar, skill) && !onlyFirst)
						{
							targetList.add(tempChar.getPet());
						}
					}
				}

				if (!player.checkPvpSkill(tempChar, skill))
				{
					continue;
				}

				if (!onlyFirst)
				{
					targetList.add(tempChar);
				}
				else
				{
					return new L2Character[]{tempChar};
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
		return L2SkillTargetType.TARGET_FRIEND_NOTME;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetFriendNotMe());
	}
}
