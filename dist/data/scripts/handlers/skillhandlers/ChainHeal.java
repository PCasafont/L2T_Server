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
import l2server.gameserver.handler.SkillHandler;
import l2server.gameserver.instancemanager.PlayerAssistsManager;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.CreatureZone;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.ValueSortMap;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Nik
 * @author UnAfraid
 */

public class ChainHeal implements ISkillHandler {
	private static final SkillType[] SKILL_IDS = {SkillType.CHAIN_HEAL};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		//check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);

		if (handler != null) {
			handler.useSkill(activeChar, skill, targets);
		}
		SystemMessage sm;

		double amount = 0;

		Creature[] characters = getTargetsToHeal((Creature[]) targets, activeChar, skill.getSkillRadius());

		double power = skill.getPower();
		// Healing critical, since CT2.3 Gracia Final
		if (Formulas.calcMCrit(activeChar.getMCriticalHit(activeChar, skill))) {
			activeChar.sendMessage("Healing critical!");
			power *= 2;
		}

		if (Config.isServer(Config.TENKAI) && activeChar instanceof Player && activeChar.isInParty()) {
			int classId = ((Player) activeChar).getCurrentClass().getParent().getAwakeningClassId();
			int members = 0;
			for (Player partyMember : activeChar.getParty().getPartyMembers()) {
				if (partyMember.getCurrentClass().getParent() != null && partyMember.getCurrentClass().getParent().getAwakeningClassId() == classId) {
					members++;
				}
			}

			if (members > 1) {
				power /= members;
			}
		}

		if (power > 100.0) {
			power = 100.0;
		}

		// Get top 10 most damaged and iterate the heal over them
		for (Creature character : characters) {
			//1505 - sublime self sacrifice
			if ((character == null || character.isDead() || character.isInvul(activeChar)) && skill.getId() != 1505) {
				continue;
			}

			if (character != activeChar && character.getFaceoffTarget() != null && character.getFaceoffTarget() != activeChar) {
				continue;
			}

			amount = character.getMaxHp() * power / 100.0;

			amount = Math.min(amount, character.getMaxHp() - character.getCurrentHp());
			amount *= character.calcStat(Stats.HEAL_EFFECTIVNESS, 100, null, null) / 100;
			amount = Math.min(character.calcStat(Stats.GAIN_HP_LIMIT, character.getMaxHp(), null, null), character.getCurrentHp() + amount) -
					character.getCurrentHp();
			if (amount < 0) {
				amount = 0;
			}

			character.setCurrentHp(character.getCurrentHp() + amount);

			if (activeChar != character) {
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
				sm.addCharName(activeChar);
			} else {
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
			}

			sm.addNumber((int) amount);
			sm.addHpChange(activeChar.getObjectId(), activeChar.getObjectId(), (int) amount);
			character.sendPacket(sm);

			character.broadcastStatusUpdate();

			if (activeChar instanceof Player && character instanceof Player) {
				if (((Player) activeChar).getPvpFlag() == 0 && ((Player) character).getPvpFlag() > 0) {
					((Player) activeChar).updatePvPStatus();
				}

				PlayerAssistsManager.getInstance().updateHelpTimer((Player) activeChar, (Player) character);
			}

			power -= 3;
		}
	}

	private Creature[] getTargetsToHeal(Creature[] targets, Creature caster, int skillRadius) {
		Map<Creature, Double> tmpTargets = new LinkedHashMap<Creature, Double>();

		List<Creature> sortedListToReturn = new LinkedList<Creature>();

		if (caster.getTarget() == caster) {
			tmpTargets.put(caster, caster.getCurrentHp() / caster.getMaxHp());
		}

		for (Creature target : caster.getKnownList().getKnownCharactersInRadius(skillRadius)) {
			//caster.sendMessage("Trying to add " + target.getName());
			if (canAddCharacter(caster, target, tmpTargets.size(), skillRadius)) {
				double hpPercent = target.getCurrentHp() / target.getMaxHp();
				tmpTargets.put(target, hpPercent);

				//caster.sendMessage("Added " + target.getName());
			}
		}

		// Sort in ascending order then add the values to the list
		ValueSortMap.sortMapByValue(tmpTargets, true);

		sortedListToReturn.addAll(tmpTargets.keySet());

		return sortedListToReturn.toArray(new Creature[sortedListToReturn.size()]);
	}

	private static boolean canAddCharacter(Creature caster, Creature target, int listZie, int skillRadius) {
		if (listZie >= 10) {
			return false;
		}

		if (target.isDead()) {
			return false;
		}

		if (!(caster instanceof Player)) {
			return false;
		}

		final Player activeChar = (Player) caster;

		if (target instanceof Playable) {
			final Player pTarget = target.getActingPlayer();

			if (!pTarget.isVisible()) {
				return false;
			}

			if (activeChar.isPlayingEvent()) {
				if (!pTarget.isPlayingEvent()) {
					return false;
				} else if (activeChar.getTeamId() != 0 && activeChar.getTeamId() != pTarget.getTeamId()) {
					return false;
				}

				return true;
			} else if (pTarget.isPlayingEvent()) {
				return false;
			}

			if (activeChar.getDuelId() != 0) {
				if (activeChar.getDuelId() != pTarget.getDuelId()) {
					return false;
				}
			} else if (pTarget.getDuelId() != 0) {
				return false;
			}
			if (activeChar.isInSameParty(pTarget) || activeChar.isInSameChannel(pTarget) || activeChar.isInSameClan(pTarget) ||
					activeChar.isInSameAlly(pTarget)) {
				return true;
			}
			if (pTarget.isAvailableForCombat() || pTarget.isInsidePvpZone() || activeChar.isInSameClanWar(pTarget)) {
				return false;
			}
			if (activeChar.isInSameOlympiadGame(pTarget) || activeChar.isInSameClan(pTarget)) {
				return false;
			}
			if (target.isInsideZone(CreatureZone.ZONE_TOWN)) {
				return true;
			}
		} else if (target instanceof NpcInstance) {
			final NpcInstance npc = (NpcInstance) target;
			if (!npc.isInsideZone(CreatureZone.ZONE_TOWN)) {
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
