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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * @author Gnacik
 */
public class GiveReco implements ISkillHandler
{
    private static final L2SkillType[] SKILL_IDS = {L2SkillType.GIVE_RECO};

    /**
     * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
     */
    @Override
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        for (L2Object obj : targets)
        {
            if (obj instanceof L2PcInstance)
            {
                L2PcInstance target = (L2PcInstance) obj;
                int power = (int) skill.getPower();
                int reco = target.getRecomHave();

                if (reco + power >= 255)
                {
                    power = 255 - reco;
                }

                if (power > 0)
                {
                    target.setRecomHave(reco + power);

                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
                    sm.addNumber(power);

                    target.sendPacket(sm);
                    target.sendPacket(new UserInfo(target));
                    target.sendPacket(new ExVoteSystemInfo(target));
                }
                else
                {
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOTHING_HAPPENED));
                }
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
