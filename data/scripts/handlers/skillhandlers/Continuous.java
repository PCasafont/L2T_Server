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

import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2SkillBehaviorType;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(Continuous.class.getName());

    private static final L2SkillType[] SKILL_IDS = {
            L2SkillType.BUFF,
            L2SkillType.DEBUFF,
            L2SkillType.CONT,
            L2SkillType.CONTINUOUS_DEBUFF,
            L2SkillType.UNDEAD_DEFENSE,
            L2SkillType.AGGDEBUFF,
            L2SkillType.FUSION
    };

    /**
     * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
     */
    @Override
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        boolean acted = true;

        L2PcInstance player = null;
        if (activeChar instanceof L2PcInstance)
        {
            player = (L2PcInstance) activeChar;
        }

        if (skill.getEffectId() != 0)
        {
            L2Skill sk = SkillTable.getInstance()
                    .getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());

            if (sk != null)
            {
                skill = sk;
            }
        }

        for (L2Object obj : targets)
        {
            if (!(obj instanceof L2Character))
            {
                continue;
            }

            L2Character target = (L2Character) obj;
            byte shld = 0;
            double ssMul = L2ItemInstance.CHARGED_NONE;

            L2Character attacker = activeChar;
            if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_EFFECTS)
            {
                target = activeChar;
                attacker = target;
            }

            // Player holding a cursed weapon can't be buffed and can't buff
            if (skill.getSkillType() == L2SkillType.BUFF && !(activeChar instanceof L2ClanHallManagerInstance))
            {
                if (target != attacker)
                {
                    if (target instanceof L2PcInstance)
                    {
                        L2PcInstance trg = (L2PcInstance) target;
                        if (trg.isCursedWeaponEquipped())
                        {
                            continue;
                        }
                        // Avoiding block checker players get buffed from outside
                        else if (trg.getBlockCheckerArena() != -1)
                        {
                            continue;
                        }
                    }
                    else if (player != null && player.isCursedWeaponEquipped())
                    {
                        continue;
                    }
                }
            }

            if (skill.isOffensive() || skill.isDebuff())
            {
                L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
                if (weaponInst != null)
                {
                    if (skill.isMagic())
                    {
                        ssMul = weaponInst.getChargedSpiritShot();
                        if (skill.getId() != 1020) // vitalize
                        {
                            weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                        }
                    }
                    else
                    {
                        ssMul = weaponInst.getChargedSoulShot();
                        if (skill.getId() != 1020) // vitalize
                        {
                            weaponInst.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
                        }
                    }
                }
                // If there is no weapon equipped, check for an active summon.
                else if (activeChar instanceof L2Summon)
                {
                    L2Summon activeSummon = (L2Summon) activeChar;
                    if (skill.isMagic())
                    {
                        ssMul = activeSummon.getChargedSpiritShot();
                        activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                    }
                    else
                    {
                        ssMul = activeSummon.getChargedSoulShot();
                        activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
                    }
                }
                else if (activeChar instanceof L2Npc)
                {
                    if (skill.isMagic())
                    {
                        ssMul = ((L2Npc) activeChar)._soulshotcharged ? L2ItemInstance.CHARGED_SOULSHOT :
                                L2ItemInstance.CHARGED_NONE;
                        ((L2Npc) activeChar)._soulshotcharged = false;
                    }
                    else
                    {
                        ssMul = ((L2Npc) activeChar)._spiritshotcharged ? L2ItemInstance.CHARGED_SPIRITSHOT :
                                L2ItemInstance.CHARGED_NONE;
                        ((L2Npc) activeChar)._spiritshotcharged = false;
                    }
                }

                shld = Formulas.calcShldUse(attacker, target, skill);
                acted = true;//Formulas.calcSkillSuccess(activeChar, target, skill, shld, 1.0);
            }

            if (acted)
            {
                if (skill.isToggle())
                {
                    L2Abnormal[] effects = target.getAllEffects();
                    if (effects != null)
                    {
                        for (L2Abnormal e : effects)
                        {
                            if (e != null)
                            {
                                if (e.getSkill().getId() == skill.getId())
                                {
                                    e.exit();
                                    return;
                                }
                            }
                        }
                    }
                }

                // if this is a debuff let the duel manager know about it
                // so the debuff can be removed after the duel
                // (player & target must be in the same duel)
                L2Abnormal[] effects = skill.getEffects(attacker, target, new Env(shld, ssMul));
                if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && (skill.isDebuff() || skill
                        .getSkillType() == L2SkillType.BUFF) && player != null && player
                        .getDuelId() == ((L2PcInstance) target).getDuelId())
                {
                    DuelManager dm = DuelManager.getInstance();
                    for (L2Abnormal buff : effects)
                    {
                        if (buff != null)
                        {
                            dm.onBuff((L2PcInstance) target, buff);
                        }
                    }
                }

                // Give the buff to our pets if possible
                if (target instanceof L2PcInstance && effects.length > 0 && (effects[0].canBeShared() || skill
                        .getTargetType() == L2SkillTargetType.TARGET_SELF) && !skill.isToggle() && skill
                        .canBeSharedWithSummon())
                {
                    for (L2SummonInstance summon : ((L2PcInstance) target).getSummons())
                    {
                        skill.getEffects(attacker, summon, new Env(shld, ssMul));
                    }
                }

                if (skill.getSkillType() == L2SkillType.AGGDEBUFF && effects.length > 0)
                {
                    if (target instanceof L2Attackable)
                    {
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, (int) skill.getPower());
                    }
                    else if (target instanceof L2Playable)
                    {
                        if (target.getTarget() == attacker)
                        {
                            target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
                        }
                        else
                        {
                            target.setTarget(attacker);
                        }
                    }
                }

                if (effects.length == 0 && skill.getSkillBehavior() != L2SkillBehaviorType.FRIENDLY)
                {
                    acted = false;
                }
            }
            else
            {
                attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
            }

            if (skill.getSkillType() == L2SkillType.CONTINUOUS_DEBUFF && !acted)
            {
                activeChar.abortCast();
            }

            // Possibility of a lethal strike
            Formulas.calcLethalHit(attacker, target, skill);
        }

        // self Effect
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

    /**
     * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
     */
    @Override
    public L2SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
