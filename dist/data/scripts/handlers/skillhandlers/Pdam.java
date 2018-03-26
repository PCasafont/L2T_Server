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
import l2server.gameserver.datatables.SkillTable;
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
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.item.L2WeaponType;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.gameserver.util.Util;
import l2server.util.Rnd;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{
	private static final Logger _log = Logger.getLogger(Pdam.class.getName());
	private static final Logger _logDamage = Logger.getLogger("damage");

	private static final L2SkillType[] SKILL_IDS = {L2SkillType.PDAM, L2SkillType.FATAL};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}

		double damage = 0;

		if (Config.DEBUG)
		{
			_log.fine("Begin Skill processing in Pdam.java " + skill.getSkillType());
		}

		L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
		double soul = L2ItemInstance.CHARGED_NONE;
		if (weapon != null && weapon.getItemType() != L2WeaponType.DAGGER)
		{
			soul = weapon.getChargedSoulShot();
		}

		// If there is no weapon equipped, check for an active summon.
		if (weapon == null && activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			soul = activeSummon.getChargedSoulShot();
		}

		boolean damagesMultipliesPerTarget = skill.getId() == 30518;
		double damageMultiplier = 1.0 + targets.length * 0.1;
		if (damageMultiplier > 2)
		{
			damageMultiplier = 2;
		}

		double pveAoeNerf = 1.0;
		for (L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}

			L2Character target = (L2Character) obj;

			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance &&
					((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if (target.isDead())
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

			final boolean dual = activeChar.isUsingDualWeapon();
			final byte shld = Formulas.calcShldUse(activeChar, target, skill);
			// PDAM critical chance not affected by buffs, only by STR. Only some skills are meant to crit.
			boolean crit = false;
			if (skill.getBaseCritRate() > 0)
			{
				double critRate = skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar);

				critRate *= Math.sqrt(BaseStats.STR.calcBonus(activeChar)) / BaseStats.STR.calcBonus(activeChar);

				critRate = activeChar.calcStat(Stats.PCRITICAL_RATE, critRate, target, skill);
				//if (target instanceof L2PcInstance)
				//	System.out.println(skill.getBaseCritRate() + " " + BaseStats.STR.calcBonus(activeChar) + " " + activeChar.calcStat(Stats.PCRITICAL_RATE, 1, target, skill) + " " + activeChar.calcStat(Stats.PSKILL_CRIT_PER_DEX, 0, null, null) * (BaseStats.DEX.calcBonus(activeChar) - 1) * critRate + " " + critRate);
				if (skill.getMaxChargeConsume() > 0)
				{
					critRate = activeChar.calcStat(Stats.MOMENTUM_CRIT_RATE, critRate, target, skill);
				}

				// Guessed formula
				critRate += activeChar.calcStat(Stats.PSKILL_CRIT_PER_DEX, 0, null, null) *
						(BaseStats.DEX.calcBonus(activeChar) - 1) * critRate;

				crit = Formulas.calcCrit(critRate, target);
			}
			if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
			{
				damage = 0;
			}
			else
			{
				damage = (int) Formulas.calcPhysSkillDam(activeChar, target, skill, shld, crit, dual, soul);
			}


				damage *= pveAoeNerf;
				pveAoeNerf *= 0.5;


			if (damagesMultipliesPerTarget)
			{
				@SuppressWarnings("unused") int originalDamages = (int) damage;
				damage *= damageMultiplier;

				//Broadcast.toGameMasters("Damages multiplied by " + damageMultiplier + " on target " + target + ". Original Damages = " + originalDamages);
			}

			if (damage != 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getAppearance().getInvisible())
				{
					L2Abnormal eInvisible = target.getFirstEffect(L2AbnormalType.HIDE);

					if (eInvisible != null)
					{
						eInvisible.exit();
					}
				}
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

			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(activeChar, target, skill);
			final byte reflect = Formulas.calcSkillReflect(target, skill);

			if (!skillIsEvaded)
			{
				if (skill.hasEffects())
				{
					L2Abnormal[] effects;
					if ((reflect & Formulas.SKILL_REFLECT_EFFECTS) != 0)
					{
						//activeChar.stopSkillEffects(skill.getId());
						effects = skill.getEffects(target, activeChar);
						if (effects != null && effects.length > 0)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					else
					{
						// activate attacked effects, if any
						//target.stopSkillEffects(skill.getId());
						effects = skill.getEffects(activeChar, target, new Env(shld, L2ItemInstance.CHARGED_NONE));
						if (effects != null && effects.length > 0)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(skill);
							target.sendPacket(sm);
						}
					}
				}

				int dmgCap = (int) target.getStat().calcStat(Stats.DAMAGE_CAP, 0, null, null);
				if (dmgCap > 0 && damage > dmgCap)
				{
					damage = dmgCap;
				}

				if (damage > 0)
				{
					int reflectedDamage = 0;

					if (!target
							.isInvul(activeChar)) // Do not reflect if weapon is of type bow or target is invulnerable
					{
						// quick fix for no drop from raid if boss attack high-level char with damage reflection
						if (!target.isRaid() || activeChar.getLevel() <= target.getLevel() + 8)
						{
							// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
							double reflectPercent =
									target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
							reflectPercent =
									activeChar.getStat().calcStat(Stats.REFLECT_VULN, reflectPercent, null, null);

							if (reflectPercent > 0)
							{
								reflectedDamage =
										(int) (reflectPercent / 100. * Math.min(target.getCurrentHp(), damage));

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

					if (target.getFirstEffect(L2AbnormalType.SPALLATION) != null &&
							!Util.checkIfInRange(130, activeChar, target, false))
					{
						activeChar.sendMessage("Your attack has been blocked.");

						target.sendMessage("You blocked an attack.");
						continue;
					}

					activeChar.sendDamageMessage(target, (int) damage, false, crit, false);

					if (Config.LOG_GAME_DAMAGE && activeChar instanceof L2Playable &&
							damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
					{
						LogRecord record = new LogRecord(Level.INFO, "");
						record.setParameters(new Object[]{activeChar, " did damage ", damage, skill, " to ", target});
						record.setLoggerName("pdam");
						_logDamage.log(record);
					}

					// Possibility of a lethal strike
					Formulas.calcLethalHit(activeChar, target, skill);

					target.reduceCurrentHp(damage, activeChar, skill);

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
							((L2Summon) target).getOwner()
									.sendMessage("Summon reflected " + reflectedDamage + " damage.");
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

					if (Rnd.get(100) < 20) // Absorb now acts as "trigger". Let's hardcode a 20% chance
					{
						// Absorb HP from the damage inflicted
						double absorbPercent =
								activeChar.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

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
								activeChar.getStatus()
										.setCurrentHp(activeChar.getCurrentHp() + absorbDamage, true, null,
												StatusUpdateDisplay.NORMAL);
								activeChar.sendMessage(
										"You absorbed " + absorbDamage + " HP from " + target.getName() + ".");
							}
						}
					}

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
							SystemMessage sm =
									SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK);
							sm.addCharName(target);
							activeChar.sendPacket(sm);
						}
						if (!Config.isServer(Config.TENKAI))
						{
							// Formula from Diego post, 700 from rpg tests
							double vegdamage = 700 * target.getPAtk(activeChar) / activeChar.getPDef(target);

							activeChar.reduceCurrentHp(vegdamage, target, skill);
						}
						else
						{
							activeChar.reduceCurrentHp(damage, target, skill);
						}
					}
				}
				else
				// No damage
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
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

				// Possibility of a lethal strike despite skill is evaded
				Formulas.calcLethalHit(activeChar, target, skill);
			}

			if (activeChar instanceof L2PcInstance)
			{
				int soulMasteryLevel = activeChar.getSkillLevelHash(467);
				if (soulMasteryLevel > 0)
				{
					L2Skill soulmastery = SkillTable.getInstance().getInfo(467, soulMasteryLevel);
					if (soulmastery != null)
					{
						if (((L2PcInstance) activeChar).getSouls() < soulmastery.getNumSouls())
						{
							int count = 0;

							if (((L2PcInstance) activeChar).getSouls() + skill.getNumSouls() <=
									soulmastery.getNumSouls())
							{
								count = skill.getNumSouls();
							}
							else
							{
								count = soulmastery.getNumSouls() - ((L2PcInstance) activeChar).getSouls();
							}
							((L2PcInstance) activeChar).increaseSouls(count);
						}
						else
						{
							SystemMessage sm =
									SystemMessage.getSystemMessage(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
							((L2PcInstance) activeChar).sendPacket(sm);
						}
					}
				}
			}
		}

		//self Effect :]
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

		if (weapon != null)
		{
			weapon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
		}

		if (skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
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
