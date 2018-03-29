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

package handlers.skillhandlers;

import l2server.gameserver.GeoData;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.network.serverpackets.FlyToLocation;
import l2server.gameserver.network.serverpackets.FlyToLocation.FlyType;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

/**
 * @author Pere
 */
public class SwitchPosition implements ISkillHandler
{
	//private static Logger log = Logger.getLogger(SummonFriend.class.getName());
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.SWITCH_POSITION};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for (L2Character target : (L2Character[]) targets)
		{
			if (activeChar == target)
			{
				continue;
			}

			if (Util.checkIfInRange(2000, activeChar, target, false) &&
					GeoData.getInstance().canSeeTarget(activeChar, target))
			{
				int x = activeChar.getX();
				int y = activeChar.getY();
				int z = activeChar.getZ();
				activeChar.setXYZ(target.getX(), target.getY(), target.getZ());

				activeChar.broadcastPacket(
						new FlyToLocation(activeChar, target.getX(), target.getY(), target.getZ(), FlyType.DUMMY));

				target.setXYZ(x, y, z);

				target.broadcastPacket(new FlyToLocation(target, x, y, z, FlyType.DUMMY));

				activeChar.broadcastPacket(new ValidateLocation(activeChar));
				target.broadcastPacket(new ValidateLocation(target));
				activeChar.revalidateZone(true);
				return;
			}
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
