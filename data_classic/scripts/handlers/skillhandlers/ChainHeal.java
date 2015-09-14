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

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import l2server.gameserver.GeoData;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2BabyPetInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.model.actor.instance.L2SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.ValueSortMap;

/**
 * 
 * @author Nik
 * @author UnAfraid
 *
 */

public class ChainHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = 
	{ 
		L2SkillType.CHAIN_HEAL 
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
		SystemMessage sm;
		
		double amount = 0;
		
		L2Character[] characters = getTargetsToHeal((L2Character[])targets, (L2PcInstance)activeChar, skill.getSkillRadius());
		
		double power = skill.getPower();
		// Healing critical, since CT2.3 Gracia Final
		if (Formulas.calcMCrit(activeChar.getMCriticalHit(activeChar, skill)))
		{
			activeChar.sendMessage("Healing critical!");
			power *= 2;
		}
		
		if (power > 100.0)
			power = 100.0;
		
		// Get top 10 most damaged and iterate the heal over them
		for (L2Character character : characters)
		{
			//1505 - sublime self sacrifice
			if ((character == null || character.isDead() || character.isInvul()) && skill.getId() != 1505)
				continue;
			
			if (character != activeChar && character.getFaceoffTarget() != null && character.getFaceoffTarget() != activeChar)
				continue;
			
			if (power == 100.0)
				amount = character.getMaxHp();
			else
				amount = character.getMaxHp() * power / 100.0;
			
			amount = Math.min(amount, character.getMaxHp() - character.getCurrentHp());
			
			amount *= character.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			
			if (amount < 0)
				amount = 0;

			amount = Math.min(character.calcStat(Stats.GAIN_HP_LIMIT, character.getMaxHp(), null, null), character.getCurrentHp() + amount) - character.getCurrentHp();
			character.setCurrentHp(character.getCurrentHp() + amount);
			
			if (activeChar != character)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
				sm.addCharName(activeChar);
			}
			else
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
			
			sm.addNumber((int)amount);
			sm.addHpChange(activeChar.getObjectId(), activeChar.getObjectId(), (int)amount);
			character.sendPacket(sm);
			
			character.broadcastStatusUpdate();
			
			if (activeChar instanceof L2PcInstance && character instanceof L2PcInstance)
			{
				if (((L2PcInstance)activeChar).getPvpFlag() == 0
						&& ((L2PcInstance)character).getPvpFlag() > 0)
					((L2PcInstance)activeChar).updatePvPStatus();
				
				PlayerAssistsManager.getInstance().updateHelpTimer((L2PcInstance)activeChar, (L2PcInstance)character);
			}
			
			power -= 3;
		}
	}
	
	private L2Character[] getTargetsToHeal(L2Character[] targets, L2PcInstance caster, int skillRadius)
	{
		Map<L2Character, Double> tmpTargets = new FastMap<L2Character, Double>();
		
		List<L2Character> sortedListToReturn = new FastList<L2Character>();

		if (caster.getTarget() == caster)
			tmpTargets.put(caster, caster.getCurrentHp() / caster.getMaxHp());
				
		for (L2Character target : caster.getKnownList().getKnownCharactersInRadius(skillRadius))
		{
			if (canAddCharacter(caster, target, tmpTargets.size(), skillRadius))
			{
				double hpPercent = target.getCurrentHp() / target.getMaxHp();
				tmpTargets.put(target, hpPercent);
			}
		}

		// Sort in ascending order then add the values to the list
		ValueSortMap.sortMapByValue(tmpTargets, true);
		
		sortedListToReturn.addAll(tmpTargets.keySet());
		
		return sortedListToReturn.toArray(new L2Character[sortedListToReturn.size()]);
	}
	
	private static boolean canAddCharacter(L2PcInstance caster, L2Character target, int listZie, int skillRadius)
	{
		if (target == null)
			return false;
		
		if (!(target instanceof L2Playable))
			return false;
		
		if (listZie >= 10)
			return false;
		
		if (caster.isInOlympiadMode() || target.isDead() || target.isInvul() || !target.isInsideRadius(caster, skillRadius, false, false) || target.getMaxHp() == target.getCurrentHp() || !GeoData.getInstance().canSeeTarget(caster, target))
			return false;
		
		L2PcInstance targetPlayer = null;
			
		if (target instanceof L2PcInstance)
			targetPlayer = (L2PcInstance)target;
		else if (target instanceof L2SummonInstance)
			targetPlayer = ((L2SummonInstance) target).getOwner();
		else if (target instanceof L2PetInstance)
			targetPlayer = ((L2PetInstance) target).getOwner();
		else if (target instanceof L2BabyPetInstance)
			targetPlayer = ((L2BabyPetInstance)target).getOwner();
			
		if (targetPlayer == null)
			return false;
		
		if (targetPlayer.isCursedWeaponEquipped())
			return false;
		
		//Event case, if caster are at same team than the target and the event is not all vs all, target are automatically added
		if (caster.getEvent() != null)
		{
			if (targetPlayer.getEvent() != null && caster.getEvent().getParticipantTeamId(caster.getObjectId()) != targetPlayer.getEvent().getParticipantTeamId(targetPlayer.getObjectId()) ||
					(caster.getEvent() != null && caster.getEvent().getConfig().isAllVsAll() && caster.getEvent().isState(EventState.STARTED)))
				return false;
			return true;
		}
			
		//Players at same clan/ally are automatically added
		if (caster.getClan() != null && targetPlayer.getClan() != null)
		{
			if (caster.getClan().getClanId() == targetPlayer.getClan().getClanId())
				return true;
				
			if (caster.getClan().getAllyId() > 0 && targetPlayer.getClan().getAllyId() > 0 && caster.getClan().getAllyId() == targetPlayer.getClan().getAllyId())
				return true;
		}
			
		//Players at same party/ch are automatically added 
		if (caster.isInParty() && targetPlayer.isInParty())
		{
			if (caster.getParty().getPartyLeaderOID() == targetPlayer.getParty().getPartyLeaderOID())
				return true;
			
			if (caster.getParty().isInCommandChannel() && targetPlayer.getParty().isInCommandChannel() && caster.getParty().getCommandChannel().getChannelLeader() == targetPlayer.getParty().getCommandChannel().getChannelLeader())
				return true;
		}

		//Players from different clans that are at clan war are automatically discarded
		/*if (targetPlayer.getClan() != null && caster.getClan() != null && caster.getClan().getWarList().contains(targetPlayer.getClan()))
			return false;

		//At this point the target is not at same clan/ally/party so if is flagged is automatically discarded
		if (targetPlayer.getPvpFlag() > 0 || targetPlayer.getReputation() < 0)
			return false;*/
		
		return false;
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