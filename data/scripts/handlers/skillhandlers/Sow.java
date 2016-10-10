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

import java.util.logging.Logger;

import l2server.Config;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2MonsterInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.PlaySound;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.Rnd;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler
{
    private static Logger _log = Logger.getLogger(Sow.class.getName());

    private static final L2SkillType[] SKILL_IDS = {L2SkillType.SOW};

    /**
     * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
     */
    @Override
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (!(activeChar instanceof L2PcInstance))
        {
            return;
        }

        final L2Object[] targetList = skill.getTargetList(activeChar);
        if (targetList == null || targetList.length == 0)
        {
            return;
        }

        if (Config.DEBUG)
        {
            _log.info("Casting sow");
        }

        L2MonsterInstance target;

        for (L2Object tgt : targetList)
        {
            if (!(tgt instanceof L2MonsterInstance))
            {
                continue;
            }

            target = (L2MonsterInstance) tgt;
            if (target.isDead() || target.isSeeded() || target.getSeederId() != activeChar.getObjectId())
            {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                continue;
            }

            final int seedId = target.getSeedType();
            if (seedId == 0)
            {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                continue;
            }

            //Consuming used seed
            if (!activeChar.destroyItemByItemId("Consume", seedId, 1, target, false))
            {
                activeChar.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }

            SystemMessage sm;
            if (calcSuccess(activeChar, target, seedId))
            {
                activeChar.sendPacket(new PlaySound("Itemsound.quest_itemget"));
                target.setSeeded((L2PcInstance) activeChar);
                sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
            }
            else
            {
                sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
            }

            if (activeChar.getParty() == null)
            {
                activeChar.sendPacket(sm);
            }
            else
            {
                activeChar.getParty().broadcastToPartyMembers(sm);
            }

            //TODO: Mob should not aggro on player, this way doesn't work really nice
            target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
    }

    private boolean calcSuccess(L2Character activeChar, L2Character target, int seedId)
    {
        // TODO: check all the chances
        int basicSuccess = L2Manor.getInstance().isAlternative(seedId) ? 20 : 90;
        final int minlevelSeed = L2Manor.getInstance().getSeedMinLevel(seedId);
        final int maxlevelSeed = L2Manor.getInstance().getSeedMaxLevel(seedId);
        final int levelPlayer = activeChar.getLevel(); // Attacker Level
        final int levelTarget = target.getLevel(); // target Level

        // seed level
        if (levelTarget < minlevelSeed)
        {
            basicSuccess -= 5 * (minlevelSeed - levelTarget);
        }
        if (levelTarget > maxlevelSeed)
        {
            basicSuccess -= 5 * (levelTarget - maxlevelSeed);
        }

        // 5% decrease in chance if player level
        // is more than +/- 5 levels to _target's_ level
        int diff = levelPlayer - levelTarget;
        if (diff < 0)
        {
            diff = -diff;
        }
        if (diff > 5)
        {
            basicSuccess -= 5 * (diff - 5);
        }

        //chance can't be less than 1%
        if (basicSuccess < 1)
        {
            basicSuccess = 1;
        }

        return Rnd.nextInt(99) < basicSuccess;
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
