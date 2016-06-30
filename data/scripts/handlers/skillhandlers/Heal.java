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
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.L2Summon;
import l2server.gameserver.model.actor.instance.L2ArmyMonsterInstance;
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2EventGolemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.BaseStats;
import l2server.gameserver.stats.Env;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.stats.funcs.Func;
import l2server.gameserver.stats.funcs.FuncSet;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.skills.L2SkillTargetType;
import l2server.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class Heal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.HEAL, L2SkillType.HEAL_STATIC, L2SkillType.OVERHEAL, L2SkillType.OVERHEAL_STATIC };
	
	/**
	 *
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(l2server.gameserver.model.actor.L2Character, l2server.gameserver.model.L2Skill, l2server.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		//ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);
		
		//if (handler != null)
		//	handler.useSkill(activeChar, skill, targets);
		
		double power = skill.getPower();
		double baseHeal = power;
		double weaponBonus = 0;
		
		switch (skill.getSkillType())
		{
			case HEAL_STATIC:
			case OVERHEAL_STATIC:
				break;
			default:
				final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
				double mAtkMul = 1; // mAtk multiplier
				if (weaponInst != null)
				{
					if (weaponInst.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
					{
						if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isMageClass())
						{
							mAtkMul = weaponInst.getChargedSpiritShot();
						}
						else
						{
							// no static bonus
							// grade dynamic bonus
							switch (weaponInst.getItem().getItemGrade())
							{
								case L2Item.CRYSTAL_S84:
									mAtkMul = 4;
									break;
								case L2Item.CRYSTAL_S80:
									mAtkMul = 2;
									break;
							}
							// shot dynamic bonus
							if (weaponInst.getChargedSpiritShot() >= L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
								mAtkMul *= 4; // 16x/8x/4x s84/s80/other
							else
								mAtkMul += 1; // 5x/3x/1x s84/s80/other
						}
						
						weaponInst.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
					}
				}
				// If there is no weapon equipped, check for an active summon.
				else if (activeChar instanceof L2Summon && ((L2Summon) activeChar).getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
				{
					if (((L2Summon) activeChar).getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					{
						mAtkMul = 4;
					}
					else
						mAtkMul = 2;
					
					((L2Summon) activeChar).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
				else if (activeChar instanceof L2Npc && ((L2Npc) activeChar)._spiritshotcharged)
				{
					mAtkMul = 4;
					
					((L2Npc) activeChar)._spiritshotcharged = false;
				}
				
				baseHeal = power + Math.sqrt(mAtkMul * activeChar.getMAtk(activeChar, skill));
				
				weaponBonus = activeChar.getLevelMod();
				if (activeChar.getLevel() > 99)
					weaponBonus += 1.5 * (activeChar.getLevel() - 99.0);
				if (weaponInst != null)
				{
					double weaponMAtk = 0.0;
					for (Func func : weaponInst.getStatFuncs())
					{
						if (func instanceof FuncSet && func.stat == Stats.MAGIC_ATTACK)
						{
							Env env = new Env();
							env.player = activeChar;
							env.value = 0;
							env.baseValue = 0;
							func.calc(env);
							weaponMAtk = env.value;
						}
					}
					weaponBonus *= weaponMAtk;
				}
				
				weaponBonus *= BaseStats.MEN.calcBonus(activeChar);
		}
		
		double hp = 0;
		double cp = 0;
		
		for (L2Character target : (L2Character[]) targets)
		{
			// We should not heal if char is dead/invul
			if (target == null || target.isDead() || target.isInvul(activeChar))
				continue;
			
			// No healing from others for player in duels
			if (target instanceof L2PcInstance && target.getActingPlayer().isInDuel() && target.getObjectId() != activeChar.getObjectId())
				continue;
			
			if (target instanceof L2DoorInstance || target instanceof L2SiegeFlagInstance || target instanceof L2EventGolemInstance || target instanceof L2ArmyMonsterInstance)
				continue;
			
			if (target != activeChar)
			{
				// Player holding a cursed weapon can't be healed and can't heal
				if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
					continue;
				else if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isCursedWeaponEquipped())
					continue;
				
				// Nor all vs all event player
				if (activeChar instanceof L2PcInstance && ((L2PcInstance) activeChar).isPlayingEvent() && ((L2PcInstance) activeChar).getEvent().getConfig().isAllVsAll() && !(target instanceof L2Summon && ((L2Summon) target).getOwner() == activeChar))
					continue;
			}
			
			hp = baseHeal;
			
			if (skill.getSkillType() != L2SkillType.HEAL_STATIC)
			{
				if (skill.getSkillType() != L2SkillType.OVERHEAL_STATIC)
				{
					hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 1.0, null, null);
					
					// Healer proficiency (since CT1)
					hp = activeChar.calcStat(Stats.HEAL_PROFICIENCY, hp, null, null);
					
					// Extra bonus (since CT1.5)
					if (!skill.isPotion())
						hp += target.calcStat(Stats.HEAL_STATIC_BONUS, 0, null, null);
					
					if (weaponBonus < hp)
						hp += weaponBonus;
					else
						hp += hp;
					
					// Healing critical, since CT2.3 Gracia Final
					if (skill.getCritChance() != -1 && skill.getTargetType() != L2SkillTargetType.TARGET_SELF && !skill.isPotion() && Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill)))
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT_MAGIC));
						hp *= 2;
					}
				}
				
				// from CT2 u will receive exact HP, u can't go over it, if u have full HP and u get HP buff, u will receive 0HP restored message
				// Soul: but from GoD onwards that "overheal" factor is converted into CP by some Areoe Healer skills
				if (target.getCurrentHp() + hp >= target.getMaxHp())
				{
					if (skill.getSkillType() == L2SkillType.OVERHEAL || skill.getSkillType() == L2SkillType.OVERHEAL_STATIC)
					{
						// CP OVERHEAL needs to be calculated before recalculate HP heal
						cp = hp - (target.getMaxHp() - target.getCurrentHp());
						if (target.getCurrentCp() + cp >= target.getMaxCp())
							cp = target.getMaxCp() - target.getCurrentCp();
					}
					hp = target.getMaxHp() - target.getCurrentHp();
				}
			}
			
			if (Config.isServer(Config.TENKAI) && activeChar instanceof L2PcInstance && activeChar.isInParty() && (skill.getTargetType() == L2SkillTargetType.TARGET_FRIENDS || skill.getTargetType() == L2SkillTargetType.TARGET_FRIEND_NOTME))
			{
				int classId = ((L2PcInstance) activeChar).getCurrentClass().getParent().getAwakeningClassId();
				int members = 0;
				for (L2PcInstance partyMember : activeChar.getParty().getPartyMembers())
				{
					if (partyMember.getCurrentClass().getParent() != null
							&& partyMember.getCurrentClass().getParent().getAwakeningClassId() == classId)
						members++;
				}
				
				if (members > 1)
				{
					hp /= members;
					cp /= members;
				}
			}
			
			hp = Math.min(target.calcStat(Stats.GAIN_HP_LIMIT, target.getMaxHp(), null, null), target.getCurrentHp() + hp) - target.getCurrentHp();
			if (hp < 0)
				hp = 0;
			
			cp = Math.min(target.calcStat(Stats.GAIN_CP_LIMIT, target.getMaxCp(), null, null), target.getCurrentCp() + cp) - target.getCurrentCp();
			
			target.setCurrentHp(hp + target.getCurrentHp());
			
			if (skill.getSkillType() == L2SkillType.OVERHEAL || skill.getSkillType() == L2SkillType.OVERHEAL_STATIC)
			{
				if (cp < 0)
					cp = 0;
				
				if (cp > 0) // TODO: needs retail confirmation, but technically correct
					target.setCurrentCp(cp + target.getCurrentCp());
			}
			
			target.broadcastStatusUpdate();
			
			if (target instanceof L2PcInstance)
			{
				if (skill.getId() == 4051)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REJUVENATING_HP);
					target.sendPacket(sm);
				}
				else
				{
					if (activeChar instanceof L2PcInstance && activeChar != target)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
						sm.addString(activeChar.getName());
						sm.addNumber((int) hp);
						sm.addHpChange(target.getObjectId(), activeChar.getObjectId(), (int) hp);
						target.sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
						sm.addNumber((int) hp);
						sm.addHpChange(activeChar.getObjectId(), activeChar.getObjectId(), (int) hp);
						target.sendPacket(sm);
					}
					
					if (skill.getSkillType() == L2SkillType.OVERHEAL || skill.getSkillType() == L2SkillType.OVERHEAL_STATIC)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
						//sm.addString(activeChar.getName());
						sm.addNumber((int) cp);
						//sm.addCpChange(target.getObjectId(), activeChar.getObjectId(), (int)cp);
						target.sendPacket(sm);
					}
				}
			}
			
			if (skill.hasEffects())
			{
				//target.stopSkillEffects(skill.getId());
				skill.getEffects(activeChar, target);
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(skill);
				target.sendPacket(sm);
			}
		}
		
		if (skill.hasSelfEffects())
		{
			L2Abnormal effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			// cast self effect if any
			skill.getEffectsSelf(activeChar);
		}
	}
	
	/**
	 *
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
