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

package l2server.gameserver.model;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.ChanceCondition.TriggerType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.serverpackets.MagicSkillLaunched;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.stats.effects.EffectChanceSkillTrigger;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CT2.3: Added support for allowing effect as a chance skill trigger (DrHouse)
 *
 * @author kombat
 */
public class ChanceSkillList extends ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition> {
	private static Logger log = LoggerFactory.getLogger(ChanceSkillList.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private final Creature owner;
	
	public ChanceSkillList(Creature owner) {
		super();
		this.owner = owner;
	}
	
	public Creature getOwner() {
		return owner;
	}
	
	public void onHit(Creature target, int damage, boolean ownerWasHit, boolean isSummon, boolean wasCrit) {
		int event;
		if (ownerWasHit) {
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit) {
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
			}
		} else {
			event = ChanceCondition.EVT_HIT;
			if (wasCrit) {
				event |= ChanceCondition.EVT_CRIT;
			}
		}
		
		if (isSummon && ownerWasHit) {
			event = ChanceCondition.EVT_ATTACKED_SUMMON;
		}
		
		onEvent(event, damage, wasCrit, target, null, Elementals.NONE);
	}
	
	public void onBlock(Creature attacker) {
		onEvent(ChanceCondition.EVT_SHIELD_BLOCK, 0, false, attacker, null, Elementals.NONE);
	}
	
	public void onEvadedHit(Creature attacker) {
		onEvent(ChanceCondition.EVT_EVADED_HIT, 0, false, attacker, null, Elementals.NONE);
	}
	
	public void onSkillHit(Creature target, Skill skill, boolean isSummon, boolean ownerWasHit) {
		int event;
		if (ownerWasHit) {
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (skill.isOffensive()) {
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
			} else {
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		} else {
			event = ChanceCondition.EVT_CAST;
			event |= skill.isMagic() ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= skill.isOffensive() ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		if (isSummon && ownerWasHit) {
			event = ChanceCondition.EVT_ATTACKED_SUMMON;
		}
		
		if (skill.isToggle()) {
			return;
		}
		
		onEvent(event, 0, false, target, skill, skill.getElement());
	}
	
	public void onStart(Skill skill, byte element) {
		onEvent(ChanceCondition.EVT_ON_START, 0, false, owner, skill, element);
	}
	
	public void onActionTime(Skill skill, byte element) {
		onEvent(ChanceCondition.EVT_ON_ACTION_TIME, 0, false, owner, skill, element);
	}
	
	public void onExit(Skill skill, byte element) {
		onEvent(ChanceCondition.EVT_ON_EXIT, 0, false, owner, skill, element);
	}
	
	public void onKill(Creature target) {
		onEvent(ChanceCondition.EVT_KILL, 0, false, target, null, Elementals.NONE);
	}
	
	public void onEvent(int event, int damage, boolean critical, Creature target, Skill skill, byte element) {
		if (owner.isDead()) {
			return;
		}
		
		//if (skill != null && skill.isToggle())
		//return;
		final boolean playable = target instanceof Playable;
		for (Entry<IChanceSkillTrigger, ChanceCondition> e : entrySet()) {
			if (e.getValue() != null && e.getValue().trigger(event, damage, critical, element, playable, skill)) {
				if (e.getKey() instanceof Skill) {
					makeCast((Skill) e.getKey(), target);
				} else if (e.getKey() instanceof EffectChanceSkillTrigger) {
					if ((event & (ChanceCondition.EVT_ON_START | ChanceCondition.EVT_ON_ACTION_TIME | ChanceCondition.EVT_ON_EXIT)) != 0 &&
							((EffectChanceSkillTrigger) e.getKey()).getSkill() != skill) {
						continue;
					}
					
					makeCast((EffectChanceSkillTrigger) e.getKey(),
							target,
							SkillTable.getInstance().getInfo(e.getKey().getTriggeredChanceId(), 1).getTargetType());
				}
			}
		}
	}
	
	private void makeCast(Skill skill, Creature target) {
		try {
			if (skill.getWeaponDependancy(owner, true) && skill.checkCondition(owner, target, false)) {
				if (skill.triggersChanceSkill() &&
						skill.getTriggeredChanceLevel() > -1) //skill will trigger another skill, but only if its not chance skill
				{
					//Auto increase trigger skills
					int level = skill.getTriggeredChanceLevel();
					if (level == 0) {
						Abnormal effect = owner.getFirstEffect(skill.getTriggeredChanceId());
						if (effect != null) {
							int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getTriggeredChanceId());
							if (effect.getLevelHash() < maxLevel) {
								level += effect.getLevelHash() + 1;
							} else {
								level = maxLevel;
							}
						} else {
							level = 1;
						}
					}
					
					skill = SkillTable.getInstance().getInfo(skill.getTriggeredChanceId(), level);
					if (skill == null || skill.getSkillType() == SkillType.NOTDONE) {
						return;
					}
					
					skill.setIsTriggered();
				}
				
				if (owner.isSkillDisabled(skill) || target.isDead()) {
					return;
				}
				
				if (!skill.checkCondition(owner, target, false)) {
					return;
				}
				
				if (skill.getReuseDelay() > 0) {
					owner.disableSkill(skill, skill.getReuseDelay());
				}
				
				WorldObject[] targets = skill.getTargetList(owner, false, target);
				
				if (targets == null || targets.length == 0) {
					return;
				}
				
				Creature firstTarget = (Creature) targets[0];
				
				ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				
				owner.broadcastPacket(new MagicSkillLaunched(owner, skill.getDisplayId(), skill.getLevelHash(), targets));
				owner.broadcastPacket(new MagicSkillUse(owner, firstTarget, skill.getDisplayId(), skill.getLevelHash(), 0, 0, 0));
				
				// Launch the magic skill and calculate its effects
				// TODO: once core will support all possible effects, use effects (not handler)
				if (handler != null) {
					handler.useSkill(owner, skill, targets);
				} else {
					skill.useSkill(owner, targets);
				}
			}
		} catch (Exception e) {
			log.warn("", e);
		}
	}
	
	private void makeCast(EffectChanceSkillTrigger effect, Creature target, SkillTargetType skillTargetType) {
		try {
			if (effect == null || !effect.triggersChanceSkill()) {
				return;
			}
			
			int level = effect.getTriggeredChanceLevel();
			int enchRoute = effect.getTriggeredChanceEnchantRoute();
			int enchLevel = effect.getTriggeredChanceEnchantLevel();
			
			Skill triggered = SkillTable.getInstance().getInfo(effect.getTriggeredChanceId(), level == 0 ? 1 : level, enchRoute, enchLevel);
			if (triggered == null) {
				return;
			}
			
			if (target instanceof Player && triggered.getTargetType() == SkillTargetType.TARGET_SUMMON) {
				List<SummonInstance> summons = ((Player) target).getSummons();
				if (!summons.isEmpty()) {
					target = summons.get(0);
				}
			}
			
			if (level == 0) {
				level = 1;
				for (Abnormal e : target.getAllEffects()) {
					if (e == null) {
						continue;
					}
					
					if (e.getSkill().getId() == effect.getTriggeredChanceId()) {
						if (e.getSkill().getLevel() < SkillTable.getInstance().getMaxLevel(e.getSkill().getId())) {
							level = e.getSkill().getLevel() + 1;
						} else {
							level = e.getSkill().getLevel();
						}
					}
				}
				
				triggered = SkillTable.getInstance().getInfo(effect.getTriggeredChanceId(), level, enchRoute, enchLevel);
				if (triggered == null) {
					return;
				}
			}
			
			triggered.setIsTriggered();
			Creature caster = triggered.getTargetType() == SkillTargetType.TARGET_SELF ? owner : effect.getEffector();
			
			if (caster == null || triggered.getSkillType() == SkillType.NOTDONE || caster.isSkillDisabled(triggered)) {
				return;
			}
			
			if (!triggered.checkCondition(owner, target, false)) {
				return;
			}
			
			if (triggered.getReuseDelay() > 0) {
				caster.disableSkill(triggered, triggered.getReuseDelay());
			}
			
			WorldObject[] targets = triggered.getTargetList(caster, false, target);
			
			if (targets == null || targets.length == 0) {
				return;
			}
			/*
            for (WorldObject obj : targets)
			{
				if (!(obj instanceof Creature))
					continue;

				Creature character = (Creature) obj;

				Abnormal activeEffect = character.getFirstEffect(effect.getTriggeredChanceId());
				if (activeEffect != null)
				{
					if (activeEffect.getLevel() == level)
						continue;
				}

				if (activeEffect != null)
					activeEffect.exit();
			}*/
			
			Creature firstTarget = (Creature) targets[0];
			
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(triggered.getSkillType());
			
			if (effect.getTriggeredChanceCondition().getTriggerType() != TriggerType.ON_ACTION_TIME) {
				owner.broadcastPacket(new MagicSkillLaunched(owner, triggered.getDisplayId(), triggered.getLevelHash(), targets));
				owner.broadcastPacket(new MagicSkillUse(owner, firstTarget, triggered.getDisplayId(), triggered.getLevelHash(), 0, 0, 0));
			}
			
			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if (handler != null) {
				handler.useSkill(caster, triggered, targets);
			} else {
				triggered.useSkill(caster, targets);
			}
		} catch (Exception e) {
			log.warn("", e);
		}
	}
}
