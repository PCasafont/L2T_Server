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

import l2server.Config;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.Rnd;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Steuf
 */
public class Blow implements ISkillHandler
{
    private static final Logger _logDamage = Logger.getLogger("damage");

    private static final L2SkillType[] SKILL_IDS = {L2SkillType.BLOW};

    public final static byte FRONT = 50;
    public final static byte SIDE = 60;
    public final static byte BEHIND = 70;

    @Override
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar.isAlikeDead())
        {
            return;
        }

        for (L2Character target : (L2Character[]) targets)
        {
            if (target.isAlikeDead())
            {
                continue;
            }

            if (activeChar instanceof L2PcInstance)
            {
                final L2PcInstance player = (L2PcInstance) activeChar;

                if (player.getExpertiseWeaponPenalty() != 0)
                {
                    return;
                }
            }

            // Check firstly if target dodges skill
            boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(activeChar, target, skill);

            //If skill requires Crit or skill requires behind,
            //calculate chance based on DEX, Position and on self BUFF
            boolean success = Formulas.calcBlowSuccess(activeChar, target, skill);

            //Blood Stab Skill can be used anywhere but doesn't have effect if you are in front
            if (skill.getId() == 10508 && !activeChar.isBehindTarget())
            {
                return;
            }

            // Tenkai customization - Blow direction feedback for .stabs command
            if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isShowingStabs())
            {
                if (activeChar.isBehindTarget())
                {
                    activeChar.sendPacket(new ExShowScreenMessage(1, 0, 5, 0, 1, 0, 0, false, 400, 0, "Backstab!"));
                }
                else if (activeChar.isInFrontOfTarget())
                {
                    activeChar.sendPacket(new ExShowScreenMessage(1, 0, 5, 0, 1, 0, 0, false, 400, 0, "Facestab!"));
                }
                else
                {
                    activeChar.sendPacket(new ExShowScreenMessage(1, 0, 5, 0, 1, 0, 0, false, 400, 0, "Sidestab!"));
                }
            }

            if (!skillIsEvaded && success)
            {
                final byte reflect = Formulas.calcSkillReflect(target, skill);

                if (skill.hasEffects())
                {
                    if (reflect == Formulas.SKILL_REFLECT_EFFECTS)
                    {
                        //activeChar.stopSkillEffects(skill.getId());
                        skill.getEffects(target, activeChar);
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                        sm.addSkillName(skill);
                        activeChar.sendPacket(sm);
                    }
                    else
                    {
                        final byte shld = Formulas.calcShldUse(activeChar, target, skill);
                        //target.stopSkillEffects(skill.getId());
                        if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, L2ItemInstance.CHARGED_NONE))
                        {
                            skill.getEffects(activeChar, target, new Env(shld, L2ItemInstance.CHARGED_NONE));
                            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                            sm.addSkillName(skill);
                            target.sendPacket(sm);
                        }
                        else
                        {
                            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
                            sm.addCharName(target);
                            sm.addSkillName(skill);
                            activeChar.sendPacket(sm);
                        }
                    }
                }
                L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
                double soul = L2ItemInstance.CHARGED_NONE;
                if (weapon != null && (weapon.getItemType() == L2WeaponType.DAGGER ||
                        weapon.getItemType() == L2WeaponType.DUALDAGGER || weapon.getItemType() == L2WeaponType.RAPIER))
                {
                    soul = weapon.getChargedSoulShot();
                }
                byte shld = Formulas.calcShldUse(activeChar, target, skill);

                // Crit rate base crit rate for skill, modified with STR bonus
                boolean crit = false;
                if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar), target))
                {
                    crit = true;
                }
                double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, soul);

                if (activeChar instanceof L2PcInstance)
                {
                    ((L2PcInstance) activeChar).sendSysMessage("Dam = " + damage);
                }
                if (skill.getMaxSoulConsumeCount() > 0 && activeChar instanceof L2PcInstance)
                {
                    switch (((L2PcInstance) activeChar).getSouls())
                    {
                        case 0:
                            break;
                        case 1:
                            damage *= 1.10;
                            break;
                        case 2:
                            damage *= 1.12;
                            break;
                        case 3:
                            damage *= 1.15;
                            break;
                        case 4:
                            damage *= 1.18;
                            break;
                        default:
                            damage *= 1.20;
                            break;
                    }
                }
                if (crit)
                {
                    if (activeChar instanceof L2PcInstance)
                    {
                        ((L2PcInstance) activeChar).sendSysMessage("Crit.");
                    }
                    damage *= 2;
                    // Vicious Stance is special after C5, and only for BLOW skills
                    // Adds directly to damage
                    L2Abnormal vicious = activeChar.getFirstEffect(312);
                    if (vicious != null && damage > 1)
                    {
                        for (Func func : vicious.getStatFuncs())
                        {
                            Env env = new Env();
                            env.player = activeChar;
                            env.target = target;
                            env.skill = skill;
                            env.value = damage;
                            func.calc(env);
                            damage = (int) env.value;
                        }
                    }
                }

                if (activeChar instanceof L2PcInstance)
                {
                    ((L2PcInstance) activeChar).sendSysMessage("Dam = " + damage);
                }

                if (soul > L2ItemInstance.CHARGED_NONE)
                {
                    weapon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
                }

                if (Config.LOG_GAME_DAMAGE && activeChar instanceof L2Playable &&
                        damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
                {
                    LogRecord record = new LogRecord(Level.INFO, "");
                    record.setParameters(new Object[]{activeChar, " did damage ", (int) damage, skill, " to ", target});
                    record.setLoggerName("pdam");
                    _logDamage.log(record);
                }

                int reflectedDamage = 0;

                if (!target.isInvul(activeChar)) // Do not reflect if weapon is of type bow or target is invulnerable
                {
                    int dmgCap = (int) target.getStat().calcStat(Stats.DAMAGE_CAP, 0, null, null);
                    if (dmgCap > 0 && damage > dmgCap)
                    {
                        damage = dmgCap;
                    }

                    // quick fix for no drop from raid if boss attack high-level char with damage reflection
                    if (!target.isRaid() || activeChar.getLevel() <= target.getLevel() + 8)
                    {
                        // Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
                        double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
                        reflectPercent = activeChar.getStat().calcStat(Stats.REFLECT_VULN, reflectPercent, null, null);

                        if (reflectPercent > 0)
                        {
                            reflectedDamage = (int) (reflectPercent / 100. * Math.min(target.getCurrentHp(), damage));

                            // Half the reflected damage for bows
                            /*L2Weapon weaponItem = activeChar.getActiveWeaponItem();
                            if (weaponItem != null && (weaponItem.getItemType() == L2WeaponType.BOW
									 || weaponItem.getItemType() == L2WeaponType.CROSSBOW))
								reflectedDamage *= 0.5f;*/

                            if (reflectedDamage >
                                    target.getMaxHp()) // to prevent extreme damage when hitting a low lvl char...
                            {
                                reflectedDamage = target.getMaxHp();
                            }

                            boolean defLimitReflects = true;

                            if (target.getFirstEffect(10021) != null || target.getFirstEffect(10017) != null ||
                                    target.getSkillLevelHash(13524) != 0)
                            {
                                defLimitReflects = false;
                            }

                            if (defLimitReflects && reflectedDamage > target.getPDef(activeChar))
                            {
                                reflectedDamage = target.getPDef(activeChar);
                            }

                            int totalHealth = (int) (target.getCurrentHp() + target.getCurrentCp());

                            if (totalHealth - damage <= 0)
                            {
                                reflectedDamage = 0;
                            }

                            //damage -= reflectedDamage;
                        }
                    }
                }

                if (reflectedDamage > 0)
                {
                    activeChar.reduceCurrentHp(reflectedDamage, target, true, false, null);

                    // Custom messages - nice but also more network load
                    if (target instanceof L2PcInstance)
                    {
                        ((L2PcInstance) target).sendMessage("You reflected " + reflectedDamage + " damage.");
                    }
                    else if (target instanceof L2Summon)
                    {
                        ((L2Summon) target).getOwner().sendMessage("Summon reflected " + reflectedDamage + " damage.");
                    }

                    if (activeChar instanceof L2PcInstance)
                    {
                        ((L2PcInstance) activeChar)
                                .sendMessage("Target reflected to you " + reflectedDamage + " damage.");
                    }
                    else if (activeChar instanceof L2Summon)
                    {
                        ((L2Summon) activeChar).getOwner()
                                .sendMessage("Target reflected to your summon " + reflectedDamage + " damage.");
                    }
                }

                if (Rnd.get(100) <= 20) // Absorb now acts as "trigger". Let's hardcode a 20% chance
                {
                    // Absorb HP from the damage inflicted
                    double absorbPercent = activeChar.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

                    if (absorbPercent > 0 && !activeChar.isInvul(target))
                    {
                        int maxCanAbsorb = (int) (activeChar.getMaxHp() - activeChar.getCurrentHp());
                        int absorbDamage = (int) (absorbPercent / 100. * damage);

                        if (absorbDamage > maxCanAbsorb)
                        {
                            absorbDamage = maxCanAbsorb; // Can't absorb more than max hp
                        }

                        if (absorbDamage > 0)
                        {
                            activeChar.getStatus().setCurrentHp(activeChar.getCurrentHp() + absorbDamage, true, null,
                                    StatusUpdateDisplay.NORMAL);
                            activeChar
                                    .sendMessage("You absorbed " + absorbDamage + " HP from " + target.getName() + ".");
                        }
                    }
                }

                target.reduceCurrentHp(damage, activeChar, skill);

                // vengeance reflected damage
                if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
                {
                    if (target instanceof L2PcInstance)
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK);
                        sm.addCharName(activeChar);
                        target.sendPacket(sm);
                    }
                    if (activeChar instanceof L2PcInstance)
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
                        sm.addCharName(target);
                        activeChar.sendPacket(sm);
                    }
                    // Formula from Diego post, 700 from rpg tests
                    double vegdamage = 700 * target.getPAtk(activeChar) / activeChar.getPDef(target);

                    activeChar.reduceCurrentHp(vegdamage, target, skill);
                }

                // Manage attack or cast break of the target (calculating rate, sending message...)
                if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
                {
                    target.breakAttack();
                    target.breakCast();
                }

                if (activeChar instanceof L2PcInstance)
                {
                    L2PcInstance activePlayer = (L2PcInstance) activeChar;

                    activePlayer.sendDamageMessage(target, (int) damage, false, crit, false);
                }
            }

            // Sending system messages
            if (skillIsEvaded)
            {
                if (activeChar instanceof L2PcInstance)
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK);
                    sm.addString(target.getName());
                    ((L2PcInstance) activeChar).sendPacket(sm);
                }
                if (target instanceof L2PcInstance)
                {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK);
                    sm.addString(activeChar.getName());
                    ((L2PcInstance) target).sendPacket(sm);
                }
            }

            //Possibility of a lethal strike
            Formulas.calcLethalHit(activeChar, target, skill);

            //Self Effect
            if (skill.hasSelfEffects())
            {
                final L2Abnormal effect = activeChar.getFirstEffect(skill.getId());
                if (effect != null && effect.isSelfEffect())
                {
                    effect.exit();
                }
                skill.getEffectsSelf(activeChar);
            }
        }
    }

    @Override
    public L2SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
