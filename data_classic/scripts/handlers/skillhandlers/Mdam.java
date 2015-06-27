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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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
import l2server.gameserver.network.serverpackets.StatusUpdate.StatusUpdateDisplay;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.8.2.9 $ $Date: 2005/04/05 19:41:23 $
 */

public class Mdam implements ISkillHandler
{
	private static final Logger _logDamage = Logger.getLogger("damage");
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MDAM,
		L2SkillType.DEATHLINK
	};
	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		double ssMul = L2ItemInstance.CHARGED_NONE;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			ssMul = weaponInst.getChargedSpiritShot();
			weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			ssMul = activeSummon.getChargedSpiritShot();
			activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}
		
		for (L2Object obj: targets)
		{
			if (!(obj instanceof L2Character))
				continue;
			
			L2Character target = (L2Character)obj;
			
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance)target).isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead())
			{
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			final byte reflect = Formulas.calcSkillReflect(target, skill);
			
			int damage = (int) Formulas.calcMagicDam(activeChar, target, skill, shld, ssMul, mcrit);
			
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
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);

			final boolean skillIsEvaded = Formulas.calcMagicalSkillEvasion(activeChar, target, skill);
			if (!skillIsEvaded)
			{
				if (damage > 0)
				{
					// Manage attack or cast break of the target (calculating rate, sending message...)
					if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
					{
						target.breakAttack();
						target.breakCast();
					}
					
					int reflectedDamage = 0;
					
					if (!target.isInvul()) // Do not reflect if weapon is of type bow or target is invulnerable
					{
						// quick fix for no drop from raid if boss attack high-level char with damage reflection
						if (!target.isRaid()
								|| activeChar.getLevel() <= target.getLevel() + 8)
						{
							// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
							double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
							reflectPercent = activeChar.getStat().calcStat(Stats.REFLECT_VULN, reflectPercent, null, null);
							
							// Magics are ranged, let's half the reflect
							//reflectPercent *= 0.5;
							
							if (reflectPercent > 0)
							{
								reflectedDamage = (int)(reflectPercent / 100. * damage);
								if (reflectedDamage > target.getMaxHp()) // to prevent extreme damage when hitting a low lvl char...
									reflectedDamage = target.getMaxHp();
								
								//damage -= reflectedDamage;
							}
						}
					}
					
					if (reflectedDamage > 0)
					{
						activeChar.reduceCurrentHp(reflectedDamage, target, true, false, null);
						
						// Custom messages - nice but also more network load
						if (target instanceof L2PcInstance)
							((L2PcInstance)target).sendMessage("You reflected " + reflectedDamage + " damage.");
						else if (target instanceof L2Summon)
							((L2Summon)target).getOwner().sendMessage("Summon reflected " + reflectedDamage + " damage.");
		
						if (activeChar instanceof L2PcInstance)
							((L2PcInstance)activeChar).sendMessage("Target reflected to you " + reflectedDamage + " damage.");
						else if (activeChar instanceof L2Summon)
							((L2Summon)activeChar).getOwner().sendMessage("Target reflected to your summon " + reflectedDamage + " damage.");
					}
					
					// vengeance reflected damage
					// DS: because only skill using vengeanceMdam is Shield Deflect Magic
					// and for this skill no damage should pass to target, just hardcode it for now
					if ((reflect & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
						activeChar.reduceCurrentHp(damage, target, skill);
					else
					{
						activeChar.sendDamageMessage(target, damage, mcrit, false, false);
						target.reduceCurrentHp(damage, activeChar, skill);
					}
					
					if (damage > 1 && skill.hasEffects())
					{
						if ((reflect & Formulas.SKILL_REFLECT_EFFECTS) != 0) // reflect skill effects
						{
							//activeChar.stopSkillEffects(skill.getId());
							skill.getEffects(target, activeChar);
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
						else
						{
							// activate attacked effects, if any
							skill.getEffects(activeChar, target, new Env(shld, activeChar.getActiveWeaponInstance() != null ? activeChar.getActiveWeaponInstance().getChargedSoulShot() : L2ItemInstance.CHARGED_NONE));
						}
					}
					
					if (Rnd.get(100) < 20) // Absorb now acts as "trigger". Let's hardcode a 20% chance
					{
						// Absorb HP from the damage inflicted
						double absorbPercent = activeChar.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
						
						if (absorbPercent > 0 && !activeChar.isInvul())
						{
							int maxCanAbsorb = (int)(activeChar.getMaxHp() - activeChar.getCurrentHp());
							int absorbDamage = (int)(absorbPercent / 100. * damage);
							
							if (absorbDamage > maxCanAbsorb)
								absorbDamage = maxCanAbsorb; // Can't absorb more than max hp

							if (absorbDamage > 0)
							{
								activeChar.getStatus().setCurrentHp(activeChar.getCurrentHp() + absorbDamage, true, null, StatusUpdateDisplay.NORMAL);
								activeChar.sendMessage("You absorbed " + absorbDamage + " HP from " + target.getName() + ".");
							}
						}
					}
					
					// Logging damage
					if (Config.LOG_GAME_DAMAGE
							&& activeChar instanceof L2Playable
							&& damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
					{
						LogRecord record = new LogRecord(Level.INFO, "");
						record.setParameters(new Object[]{activeChar, " did damage ", damage, skill, " to ", target});
						record.setLoggerName("mdam");
						_logDamage.log(record);
					}
				}
			}
			else
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
		
		if (skill.isSuicideAttack())
			activeChar.doDie(activeChar);
	}
	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
