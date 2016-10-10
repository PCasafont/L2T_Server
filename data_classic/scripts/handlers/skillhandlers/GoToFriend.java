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

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;

/**
 * @author Pere
 */
public class GoToFriend implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(SummonFriend.class.getName());
    private static final L2SkillType[] SKILL_IDS = {L2SkillType.GO_TO_FRIEND};

    /**
     * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
     */
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        for (L2Character target : (L2Character[]) targets)
        {
            if (activeChar == target)
            {
                continue;
            }

            if (Util.checkIfInRange(1000, activeChar, target, false))
            {
                activeChar.teleToLocation(target.getX(), target.getY(), target.getZ(), true);
                return;
            }
        }
    }

    /**
     * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
     */
    public L2SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
