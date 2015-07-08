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
package l2tserver.gameserver.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import l2tserver.gameserver.datatables.SkillTable;
import l2tserver.gameserver.handler.ISkillHandler;
import l2tserver.gameserver.handler.SkillHandler;
import l2tserver.gameserver.model.ChanceCondition.TriggerType;
import l2tserver.gameserver.model.L2Skill.SkillTargetType;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Playable;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.actor.instance.L2SummonInstance;
import l2tserver.gameserver.network.serverpackets.MagicSkillLaunched;
import l2tserver.gameserver.network.serverpackets.MagicSkillUse;
import l2tserver.gameserver.stats.effects.EffectChanceSkillTrigger;
import l2tserver.gameserver.templates.skills.L2SkillType;
import l2tserver.log.Log;

/**
 * CT2.3: Added support for allowing effect as a chance skill trigger (DrHouse)
 *
 * @author  kombat
 */
public class ChanceSkillList extends ConcurrentHashMap<IChanceSkillTrigger, ChanceCondition>
{
	private static final long serialVersionUID = 1L;
	
	private final L2Character _owner;
	
	public ChanceSkillList(L2Character owner)
	{
		super();
		_owner = owner;
	}
	
	public L2Character getOwner()
	{
		return _owner;
	}
	
	public void onHit(L2Character target, int damage, boolean ownerWasHit, boolean isSummon, boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
				event |= ChanceCondition.EVT_CRIT;
		}
		
		if (isSummon && ownerWasHit)
			event = ChanceCondition.EVT_ATTACKED_SUMMON;
		
		onEvent(event, damage, wasCrit, target, null, Elementals.NONE);
	}
	
	public void onBlock(L2Character attacker)
	{
		onEvent(ChanceCondition.EVT_SHIELD_BLOCK, 0, false, attacker, null, Elementals.NONE);
	}
	
	public void onEvadedHit(L2Character attacker)
	{
		onEvent(ChanceCondition.EVT_EVADED_HIT, 0, false, attacker, null, Elementals.NONE);
	}
	
	public void onSkillHit(L2Character target, L2Skill skill, boolean isSummon, boolean ownerWasHit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (skill.isOffensive())
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= skill.isMagic() ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= skill.isOffensive() ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		if (isSummon && ownerWasHit)
			event = ChanceCondition.EVT_ATTACKED_SUMMON;
		
		if (skill.isToggle())
			return;
		
		onEvent(event, 0, false, target, skill, skill.getElement());
	}
	
	public void onStart(L2Skill skill, byte element)
	{
		onEvent(ChanceCondition.EVT_ON_START, 0, false, _owner, skill, element);
	}
	
	public void onActionTime(L2Skill skill, byte element)
	{
		onEvent(ChanceCondition.EVT_ON_ACTION_TIME, 0, false, _owner, skill, element);
	}
	
	public void onExit(L2Skill skill, byte element)
	{
		onEvent(ChanceCondition.EVT_ON_EXIT, 0, false, _owner, skill, element);
	}
	
	public void onKill(L2Character target)
	{
		onEvent(ChanceCondition.EVT_KILL, 0, false, target, null, Elementals.NONE);
	}
	
	public void onEvent(int event, int damage, boolean critical, L2Character target, L2Skill skill, byte element)
	{
		if (_owner.isDead())
			return;

		//if (skill != null && skill.isToggle())
			//return;
		
		final boolean playable = target instanceof L2Playable;
		for (Map.Entry<IChanceSkillTrigger, ChanceCondition> e : entrySet())
		{
			if (e.getValue() != null && e.getValue().trigger(event, damage, critical, element, playable, skill))
			{
				if (e.getKey() instanceof L2Skill)
					makeCast((L2Skill)e.getKey(), target);
				else if (e.getKey() instanceof EffectChanceSkillTrigger)
				{
					if ((event & (ChanceCondition.EVT_ON_START | ChanceCondition.EVT_ON_ACTION_TIME | ChanceCondition.EVT_ON_EXIT)) != 0
							&& ((EffectChanceSkillTrigger)e.getKey()).getSkill() != skill)
						continue;
					
					makeCast((EffectChanceSkillTrigger)e.getKey(), target, SkillTable.getInstance().getInfo(e.getKey().getTriggeredChanceId(), 1).getTargetType());
				}
			}
		}
	}
	
	private void makeCast(L2Skill skill, L2Character target)
	{
		try
		{
			if (skill.getWeaponDependancy(_owner, true) && skill.checkCondition(_owner, target, false))
			{
				if (skill.triggersChanceSkill() && skill.getTriggeredLevel() > -1) //skill will trigger another skill, but only if its not chance skill
				{
					//Auto increase trigger skills
					int level = skill.getTriggeredLevel();
					if (level == 0)
					{
						L2Abnormal effect = _owner.getFirstEffect(skill.getTriggeredChanceId());
						if (effect != null)
						{
							int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getTriggeredChanceId());
							if (effect.getLevelHash() < maxLevel)
								level += effect.getLevelHash() + 1;
							else
								level = maxLevel;
						}
						else
							level = 1;
					}
					
					skill = SkillTable.getInstance().getInfo(skill.getTriggeredChanceId(), level);
					if (skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
						return;
					skill.setIsTriggered();
				}
				
				if (_owner.isSkillDisabled(skill) || target.isDead())
					return;
				
				if (!skill.checkCondition(_owner, target, false))
					return;
				
				if (skill.getReuseDelay() > 0)
					_owner.disableSkill(skill, skill.getReuseDelay());
				
				L2Object[] targets = skill.getTargetList(_owner, false, target);
				
				if (targets.length == 0)
					return;
				
				L2Character firstTarget = (L2Character)targets[0];
				
				ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
				
				_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill.getDisplayId(), skill.getLevelHash(), targets));
				_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, skill.getDisplayId(), skill.getLevelHash(), 0, 0, 0));
				
				// Launch the magic skill and calculate its effects
				// TODO: once core will support all possible effects, use effects (not handler)
				if (handler != null)
					handler.useSkill(_owner, skill, targets);
				else
					skill.useSkill(_owner, targets);
			}
		}
		catch(Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
	
	private void makeCast(EffectChanceSkillTrigger effect, L2Character target, SkillTargetType skillTargetType)
	{
		try
		{
			if (effect == null || !effect.triggersChanceSkill())
				return;
			
			int level = effect.getTriggeredChanceLevel();
			if (level == 0)
			{
				level = 1;
				if (skillTargetType == SkillTargetType.TARGET_ALL_SUMMONS || skillTargetType == SkillTargetType.TARGET_SUMMON)
				{
					if (target instanceof L2PcInstance)
					{
						List<L2SummonInstance> summons = ((L2PcInstance)target).getSummons();
						if (!summons.isEmpty())
							target = summons.get(0);
					}
				}
				
				for (L2Abnormal e : target.getAllEffects())
				{
					if (e == null)
						continue;
					
					if (e.getSkill().getId() == effect.getTriggeredChanceId())
					{
						if (e.getSkill().getLevelHash() < SkillTable.getInstance().getMaxLevel(e.getSkill().getId()))
						{
							level = e.getSkill().getLevelHash() + 1;
						}
						else
							return;
					}
				}
			}
			
			L2Skill triggered = SkillTable.getInstance().getInfo(effect.getTriggeredChanceId(), level);
			if (triggered == null)
				return;
			
			triggered.setIsTriggered();
			L2Character caster = triggered.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF ? _owner : effect.getEffector();
			
			if (caster == null
					|| triggered.getSkillType() == L2SkillType.NOTDONE
					|| caster.isSkillDisabled(triggered))
				return;
			
			if (!triggered.checkCondition(_owner, target, false))
				return;
			
			if (triggered.getReuseDelay() > 0)
				caster.disableSkill(triggered, triggered.getReuseDelay());
			
			L2Object[] targets = triggered.getTargetList(caster, false, target);
			
			if (targets.length == 0)
				return;
			
			L2Character firstTarget = (L2Character)targets[0];
			
			ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(triggered.getSkillType());
			
			if (effect.getTriggeredChanceCondition().getTriggerType() != TriggerType.ON_ACTION_TIME)
			{
				_owner.broadcastPacket(new MagicSkillLaunched(_owner, triggered.getDisplayId(), triggered.getLevelHash(), targets));
				_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, triggered.getDisplayId(), triggered.getLevelHash(), 0, 0, 0));
			}
			
			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if (handler != null)
				handler.useSkill(caster, triggered, targets);
			else
				triggered.useSkill(caster, targets);
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}
}