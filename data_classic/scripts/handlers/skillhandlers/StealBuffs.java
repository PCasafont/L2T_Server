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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.Rnd;

//TODO: check if it will be needed anymore, that shitty patch seems that it will delete D:

public class StealBuffs implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STEAL_BUFF
	};

	// Resistance given by each buff enchant level
	private final double ENCHANT_BENEFIT = 0.5;
	
	// Minimum cancellation chance
	private final int MIN_CANCEL_CHANCE = 0;
	
	// Level difference penalty
	private double PER_LVL_PENALTY = 5;

	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		dischargeShots(activeChar, skill);
		
		L2Character target;
		L2Abnormal effect;
		int maxNegate = skill.getMaxNegatedEffects();
		double chance = skill.getPower();
		boolean targetWasInOlys = false;
		
		for (L2Object obj: targets)
		{
			if (!(obj instanceof L2Character))
				continue;
			
			if (obj instanceof L2PcInstance)
				targetWasInOlys = ((L2PcInstance)obj).isInOlympiadMode();
			else if (obj instanceof L2SummonInstance)
				((L2SummonInstance)obj).getOwner().isInOlympiadMode();
			else if (obj instanceof L2PetInstance)
				((L2PetInstance)obj).getOwner().isInOlympiadMode();
			
			target = (L2Character)obj;
			
			if (target.isDead())
				continue;
			
			if (!(target instanceof L2PcInstance))
				continue;
			
			Env env;
			int lastSkillId = 0;
			final L2Abnormal[] effects = target.getAllEffects();
			final List<L2Abnormal> toSteal = new ArrayList<L2Abnormal>(maxNegate);
			
			// Consider caster skill and target level
			chance -= (target.getLevel() - skill.getMagicLevel()) * PER_LVL_PENALTY;
			chance *= Formulas.calcEffectTypeProficiency(activeChar, target, L2AbnormalType.CANCEL) / Formulas.calcEffectTypeResistance(target, L2AbnormalType.CANCEL);
			if (chance < 0.0)
				chance = 0.0;
			
			for (int i = effects.length; --i >= 0;) // reverse order
			{
				effect = effects[i];
				if (effect == null)
					continue;
				
				if (!effect.canBeStolen()) // remove effect if can't be stolen
				{
					effects[i] = null;
					continue;
				}
				
				// if eff time is smaller than 5 sec, will not be stolen, just to save CPU,
				// avoid synchronization(?) problems and NPEs
				if (effect.getDuration() - effect.getTime() < 5)
				{
					effects[i] = null;
					continue;
				}
				
				// first pass - only dances/songs
				if (!effect.getSkill().isDance())
					continue;
				
				if (effect.getSkill().getId() != lastSkillId)
				{
					lastSkillId = effect.getSkill().getId();
					maxNegate--;
				}
				
				// Save original rate temporarily
				double tempRate = chance;

				// Reduce land rate depending on effect's enchant level
				if (effect.getLevel() > 100)
					chance -= (effect.getLevel() % 100) * ENCHANT_BENEFIT;
				if (chance < MIN_CANCEL_CHANCE)
					chance = MIN_CANCEL_CHANCE;
				
				if (Rnd.get(100) < chance)	// Tenkai custom - only percentual chance to steal a buff
					toSteal.add(effect);
				
				// Restore original rate
				chance = tempRate;
				
				if (maxNegate == 0)
					break;
			}
			
			if (maxNegate > 0) // second pass
			{
				lastSkillId = 0;
				for (int i = effects.length; --i >= 0;)
				{
					effect = effects[i];
					if (effect == null)
						continue;
					
					// second pass - all except dances/songs
					if (effect.getSkill().isDance())
						continue;
					
					if (effect.getSkill().getId() != lastSkillId)
					{
						lastSkillId = effect.getSkill().getId();
						maxNegate--;
					}
					
					// Save original rate temporarily
					double tempRate = chance;

					// Reduce land rate depending on effect's enchant level
					if (effect.getLevel() > 100)
						chance -= (effect.getLevel() % 100) * ENCHANT_BENEFIT;
					if (chance < MIN_CANCEL_CHANCE)
						chance = MIN_CANCEL_CHANCE;
					
					if (Rnd.get(100) < chance)	// Tenkai custom - only percentual chance to steal a buff
						toSteal.add(effect);
					
					// Restore original rate
					chance = tempRate;
					
					if (maxNegate == 0)
						break;
				}
			}
			
			if (toSteal.size() == 0)
				continue;
			
			// stealing effects
			for (L2Abnormal eff : toSteal)
			{
				env = new Env();
				env.player = target;
				env.target = activeChar;
				env.skill = eff.getSkill();
				try
				{
					effect = eff.getTemplate().getStolenEffect(env, eff);
					if (effect != null)
					{
						effect.scheduleEffect();
						if (effect.getShowIcon() && activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
							sm.addSkillName(effect);
							activeChar.sendPacket(sm);
						}
					}
					// Finishing stolen effect
					eff.exit();
					
					// Tenkai custom - Buffs returning
					if (eff.getEffected() instanceof L2PcInstance)
						eff.getEffected().getActingPlayer().scheduleEffectRecovery(eff, 15, targetWasInOlys);
				}
				catch (RuntimeException e)
				{
					_log.log(Level.WARNING, "Cannot steal effect: " + eff + " Stealer: " + activeChar + " Stolen: " + target, e);
				}
			}
			
			//Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		if (skill.hasSelfEffects())
		{
			// Applying self-effects
			effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}
	}

	private void dischargeShots(L2Character activeChar, L2Skill skill)
	{
		// discharge shots
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				if (weaponInst.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (weaponInst.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;
			
			if (skill.isMagic())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Npc)
			((L2Npc) activeChar)._spiritshotcharged = false;
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