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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.skills.L2SkillType;

import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/04 19:08:01 $
 */

public class Charge implements ISkillHandler
{
    static Logger _log = Logger.getLogger(Charge.class.getName());

    /* (non-Javadoc)
     * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.L2PcInstance, l2server.gameserver.model.L2ItemInstance)
     */
    private static final L2SkillType[] SKILL_IDS = {/*L2SkillType.CHARGE*/};

    @Override
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {

        for (L2Object target : targets)
        {
            if (!(target instanceof L2PcInstance))
            {
                continue;
            }
            skill.getEffects(activeChar, (L2PcInstance) target);
        }

        // self Effect :]
        if (skill.hasSelfEffects())
        {
            final L2Abnormal effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
            {
                //Replace old effect with new one.
                effect.exit();
            }
            skill.getEffectsSelf(activeChar);
        }
    }

    @Override
    public L2SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
