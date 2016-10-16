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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetMultiface implements ISkillTargetTypeHandler
{
	/**
	 */
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<L2Character>();

		if (!(target instanceof L2Attackable) && !(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		if (onlyFirst == false)
		{
			targetList.add(target);
		}
		else
		{
			return new L2Character[]{target};
		}

		int radius = skill.getSkillRadius();

		Collection<L2Object> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (L2Object obj : objs)
			{
				if (!Util.checkIfInRange(radius, activeChar, obj, true))
				{
					continue;
				}

				if (obj instanceof L2Attackable && obj != target)
				{
					targetList.add((L2Character) obj);
				}

				if (targetList.size() == 0)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
					return null;
				}
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
		//TODO multiface targets all around right now.  need it to just get targets the character is facing.
	}

	/**
	 */
	@Override
	public Enum<L2SkillTargetType> getTargetType()
	{
		return L2SkillTargetType.TARGET_MULTIFACE;
	}

	public static void main(String[] args)
	{
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetMultiface());
	}
}
