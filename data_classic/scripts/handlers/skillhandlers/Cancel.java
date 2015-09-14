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

import javolution.util.FastList;
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
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2AbnormalType;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.Rnd;

/**
 * 
 * @author Kilian
 *
 */
public class Cancel implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CANCEL,
	};
	
	// Resistance given by each buff enchant level
	private final double ENCHANT_BENEFIT = 0.5;
	
	// Minimum cancellation chance
	private final int MIN_CANCEL_CHANCE = 0;
	
	// Whether the skill should depend on level difference
	private boolean LVL_DEPENDENT_PVE = true;
	private boolean LVL_DEPENDENT_PVP = true;
	private double 	PER_LVL_PENALTY = 5;
	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// Remove charged shots from weapon if there is/are any
		dischargeShots(activeChar, skill);
		
		L2Character target;

		final int maxNegate = skill.getMaxNegatedEffects();	// Skill cancels up to this amount of buffs
		double rate 		= skill.getPower();		// Using the skill power as base land rate for the cancellation success
		boolean targetWasInOlys = false;
		
		// This is where the actual cancellation process takes place. Multiple targets possible (e.g. Insane Crusher, Mass Banes)
		for (L2Object obj : targets)
		{
			// Only apply cancellation effect to characters
			if (!(obj instanceof L2Character))
				continue;
			
			if (obj instanceof L2PcInstance)
				targetWasInOlys = ((L2PcInstance)obj).isInOlympiadMode();
			else if (obj instanceof L2SummonInstance)
				((L2SummonInstance)obj).getOwner().isInOlympiadMode();
			else if (obj instanceof L2PetInstance)
				((L2PetInstance)obj).getOwner().isInOlympiadMode();
			
			target = (L2Character)obj;
			
			// No effect on dead targets
			if (target.isDead())
				continue;

			// Reference to the collection of target's buffs and debuffs
			final L2Abnormal[] effects = target.getAllEffects();
			
			// Consider caster skill and target level
			if ((LVL_DEPENDENT_PVE && !(target instanceof L2PcInstance))
					|| (LVL_DEPENDENT_PVP && activeChar instanceof L2PcInstance && target instanceof L2PcInstance))
			{
				int magicLvl = skill.getMagicLevel();
				if (magicLvl <= 0)
					magicLvl = activeChar.getLevel();
				rate -= (target.getLevel() - magicLvl) * PER_LVL_PENALTY;
			}
			
			rate *= Formulas.calcEffectTypeProficiency(activeChar, target, L2AbnormalType.CANCEL) / Formulas.calcEffectTypeResistance(target, L2AbnormalType.CANCEL);
			if (rate < 0)
				rate = 0;

			// Feedback for active .landrates command
			if (activeChar instanceof L2PcInstance && activeChar.getActingPlayer().isLandRates())
			{
				activeChar.sendMessage("Your cancel effect has a base land rate of "+rate
										+". However, enchanted buffs reduce the individual chance.");
			}
			if (target instanceof L2PcInstance && target.getActingPlayer().isLandRates())
			{
				target.sendMessage("The enemy's cancel effect has a base land rate of "+rate
									+". However, enchanted buffs reduce the individual chance.");
			}
			
			// Call cancellation process depending on skill type
			if (rate > 0)
				generalBuffCancellation(activeChar, effects, maxNegate, rate, targetWasInOlys);
		}
	}
	
	private void dischargeShots(L2Character _activeChar, L2Skill _skill)
	{
		final L2ItemInstance weaponInst = _activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (_skill.isMagic())
			{
				if (weaponInst.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (weaponInst.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (_activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) _activeChar;
			
			if (_skill.isMagic())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (_activeChar instanceof L2Npc)
		{
			((L2Npc)_activeChar)._soulshotcharged = false;
			((L2Npc)_activeChar)._spiritshotcharged = false;
		}
	}
	
	private void generalBuffCancellation(L2Character activeChar, L2Abnormal[] effects, int maxNegate, double rate, boolean targetWasInOlys)
	{
		FastList<L2Abnormal> buffs = new FastList<L2Abnormal>();
		
		// Filter buff-type effects out of the effect collection
		for (L2Abnormal effect : effects)
		{
			if (effect.canBeStolen())
				buffs.add(effect);
		}
		
		// In case there are less than _maxNegate buffs available, it would cause multiple tries on same buff
		if (buffs.size() < maxNegate)
			maxNegate = buffs.size();
		
		int candidate = 0;
		for (int i = 0; i < maxNegate; i++)
		{
			// Get a random buff index for cancellation try
			candidate = Rnd.get(buffs.size());
			
			// Save original rate temporarily
			double tempRate = rate;

			// Reduce land rate depending on effect's enchant level
			if (buffs.get(candidate).getLevel() > 100)
				rate -= (buffs.get(candidate).getLevel() % 100) * ENCHANT_BENEFIT;
			if (rate < MIN_CANCEL_CHANCE)
				rate = MIN_CANCEL_CHANCE;

			// More detailed .landrates feedback considering enchanted buffs
			if (activeChar instanceof L2PcInstance && activeChar.getActingPlayer().isLandRates())
			{
				activeChar.sendMessage("Attempted to remove "+buffs.get(candidate).getSkill().getName()
										+" with "+rate+"% chance.");
			}
			
			// Give it a try with rate% chance
			if (Rnd.get(100) < rate)
			{
				buffs.get(candidate).exit();
				if (buffs.get(candidate).getEffected() instanceof L2PcInstance)
					buffs.get(candidate).getEffected().getActingPlayer().scheduleEffectRecovery(buffs.get(candidate), 60, targetWasInOlys);
				if (activeChar instanceof L2PcInstance && activeChar.getActingPlayer().isLandRates())
					activeChar.sendMessage("Attempt to remove "+buffs.get(candidate).getSkill().getName()+" succeeded.");
			}
			
			// Restore original rate
			rate = tempRate;
			
			// Remove the reference to the canceled buffs from the collection to not try same again
			buffs.remove(candidate);
		}
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