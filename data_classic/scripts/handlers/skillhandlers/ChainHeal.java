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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import l2server.Config;
import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2NpcInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.L2SkillType;
import l2server.util.ValueSortMap;

/**
 * @author Nik
 * @author UnAfraid
 */

public class ChainHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {L2SkillType.CHAIN_HEAL};

	/**
	 * @see ISkillHandler#useSkill(L2Character, L2Skill, L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(L2SkillType.BUFF);

		if (handler != null)
		{
			handler.useSkill(activeChar, skill, targets);
		}
		SystemMessage sm;

		double amount = 0;

		L2Character[] characters = getTargetsToHeal((L2Character[]) targets, activeChar, skill.getSkillRadius());

		double power = skill.getPower();
		// Healing critical, since CT2.3 Gracia Final
		if (Formulas.calcMCrit(activeChar.getMCriticalHit(activeChar, skill)))
		{
			activeChar.sendMessage("Healing critical!");
			power *= 2;
		}

		if (Config.isServer(Config.TENKAI) && activeChar instanceof L2PcInstance && activeChar.isInParty())
		{
			int classId = ((L2PcInstance) activeChar).getCurrentClass().getParent().getAwakeningClassId();
			int members = 0;
			for (L2PcInstance partyMember : activeChar.getParty().getPartyMembers())
			{
				if (partyMember.getCurrentClass().getParent() != null &&
						partyMember.getCurrentClass().getParent().getAwakeningClassId() == classId)
				{
					members++;
				}
			}

			if (members > 1)
			{
				power /= members;
			}
		}

		if (power > 100.0)
		{
			power = 100.0;
		}

		// Get top 10 most damaged and iterate the heal over them
		for (L2Character character : characters)
		{
			//1505 - sublime self sacrifice
			if ((character == null || character.isDead() || character.isInvul(activeChar)) && skill.getId() != 1505)
			{
				continue;
			}

			if (character != activeChar && character.getFaceoffTarget() != null &&
					character.getFaceoffTarget() != activeChar)
			{
				continue;
			}

			amount = character.getMaxHp() * power / 100.0;

			amount = Math.min(amount, character.getMaxHp() - character.getCurrentHp());
			amount *= character.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			amount = Math.min(character.calcStat(Stats.GAIN_HP_LIMIT, character.getMaxHp(), null, null),
					character.getCurrentHp() + amount) - character.getCurrentHp();
			if (amount < 0)
			{
				amount = 0;
			}

			character.setCurrentHp(character.getCurrentHp() + amount);

			if (activeChar != character)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
				sm.addCharName(activeChar);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
			}

			sm.addNumber((int) amount);
			sm.addHpChange(activeChar.getObjectId(), activeChar.getObjectId(), (int) amount);
			character.sendPacket(sm);

			character.broadcastStatusUpdate();

			if (activeChar instanceof L2PcInstance && character instanceof L2PcInstance)
			{
				if (((L2PcInstance) activeChar).getPvpFlag() == 0 && ((L2PcInstance) character).getPvpFlag() > 0)
				{
					((L2PcInstance) activeChar).updatePvPStatus();
				}

				PlayerAssistsManager.getInstance().updateHelpTimer((L2PcInstance) activeChar, (L2PcInstance) character);
			}

			power -= 3;
		}
	}

	private L2Character[] getTargetsToHeal(L2Character[] targets, L2Character caster, int skillRadius)
	{
		Map<L2Character, Double> tmpTargets = new LinkedHashMap<L2Character, Double>();

		List<L2Character> sortedListToReturn = new LinkedList<L2Character>();

		if (caster.getTarget() == caster)
		{
			tmpTargets.put(caster, caster.getCurrentHp() / caster.getMaxHp());
		}

		for (L2Character target : caster.getKnownList().getKnownCharactersInRadius(skillRadius))
		{
			//caster.sendMessage("Trying to add " + target.getName());
			if (canAddCharacter(caster, target, tmpTargets.size(), skillRadius))
			{
				double hpPercent = target.getCurrentHp() / target.getMaxHp();
				tmpTargets.put(target, hpPercent);

				//caster.sendMessage("Added " + target.getName());
			}
		}

		// Sort in ascending order then add the values to the list
		ValueSortMap.sortMapByValue(tmpTargets, true);

		sortedListToReturn.addAll(tmpTargets.keySet());

		return sortedListToReturn.toArray(new L2Character[sortedListToReturn.size()]);
	}

	private static boolean canAddCharacter(L2Character caster, L2Character target, int listZie, int skillRadius)
	{
		if (listZie >= 10)
		{
			return false;
		}

		if (target.isDead())
		{
			return false;
		}

		if (!(caster instanceof L2PcInstance))
		{
			return false;
		}

		final L2PcInstance activeChar = (L2PcInstance) caster;

		if (target instanceof L2Playable)
		{
			final L2PcInstance pTarget = target.getActingPlayer();

			if (!pTarget.isVisible())
			{
				return false;
			}

			if (activeChar.isPlayingEvent())
			{
				if (!pTarget.isPlayingEvent())
				{
					return false;
				}
				else if (activeChar.getTeamId() != 0 && activeChar.getTeamId() != pTarget.getTeamId())
				{
					return false;
				}

				return true;
			}
			else if (pTarget.isPlayingEvent())
			{
				return false;
			}

			if (activeChar.getDuelId() != 0)
			{
				if (activeChar.getDuelId() != pTarget.getDuelId())
				{
					return false;
				}
			}
			else if (pTarget.getDuelId() != 0)
			{
				return false;
			}
			if (activeChar.isInSameParty(pTarget) || activeChar.isInSameChannel(pTarget) ||
					activeChar.isInSameClan(pTarget) || activeChar.isInSameAlly(pTarget))
			{
				return true;
			}
			if (pTarget.isAvailableForCombat() || pTarget.isInsidePvpZone() || activeChar.isInSameClanWar(pTarget))
			{
				return false;
			}
			if (target.isInsideZone(L2Character.ZONE_TOWN))
			{
				return true;
			}
		}
		else if (target instanceof L2NpcInstance)
		{
			final L2NpcInstance npc = (L2NpcInstance) target;
			if (!npc.isInsideZone(L2Character.ZONE_TOWN))
			{
				return false;
			}
		}
		else
		{
			return false;
		}

		return true;
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
