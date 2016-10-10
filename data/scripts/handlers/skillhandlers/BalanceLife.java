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
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2MobSummonInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.StatusUpdate;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class BalanceLife implements ISkillHandler
{
    private static final L2SkillType[] SKILL_IDS = {L2SkillType.BALANCE_LIFE};

    /**
     * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
     */
    @Override
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        // L2Character activeChar = activeChar;
        // check for other effects
        ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);

        if (handler != null)
        {
            handler.useSkill(activeChar, skill, targets);
        }

        L2PcInstance player = null;
        if (activeChar instanceof L2PcInstance)
        {
            player = (L2PcInstance) activeChar;
        }

        double fullHP = 0;
        double currentHPs = 0;

        for (L2Character target : (L2Character[]) targets)
        {
            // We should not heal if char is dead/
            if (target == null || target
                    .isDead() || target instanceof L2MobSummonInstance) // Tenkai custom - don't consider coke mobs
            {
                continue;
            }

            // Player holding a cursed weapon can't be healed and can't heal
            if (target != activeChar)
            {
                if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
                {
                    continue;
                }
                else if (player != null && player.isCursedWeaponEquipped())
                {
                    continue;
                }
            }

            fullHP += target.getMaxHp();
            currentHPs += target.getCurrentHp();
        }

        double percentHP = currentHPs / fullHP;

        for (L2Character target : (L2Character[]) targets)
        {
            if (target == null || target
                    .isDead() || target instanceof L2MobSummonInstance) // Tenkai custom - don't consider coke mobs
            {
                continue;
            }

            // Player holding a cursed weapon can't be healed and can't heal
            if (target != activeChar)
            {
                if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
                {
                    continue;
                }
                else if (player != null && player.isCursedWeaponEquipped())
                {
                    continue;
                }
            }

            double newHP = target.getMaxHp() * percentHP;

            target.setCurrentHp(newHP);

            StatusUpdate su = new StatusUpdate(target, player, StatusUpdateDisplay.NORMAL);
            su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
            target.sendPacket(su);
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
