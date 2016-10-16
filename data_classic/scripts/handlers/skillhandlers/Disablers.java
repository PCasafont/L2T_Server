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
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.ai.L2AttackableAI;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.Rnd;

/**
 * This Handles Disabler skills
 *
 * @author _drunk_
 */
public class Disablers implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
			L2SkillType.AGGDAMAGE,
			L2SkillType.AGGREDUCE,
			L2SkillType.AGGREDUCE_CHAR,
			L2SkillType.AGGREMOVE,
			L2SkillType.FAKE_DEATH,
			L2SkillType.NEGATE,
			L2SkillType.CANCEL_DEBUFF,
			L2SkillType.ERASE,
			L2SkillType.BETRAY,
			L2SkillType.RESET
	};

	/**
	 * @see ISkillHandler#useSkill(L2Character, L2Skill, L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2SkillType type = skill.getSkillType();

		byte shld = 0;
		double ssMul = L2ItemInstance.CHARGED_NONE;
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
				weaponInst.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
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
				ssMul = ((L2Npc) activeChar).soulshotcharged ? L2ItemInstance.CHARGED_SOULSHOT :
						L2ItemInstance.CHARGED_NONE;
				((L2Npc) activeChar).soulshotcharged = false;
			}
			else
			{
				ssMul = ((L2Npc) activeChar).spiritshotcharged ? L2ItemInstance.CHARGED_SPIRITSHOT :
						L2ItemInstance.CHARGED_NONE;
				((L2Npc) activeChar).spiritshotcharged = false;
			}
		}

		for (L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}
			L2Character target = (L2Character) obj;
			if (target.isDead() ||
					target.isInvul(activeChar) && !skill.ignoreImmunity() && type != L2SkillType.NEGATE &&
							!target.isParalyzed()) // bypass if target is null, dead or invul (excluding invul from Petrification)
			{
				continue;
			}

			if (target != activeChar && target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar)
			{
				continue;
			}

			if (target.calcStat(Stats.DEBUFF_IMMUNITY, 0.0, activeChar, null) > 0.0)
			{
				target.stopEffectsOnDebuffBlock();
				continue;
			}

			shld = Formulas.calcShldUse(activeChar, target, skill);

			switch (type)
			{
				case BETRAY:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ssMul))
					{
						skill.getEffects(activeChar, target, new Env(shld, ssMul));
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
						sm.addCharName(target);
						sm.addSkillName(skill);
						activeChar.sendPacket(sm);
					}
					break;
				}
				case FAKE_DEATH:
				{
					// stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
					skill.getEffects(activeChar, target, new Env(shld, ssMul));
					break;
				}
				case AGGDAMAGE:
				{
					int aggDamage = (int) (500 * skill.getPower() / (target.getLevel() + 7));
					aggDamage = (int) activeChar.calcStat(Stats.AGGRESSION_PROF, aggDamage, target, skill);
					if (target instanceof L2Attackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, aggDamage);
					}
					// TODO [Nemesiss] should this have 100% chance?
					skill.getEffects(activeChar, target, new Env(shld, ssMul));
					break;
				}
				case AGGREDUCE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2Attackable)
					{
						skill.getEffects(activeChar, target, new Env(shld, ssMul));

						double aggdiff = ((L2Attackable) target).getHating(activeChar) -
								target.calcStat(Stats.AGGRESSION, ((L2Attackable) target).getHating(activeChar), target,
										skill);

						if (skill.getPower() > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) skill.getPower());
						}
						else if (aggdiff > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) aggdiff);
						}
					}
					// when fail, target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					break;
				}
				case AGGREDUCE_CHAR:
				{
					// these skills needs to be rechecked
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ssMul))
					{
						if (target instanceof L2Attackable)
						{
							L2Attackable targ = (L2Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null && targ.hasAI() && targ.getAI() instanceof L2AttackableAI)
							{
								((L2AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.clearAggroList();
								targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target, new Env(shld, ssMul));
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					}
					break;
				}
				case AGGREMOVE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2Attackable && !target.isRaid())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ssMul))
						{
							if (skill.getTargetType() == L2SkillTargetType.TARGET_UNDEAD)
							{
								if (target.isUndead())
								{
									((L2Attackable) target).reduceHate(null,
											((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
								}
							}
							else
							{
								((L2Attackable) target).reduceHate(null,
										((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
							}
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
								sm.addCharName(target);
								sm.addSkillName(skill);
								activeChar.sendPacket(sm);
							}
							target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
						}
					}
					else
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
					}
					break;
				}
				case ERASE:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ssMul)
							// doesn't affect siege golem or wild hog cannon
							&& !(target instanceof L2SiegeSummonInstance))
					{
						L2Summon summonPet = (L2Summon) target;
						L2PcInstance summonOwner = summonPet.getOwner();
						summonPet.unSummon(summonOwner);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
						summonOwner.sendPacket(sm);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case RESET:
				{
					L2Abnormal[] effects = target.getAllEffects();

					if (effects == null || effects.length == 0)
					{
						break;
					}

					for (L2Abnormal e : effects)
					{
						if (e == null || !e.getSkill().isOffensive())
						{
							continue;
						}
						else if (e.getType() == L2AbnormalType.SLEEP)
						{
							continue;
						}

						//LasTravel TEMP: Devil's Sway: Resets the duration of the target's paralysis, hold, silence, sleep, shock, fear, petrification, and disarm.
						if (skill.getId() == 11095)
						{
							String a = e.getType().name();

							if (a == null)
							{
								continue;
							}

							if (!(a.contains("PARAL") || a.contains("HOLD") || a.contains("SILENCE") ||
									a.contains("SLEEP") || a.contains("FEAR") || a.contains("STUN") ||
									a.contains("PETRI") || a.contains("DISARM")))
							{
								continue;
							}
						}

						e.exit();
						Env env = new Env();
						env.player = activeChar;
						env.target = target;
						env.skill = e.getSkill();
						L2Abnormal ef = e.getTemplate().getEffect(env);
						if (ef != null)
						{
							ef.scheduleEffect();
						}
					}

					target.broadcastAbnormalStatusUpdate();

					break;
				}
				case CANCEL_DEBUFF:
				{
					L2Abnormal[] effects = target.getAllEffects();

					if (effects == null || effects.length == 0)
					{
						break;
					}

					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
					for (L2Abnormal e : effects)
					{
						if (e == null || !e.getSkill().isDebuff() || !e.getSkill().canBeDispeled())
						{
							continue;
						}

						e.exit();

						if (count > -1)
						{
							count++;
							if (count >= skill.getMaxNegatedEffects())
							{
								break;
							}
						}
					}

					break;
				}
				case CANCEL_STATS: // same than CANCEL but
				{
					L2Character attacker = activeChar;
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_EFFECTS)
					{
						target = activeChar;
						attacker = target;
					}

					if (Formulas.calcSkillSuccess(attacker, target, skill, shld, ssMul))
					{
						L2Abnormal[] effects = target.getAllEffects();

						int max = skill.getMaxNegatedEffects();
						if (max == 0)
						{
							max = Integer.MAX_VALUE; //this is for RBcancells and stuff...
						}

						if (effects.length >= max)
						{
							effects = SortEffects(effects);
						}

						//for (int i = 0; i < effects.length;i++)
						//    activeChar.sendMessage(Integer.toString(effects[i].getSkill().getMagicLevel()));

						int count = 1;

						for (L2Abnormal a : effects)
						{
							// do not delete signet effects!
							switch (a.getType())
							{
								case SIGNET_GROUND:
								case SIGNET_EFFECT:
									continue;
								default:
							}

							switch (a.getSkill().getId())
							{
								case 4082:
								case 4215:
								case 4515:
								case 5182:
								case 110:
								case 111:
								case 1323:
								case 1325:
									continue;
							}

							switch (a.getSkill().getSkillType())
							{
								case BUFF:
								case HEAL_PERCENT:
								case COMBATPOINTHEAL:
									break;
								default:
									continue;
							}

							double rate = 1 - count / max;
							if (rate < 0.33)
							{
								rate = 0.33;
							}
							else if (rate > 0.95)
							{
								rate = 0.95;
							}
							if (Rnd.get(1000) < rate * 1000)
							{
								boolean exit = false;
								for (L2AbnormalType skillType : skill.getNegateStats())
								{
									if (skillType == a.getType())
									{
										exit = true;
										break;
									}
								}

								if (exit)
								{
									a.exit();
									if (count == max)
									{
										break;
									}

									count++;
								}
							}
						}
					}
					else
					{
						if (attacker instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
							sm.addCharName(target);
							sm.addSkillName(skill);
							attacker.sendPacket(sm);
						}
					}

					break;
				}
				case NEGATE:
				{
					if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_EFFECTS)
					{
						target = activeChar;
					}

					if (skill.getNegateId().length != 0)
					{
						for (int i = 0; i < skill.getNegateId().length; i++)
						{
							if (skill.getNegateId()[i] != 0)
							{
								target.stopSkillEffects(skill.getNegateId()[i]);
							}
						}
					}
					else if (skill.getNegateAbnormals() != null)
					{
						for (L2Abnormal effect : target.getAllEffects())
						{
							if (effect == null)
							{
								continue;
							}

							for (String negateAbnormalType : skill.getNegateAbnormals().keySet())
							{
								for (String stackType : effect.getStackType())
								{
									if (negateAbnormalType.equalsIgnoreCase(stackType) &&
											skill.getNegateAbnormals().get(negateAbnormalType) >= effect.getStackLvl())
									{
										effect.exit();
									}
								}
							}
						}
					}
					else
					// all others negate type skills
					{
						int removedBuffs = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
						for (L2AbnormalType skillType : skill.getNegateStats())
						{
							if (removedBuffs > skill.getMaxNegatedEffects())
							{
								break;
							}

							switch (skillType)
							{
								case BUFF:
									int lvlmodifier = 52 + skill.getMagicLevel() * 2;
									if (skill.getMagicLevel() == 12)
									{
										lvlmodifier = Config.MAX_LEVEL;
									}
									int landrate = 90;
									if (target.getLevel() - lvlmodifier > 0)
									{
										landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
									}

									landrate = (int) activeChar.calcStat(Stats.CANCEL_RES, landrate, target, null);

									if (Rnd.get(100) < landrate)
									{
										removedBuffs +=
												negateEffect(target, L2AbnormalType.BUFF, skill.getMaxNegatedEffects());
									}
									break;
								default:
									removedBuffs += negateEffect(target, skillType, skill.getMaxNegatedEffects());
									break;
							}//end switch
						}//end for
					}//end else

					if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ssMul))
					{
						skill.getEffects(activeChar, target, new Env(shld, ssMul));
					}
				}// end case
				default:
			}//end switch

			//Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}//end for

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
	} //end void

	/**
	 * @param target
	 * @param type
	 * @param maxRemoved
	 * @return
	 */
	private int negateEffect(L2Character target, L2AbnormalType type, int maxRemoved)
	{
		return negateEffect(target, type, 0, maxRemoved);
	}

	/**
	 * @param target
	 * @param type
	 * @param skillId
	 * @param maxRemoved
	 * @return
	 */
	private int negateEffect(L2Character target, L2AbnormalType type, int skillId, int maxRemoved)
	{
		L2Abnormal[] effects = target.getAllEffects();
		int count = maxRemoved <= 0 ? -2 : 0;
		for (L2Abnormal e : effects)
		{
			if (e.getType() == type)
			{
				if (skillId != 0)
				{
					if (skillId == e.getSkill().getId() && count < maxRemoved)
					{
						e.exit();
						if (count > -1)
						{
							count++;
						}
					}
				}
				else if (count < maxRemoved)
				{
					e.exit();
					if (count > -1)
					{
						count++;
					}
				}
			}
		}

		return maxRemoved <= 0 ? count + 2 : count;
	}

	private L2Abnormal[] SortEffects(L2Abnormal[] initial)
	{
		//this is just classic insert sort
		//If u can find better sort for max 20-30 units, rewrite this... :)
		int min, index = 0;
		L2Abnormal pom;
		for (int i = 0; i < initial.length; i++)
		{
			min = initial[i].getSkill().getMagicLevel();
			for (int j = i; j < initial.length; j++)
			{
				if (initial[j].getSkill().getMagicLevel() <= min)
				{
					min = initial[j].getSkill().getMagicLevel();
					index = j;
				}
			}
			pom = initial[i];
			initial[i] = initial[index];
			initial[index] = pom;
		}

		return initial;
	}

	/**
	 * @see ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
