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

import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.L2Skill.SkillTargetType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.templates.skills.L2SkillType;

public class HealPercent implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.HEAL_PERCENT,
		L2SkillType.MANAHEAL_PERCENT,
		L2SkillType.CPHEAL_PERCENT,
		L2SkillType.HPMPHEAL_PERCENT,
		L2SkillType.HPMPCPHEAL_PERCENT,
		L2SkillType.HPCPHEAL_PERCENT
	};
	
	/**
	 * 
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);
		
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		
		boolean cp = false;
		boolean hp = false;
		boolean mp = false;
		switch (skill.getSkillType())
		{
			case CPHEAL_PERCENT:
				cp = true;
				break;
			case HEAL_PERCENT:
				hp = true;
				break;
			case MANAHEAL_PERCENT:
				mp = true;
				break;
			case HPMPHEAL_PERCENT:
				mp = true;
				hp = true;
				break;
			case HPMPCPHEAL_PERCENT:
				cp = true;
				hp = true;
				mp = true;
				break;
			case HPCPHEAL_PERCENT:
				hp = true;
				cp = true;
		}
		
		//StatusUpdate su = null;
		SystemMessage sm;
		double amount = 0;
		double percent = skill.getPower();
		// Healing critical, since CT2.3 Gracia Final
		if (skill.getTargetType() != SkillTargetType.TARGET_SELF && !skill.isPotion()
				&& Formulas.calcMCrit(activeChar.getMCriticalHit(activeChar, skill)))
		{
			activeChar.sendMessage("Healing critical!");
			percent *= 2;
		}
		
		if (percent > 100.0)
			percent = 100.0;
		
		boolean full = percent == 100.0;
		boolean targetPlayer = false;
		
		for (L2Character target: (L2Character[]) targets)
		{
			//1505  - sublime self sacrifice
			//11560 - celestial aegis
			if ((target == null || target.isDead() || target.isInvul()) && skill.getId() != 1505 && skill.getId() != 11560)
				continue;
			
			if (target != activeChar && target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar)
				continue;
			
			targetPlayer = target instanceof L2PcInstance;
			
			if (target != activeChar)
			{
				// Cursed weapon owner can't heal or be healed
				if (activeChar instanceof L2PcInstance && ((L2PcInstance)activeChar).isCursedWeaponEquipped())
					continue;
				if (targetPlayer && ((L2PcInstance)target).isCursedWeaponEquipped())
					continue;
				
				// Nor all vs all event player
				if (activeChar instanceof L2PcInstance
						&& ((L2PcInstance)activeChar).getEvent() != null
						&& ((L2PcInstance)activeChar).getEvent().isState(EventState.STARTED)
						&& ((L2PcInstance)activeChar).getEvent().getConfig().isAllVsAll()
						&& !(target instanceof L2Summon && ((L2Summon)target).getOwner() == activeChar))
					continue;
			}
			
			// Doors and flags can't be healed in any way
			if (hp && (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance))
				continue;
			
			// Only players have CP
			if (cp && targetPlayer)
			{
				if (full)
					amount = target.getMaxCp();
				else
					amount = target.getMaxCp() * percent / 100.0;
				
				if (skill.getId() == 10270) // Second Wind
					amount = target.getMaxCp() * 15.0 / 100.0;
				
				amount = Math.min(amount, target.getMaxCp() - target.getCurrentCp());
				target.setCurrentCp(amount + target.getCurrentCp());
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
				sm.addNumber((int)amount);
				target.sendPacket(sm);
			}
			
			if (hp)
			{
				if (full)
					amount = target.getMaxHp();
				else
					amount = target.getMaxHp() * percent / 100.0;
				
				amount = Math.min(amount, target.getMaxHp() - target.getCurrentHp());
				target.setCurrentHp(amount + target.getCurrentHp());
				
				if (targetPlayer)
				{
					if (activeChar != target)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
						sm.addCharName(activeChar);
					}
					else
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
					sm.addNumber((int)amount);
					sm.addHpChange(target.getObjectId(), activeChar.getObjectId(), (int)amount);
					target.sendPacket(sm);
				}
			}
			
			if (mp)
			{
				if (full)
					amount = target.getMaxMp();
				else
					amount = target.getMaxMp() * percent / 100.0;
				
				amount = Math.min(amount, target.getMaxMp() - target.getCurrentMp());
				target.setCurrentMp(amount + target.getCurrentMp());
				
				if (targetPlayer)
				{
					if (activeChar != target)
					{
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
						sm.addCharName(activeChar);
					}
					else
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
					sm.addNumber((int)amount);
					target.sendPacket(sm);
				}
			}
			
			target.broadcastStatusUpdate();
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