/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.InstanceType;
import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.ai.CtrlEvent;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.instancemanager.DuelManager;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.skills.SkillDrain;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.templates.skills.SkillBehaviorType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class CubicInstance {
	private static Logger log = LoggerFactory.getLogger(CubicInstance.class.getName());

	// Type of Cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	public static final int KNIGHT_CUBIC = 15;
	public static final int AVENGING_CUBIC = 16;
	public static final int FAIRY_OF_LIFE = 17;
	public static final int BUFF_CUBIC = 18;
	public static final int MIND_CUBIC = 19;
	public static final int PHANTOM_CUBIC = 20;
	public static final int HEX_CUBIC = 21;
	public static final int GUARDIAN_CUBIC = 22;
	
	// Max range of cubic skills
	// TODO: Check/fix the max range
	public static final int MAX_MAGIC_RANGE = 900;
	
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	public static final int SKILL_BUFF_CUBIC_HEAL = 10082;
	public static final int SKILL_MIND_CUBIC_HEAL = 10084;
	
	protected Player owner;
	protected Creature target;
	
	protected int id;
	protected int matk;
	protected int activationtime;
	protected int activationchance;
	protected int maxcount;
	protected int currentcount;
	protected boolean active;
	private boolean givenByOther;
	
	protected List<Skill> skills = new ArrayList<>();
	
	private Future<?> disappearTask;
	private Future<?> actionTask;
	
	public CubicInstance(Player owner,
	                     int id,
	                     int level,
	                     int mAtk,
	                     int activationtime,
	                     int activationchance,
	                     int maxcount,
	                     int totallifetime,
	                     boolean givenByOther) {
		this.owner = owner;
		this.id = id;
		this.matk = mAtk;
		this.activationtime = activationtime * 1000;
		this.activationchance = activationchance;
		this.maxcount = maxcount;
		currentcount = 0;
		active = false;
		this.givenByOther = givenByOther;
		
		switch (id) {
			case STORM_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			case LIFE_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4053, level));
				skills.add(SkillTable.getInstance().getInfo(4054, level));
				skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			case BINDING_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			case AQUA_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			case SPARK_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			case ATTRACT_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(5115, level));
				skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				// skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				skills.add(SkillTable.getInstance().getInfo(4051, 7)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(4166, 9)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				skills.add(SkillTable.getInstance().getInfo(4052, 6)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_EVATEMPLAR:
				// skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				skills.add(SkillTable.getInstance().getInfo(4053, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(5115, 4)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case KNIGHT_CUBIC:
				//skills.add(SkillTable.getInstance().getInfo(10055, 1));
				skills.add(SkillTable.getInstance().getInfo(10056, level));
				//skills.add(SkillTable.getInstance().getInfo(10057, 1));
				doAction();
				break;
			case AVENGING_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(11292, Math.min(level, 8)));
				skills.add(SkillTable.getInstance().getInfo(11293, Math.min(level, 8)));
				skills.add(SkillTable.getInstance().getInfo(11294, level));
				doAction();
				break;
			case FAIRY_OF_LIFE:
				skills.add(SkillTable.getInstance().getInfo(11807, level));
				doAction();
				break;
			case BUFF_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(10082, level));
				doAction();
				break;
			case MIND_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(10084, level));
				doAction();
				break;
			case PHANTOM_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(10085, level));
				doAction();
				break;
			case HEX_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(10086, level));
				doAction();
				break;
			case GUARDIAN_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(10093, level));
				doAction();
				break;
		}
		disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), totallifetime); // disappear
	}
	
	public synchronized void doAction() {
		if (active) {
			return;
		}
		
		active = true;
		
		switch (id) {
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
			case KNIGHT_CUBIC:
			case AVENGING_CUBIC:
			case FAIRY_OF_LIFE:
			case HEX_CUBIC:
			case PHANTOM_CUBIC:
			case GUARDIAN_CUBIC:
				actionTask =
						ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(activationchance), activationtime / 2, activationtime);
				break;
			
			case LIFE_CUBIC:
			case BUFF_CUBIC:
			case MIND_CUBIC:
				actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(), 0, activationtime);
				break;
		}
	}
	
	public int getId() {
		return id;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	public final int getMCriticalHit(Creature target, Skill skill) {
		// TODO: Temporary now mcrit for cubics is the baseMCritRate of its owner
		return owner.getTemplate().baseMCritRate;
	}
	
	public int getMAtk() {
		return matk;
	}
	
	public void stopAction() {
		target = null;
		
		if (actionTask != null) {
			actionTask.cancel(true);
			actionTask = null;
		}
		
		active = false;
	}
	
	public void cancelDisappear() {
		if (disappearTask != null) {
			disappearTask.cancel(true);
			disappearTask = null;
		}
	}
	
	/**
	 * this sets the enemy target for a cubic
	 */
	public void getCubicTarget() {
		try {
			target = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private class Action implements Runnable {
		@SuppressWarnings("unused")
		private int chance;
		
		Action(int chance) {
			this.chance = chance;
			// run task
		}
		
		@Override
		public void run() {
			try {
				boolean active = false;
				for (CubicInstance cubic : owner.getCubics().values()) {
					if (cubic == CubicInstance.this) {
						active = true;
					}
				}
				
				if (active) {
					active = active && !owner.isDead() && owner.isOnline();
				}
				
				if (!active) {
					stopAction();
					owner.delCubic(id);
					owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}
				
				boolean hasActiveSummon = false;
				boolean cubicShouldStopIfNoAction = true;
				
				switch (id) {
					case KNIGHT_CUBIC:
					case AVENGING_CUBIC:
					case HEX_CUBIC:
					case PHANTOM_CUBIC:
					case FAIRY_OF_LIFE:
					case GUARDIAN_CUBIC:
						cubicShouldStopIfNoAction = false;
						break;
				}
				
				for (SummonInstance summon : owner.getSummons()) {
					if (AttackStanceTaskManager.getInstance().getAttackStanceTask(summon)) {
						hasActiveSummon = true;
					}
				}
				
				if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(owner) && !hasActiveSummon && cubicShouldStopIfNoAction) {
					if (owner.getPet() != null) {
						if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(owner.getPet())) {
							stopAction();
							return;
						}
					} else {
						stopAction();
						return;
					}
				}
				// The cubic has already reached its limit and it will stay idle until its lifetime ends.
				if (maxcount > -1 && currentcount >= maxcount) {
					stopAction();
					return;
				}
				
				for (Skill sk : skills) {
					Skill skill = SkillTable.getInstance().getInfo(sk.getId(), sk.getLevel());
					//Broadcast.toGameMasters(skill.getName() + " has a reuse delay of " + skill.getReuseDelay());
					
					if (skill.getId() == SKILL_CUBIC_HEAL) {
						// friendly skill, so we look a target in owner's party
						cubicTargetForHeal();
					} else if (skill.getId() == 10056 || skill.getId() == 11292 || skill.getId() == 11807 || skill.getId() == 10093) {
						target = owner;
					} else {
						// offensive skill, we look for an enemy target
						if (owner.getTarget() instanceof Creature) {
							target = (Creature) owner.getTarget();
						}
						
						if (!owner.isAbleToCastOnTarget(target, skill, false)) {
							target = null;
						}
						
						if (target != null && !Util.checkIfInRange(skill.getCastRange(), owner, target, false)) {
							target = null;
						}
						
						if (target == null) {
							break;
						}
					}
					
					if (owner.isSkillDisabled(skill.getReuseHashCode())) {
						continue;
					}
					
					int reuseDelay = skill.getReuseDelay();
					
					if (reuseDelay == 0) {
						reuseDelay = 10000;
					}
					
					owner.disableSkill(skill, reuseDelay);
					
					//Broadcast.toGameMasters(owner.getName() + " Cubic(" + id + ") is casting " + skill.getName() + " on " + target.getName() + ".");
					
					Creature target = CubicInstance.this.target; // copy to avoid npe
					
					if (target != null && !target.isDead()) {
						if (Config.DEBUG) {
							log.info("CubicInstance: Action.run();");
							log.info("Cubic Id: " + id + " Target: " + target.getName() + " distance: " +
									Math.sqrt(target.getDistanceSq(owner.getX(), owner.getY(), owner.getZ())));
						}
						
						owner.broadcastPacket(new MagicSkillUse(owner, target, skill.getId(), skill.getLevel(), 0, 0, 0));
						
						ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
						
						Creature[] targets = {target};
						
						SkillType type = skill.getSkillType();
						
						//Broadcast.toGameMasters("Cubic is using Skill " + skill.getName());
						if (type == SkillType.AGGDAMAGE) {
							if (Config.DEBUG) {
								log.info("CubicInstance: Action.run() handler " + type);
							}
							
							useCubicDisabler(type, CubicInstance.this, skill, targets);
						} else if (type == SkillType.MDAM || type == SkillType.DRAIN) {
							if (Config.DEBUG) {
								log.info("CubicInstance: Action.run() handler " + type);
							}
							
							useCubicMdam(CubicInstance.this, skill, targets);
						} else if (type == SkillType.DEBUFF) {
							if (Config.DEBUG) {
								log.info("CubicInstance: Action.run() handler " + type);
							}
							
							useCubicContinuous(CubicInstance.this, skill, targets);
						} else if (type == SkillType.DRAIN) {
							if (Config.DEBUG) {
								log.info("CubicInstance: Action.run() skill " + type);
							}
							
							((SkillDrain) skill).useCubicSkill(CubicInstance.this, targets);
						} else {
							handler.useSkill(owner, skill, targets);
						}
					}
					
					// The cubic has done an action, increase the currentcount
					currentcount++;
					
					if (skill.getSkillBehavior() != SkillBehaviorType.FRIENDLY) {
						break;
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	public void useCubicContinuous(CubicInstance activeCubic, Skill skill, WorldObject[] targets) {
		for (Creature target : (Creature[]) targets) {
			if (target == null || target.isDead()) {
				continue;
			}
			
			if (skill.isOffensive()) {
				byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
				
				boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld);
				
				if (!acted) {
					activeCubic.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
					
					continue;
				}
			}
			
			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if (target instanceof Player && ((Player) target).isInDuel() && skill.getSkillType() == SkillType.DEBUFF &&
					activeCubic.getOwner().getDuelId() == ((Player) target).getDuelId()) {
				DuelManager dm = DuelManager.getInstance();
				
				for (Abnormal debuff : skill.getEffects(activeCubic.getOwner(), target)) {
					if (debuff != null) {
						dm.onBuff((Player) target, debuff);
					}
				}
			} else {
				skill.getEffects(activeCubic, target, null);
			}
		}
	}
	
	public void useCubicMdam(CubicInstance activeCubic, Skill skill, WorldObject[] targets) {
		for (Creature target : (Creature[]) targets) {
			if (target == null) {
				continue;
			}
			
			if (target.isAlikeDead()) {
				if (target instanceof Player) {
					target.stopFakeDeath(true);
				} else {
					continue;
				}
			}
			
			boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
			
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
			
			int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit, shld);
			
			/*
			 *  If target is reflecting the skill then no damage is done
			 *  Ignoring vengance-like reflections
			 */
			if ((Formulas.calcSkillReflect(target, skill) & Formulas.SKILL_REFLECT_EFFECTS) > 0) {
				damage = 0;
			}
			
			if (Config.DEBUG) {
				log.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);
			}
			
			if (damage > 0) {
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
					target.breakAttack();
					
					target.breakCast();
				}
				
				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);
				
				if (skill.hasEffects()) {
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					
					if (target.getFirstEffect(skill) != null) {
						target.removeEffect(target.getFirstEffect(skill));
					}
					
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld)) {
						skill.getEffects(activeCubic, target, null);
					}
				}
				
				target.reduceCurrentHp(damage, activeCubic.getOwner(), skill);
			}
		}
	}
	
	public void useCubicDisabler(SkillType type, CubicInstance activeCubic, Skill skill, WorldObject[] targets) {
		if (Config.DEBUG) {
			log.info("Disablers: useCubicSkill()");
		}
		
		for (Creature target : (Creature[]) targets) {
			if (target == null || target.isDead()) // bypass if target is null or dead
			{
				continue;
			}
			
			byte shld = Formulas.calcShldUse(activeCubic.getOwner(), target, skill);
			
			switch (type) {
				case DEBUFF: {
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld)) {
						// if this is a debuff let the duel manager know about it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if (target instanceof Player && ((Player) target).isInDuel() && skill.getSkillType() == SkillType.DEBUFF &&
								activeCubic.getOwner().getDuelId() == ((Player) target).getDuelId()) {
							DuelManager dm = DuelManager.getInstance();
							
							for (Abnormal debuff : skill.getEffects(activeCubic.getOwner(), target)) {
								if (debuff != null) {
									dm.onBuff((Player) target, debuff);
								}
							}
						} else {
							skill.getEffects(activeCubic, target, null);
						}
						
						if (Config.DEBUG) {
							log.info("Disablers: useCubicSkill() -> success");
						}
					} else {
						if (Config.DEBUG) {
							log.info("Disablers: useCubicSkill() -> failed");
						}
					}
					
					break;
				}
				
				case CANCEL_DEBUFF: {
					Abnormal[] effects = target.getAllEffects();
					
					if (effects == null || effects.length == 0) {
						break;
					}
					
					int count = skill.getMaxNegatedEffects() > 0 ? 0 : -2;
					
					for (Abnormal e : effects) {
						if (e.getSkill().isDebuff() && count < skill.getMaxNegatedEffects()) {
							// Do not remove raid curse skills
							if (e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082) {
								e.exit();
								
								if (count > -1) {
									count++;
								}
							}
						}
					}
					
					break;
				}
				
				case AGGDAMAGE: {
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill, shld)) {
						if (target instanceof Attackable) {
							target.getAI()
									.notifyEvent(CtrlEvent.EVT_AGGRESSION,
											activeCubic.getOwner(),
											(int) (150 * skill.getPower() / (target.getLevel() + 7)));
						}
						
						skill.getEffects(activeCubic, target, null);
						
						if (Config.DEBUG) {
							log.info("Disablers: useCubicSkill() -> success");
						}
					} else {
						if (Config.DEBUG) {
							log.info("Disablers: useCubicSkill() -> failed");
						}
					}
					
					break;
				}
			}
		}
	}
	
	/**
	 * returns true if the target is inside of the owner's max Cubic range
	 */
	public boolean isInCubicRange(Creature owner, Creature target) {
		if (owner == null || target == null) {
			return false;
		}
		
		int x, y, z;
		
		// temporary range check until real behavior of cubics is known/coded
		int range = MAX_MAGIC_RANGE;
		
		x = owner.getX() - target.getX();
		
		y = owner.getY() - target.getY();
		
		z = owner.getZ() - target.getZ();
		
		return x * x + y * y + z * z <= range * range;
	}
	
	/**
	 * this sets the friendly target for a cubic
	 */
	public void cubicTargetForHeal() {
		Creature target = null;
		
		double percentleft = 100.0;
		
		L2Party party = owner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a
		// party
		if (owner.isInDuel()) {
			if (!DuelManager.getInstance().getDuel(owner.getDuelId()).isPartyDuel()) {
				party = null;
			}
		}
		
		if (party != null && !owner.isInOlympiadMode()) {
			// Get all visible objects in a spheric area near the Creature
			// Get a list of Party Members
			List<Player> partyList = party.getPartyMembers();
			
			for (Player partyMember : partyList) {
				if (!partyMember.isDead()) {
					// if party member not dead, check if he is in castrange of heal cubic
					if (isInCubicRange(owner, partyMember)) {
						// member is in cubic casting range, check if he need heal and if he have
						// the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp()) {
							if (percentleft > partyMember.getCurrentHp() / partyMember.getMaxHp()) {
								percentleft = partyMember.getCurrentHp() / partyMember.getMaxHp();
								
								target = partyMember;
							}
						}
					}
				}
				
				if (partyMember.getPet() != null) {
					if (partyMember.getPet().isDead()) {
						continue;
					}
					
					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(owner, partyMember.getPet())) {
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (partyMember.getPet().getCurrentHp() < partyMember.getPet().getMaxHp() &&
							percentleft > partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp()) {
						percentleft = partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp();
						
						target = partyMember.getPet();
					}
				}
				
				for (SummonInstance summon : partyMember.getSummons()) {
					if (summon.isDead()) {
						continue;
					}
					
					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(owner, summon)) {
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (summon.getCurrentHp() < summon.getMaxHp() && percentleft > summon.getCurrentHp() / summon.getMaxHp()) {
						percentleft = summon.getCurrentHp() / summon.getMaxHp();
						
						target = summon;
					}
				}
			}
		} else {
			if (owner.getCurrentHp() < owner.getMaxHp()) {
				percentleft = owner.getCurrentHp() / owner.getMaxHp();
				
				target = owner;
			}
			
			if (owner.getPet() != null && !owner.getPet().isDead() && owner.getPet().getCurrentHp() < owner.getPet().getMaxHp() &&
					percentleft > owner.getPet().getCurrentHp() / owner.getPet().getMaxHp() && isInCubicRange(owner, owner.getPet())) {
				percentleft = owner.getPet().getCurrentHp() / owner.getPet().getMaxHp();
				
				target = owner.getPet();
			}
			
			for (SummonInstance summon : owner.getSummons()) {
				if (!summon.isDead() && summon.getCurrentHp() < summon.getMaxHp() && percentleft > summon.getCurrentHp() / summon.getMaxHp() &&
						isInCubicRange(owner, summon)) {
					percentleft = summon.getCurrentHp() / summon.getMaxHp();
					
					target = summon;
				}
			}
		}
		
		this.target = target;
	}
	
	public boolean givenByOther() {
		return givenByOther;
	}
	
	private class Heal implements Runnable {
		Heal() {
			// run task
		}
		
		@Override
		public void run() {
			boolean active = false;
			for (CubicInstance cubic : owner.getCubics().values()) {
				if (cubic == CubicInstance.this) {
					active = true;
				}
			}
			
			if (active) {
				active = active && !owner.isDead() && owner.isOnline();
			}
			
			if (!active) {
				stopAction();
				owner.delCubic(id);
				owner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			
			try {
				Skill skill = null;
				
				for (Skill sk : skills) {
					if (sk != null &&
							(sk.getId() == SKILL_CUBIC_HEAL || sk.getId() == SKILL_BUFF_CUBIC_HEAL || sk.getId() == SKILL_MIND_CUBIC_HEAL)) {
						skill = sk;
						
						break;
					}
				}
				
				if (skill != null) {
					Creature target = CubicInstance.this.target;
					
					if (id != BUFF_CUBIC && id != MIND_CUBIC) {
						cubicTargetForHeal();
					} else {
						target = owner;
					}
					
					if (target != null && !target.isDead()) {
						boolean cubicShouldStopIfNoAction = !(id == BUFF_CUBIC || id == MIND_CUBIC);
						
						if (target.getMaxHp() - target.getCurrentHp() > skill.getPower() || !cubicShouldStopIfNoAction) {
							Creature[] targets = {target};
							
							ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
							
							if (handler != null) {
								handler.useSkill(owner, skill, targets);
							} else {
								skill.useSkill(owner, targets);
							}
							
							MagicSkillUse msu = new MagicSkillUse(owner, target, skill.getId(), skill.getLevel(), 0, 0, 0);
							
							owner.broadcastPacket(msu);
						}
					}
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	private class Disappear implements Runnable {
		Disappear() {
			// run task
		}
		
		@Override
		public void run() {
			stopAction();
			
			owner.delCubic(id);
			
			owner.broadcastUserInfo();
		}
	}
}
