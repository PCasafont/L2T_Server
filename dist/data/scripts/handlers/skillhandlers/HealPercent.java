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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SiegeFlagInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Formulas;
import l2server.gameserver.stats.Stats;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.util.ValueSortMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealPercent implements ISkillHandler {
	private static final SkillType[] SKILL_IDS =
			{SkillType.HEAL_PERCENT, SkillType.MANAHEAL_PERCENT, SkillType.CPHEAL_PERCENT, SkillType.HPMPHEAL_PERCENT,
			 SkillType.HPMPCPHEAL_PERCENT, SkillType.HPCPHEAL_PERCENT};

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

		double chainHp = 0;
		// Sort by most injured targets now.
		if (skill.getTargetDirection() == SkillTargetDirection.CHAIN_HEAL) {
			Map<Creature, Double> tmpTargets = new HashMap<Creature, Double>();

			List<Creature> sortedListToReturn = new ArrayList<Creature>();

			//activeChar.sendMessage("Main Target = " + activeChar.getTarget().getName());

			Creature currentTarget = (Creature) activeChar.getTarget();

			if (currentTarget instanceof Playable) {
				tmpTargets.put(currentTarget, 150.000);
			} else {
				currentTarget = activeChar;
			}

			chainHp = currentTarget.getMaxHp() * skill.getPower() / 100.0;
			for (Creature target : (Creature[]) targets) {
				double hpPercent = target.getCurrentHp() / target.getMaxHp();

				if (!tmpTargets.containsKey(target)) {
					tmpTargets.put(target, hpPercent);
				}
			}

			if (!tmpTargets.containsKey(activeChar)) {
				tmpTargets.put(activeChar, activeChar.getCurrentHp() / activeChar.getMaxHp());
			}

			ValueSortMap.sortMapByValue(tmpTargets, true);

			sortedListToReturn.addAll(tmpTargets.keySet());

			targets = sortedListToReturn.toArray(new Creature[sortedListToReturn.size()]);

			/*
			int i = 0;
			for (Creature target: (Creature[]) targets)
			{
				activeChar.sendMessage(++i + " - " + target.getName());
			}*/
		}

		boolean cp = false;
		boolean hp = false;
		boolean mp = false;
		switch (skill.getSkillType()) {
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
			default:
		}

		//StatusUpdate su = null;
		SystemMessage sm;
		double amount = 0;
		double percent = skill.getPower();
		// Healing critical, since CT2.3 Gracia Final
		if (skill.getCritChance() != -1 && skill.getTargetType() != SkillTargetType.TARGET_SELF && !skill.isPotion() &&
				Formulas.calcMCrit(activeChar.getMCriticalHit(activeChar, skill))) {
			activeChar.sendMessage("Healing critical!");
			percent *= 2;
		}

		if (Config.isServer(Config.TENKAI) && activeChar instanceof Player && activeChar.isInParty() &&
				(skill.getTargetType() == SkillTargetType.TARGET_FRIENDS || skill.getTargetType() == SkillTargetType.TARGET_FRIEND_NOTME)) {
			int classId = ((Player) activeChar).getCurrentClass().getParent().getAwakeningClassId();
			int members = 0;
			for (Player partyMember : activeChar.getParty().getPartyMembers()) {
				if (partyMember.getCurrentClass().getParent().getAwakeningClassId() == classId) {
					members++;
				}
			}

			if (members > 1) {
				percent /= members;
			}
		}

		if (percent > 100.0) {
			percent = 100.0;
		}

		boolean full = percent == 100.0;
		boolean targetPlayer = false;

		for (Creature target : (Creature[]) targets) {
			//1505  - sublime self sacrifice
			//11560 - celestial aegis
			if ((target == null || target.isDead() || target.isInvul(activeChar)) && skill.getId() != 1505 && skill.getId() != 11560) {
				continue;
			}

			if (target != activeChar && target.getFaceoffTarget() != null && target.getFaceoffTarget() != activeChar) {
				continue;
			}

			if (skill.getId() == 11828) {
				percent *= 0.98;
			}

			targetPlayer = target instanceof Player;

			if (target != activeChar) {
				// Cursed weapon owner can't heal or be healed
				if (activeChar instanceof Player && ((Player) activeChar).isCursedWeaponEquipped()) {
					continue;
				}
				if (targetPlayer && ((Player) target).isCursedWeaponEquipped()) {
					continue;
				}

				// Nor all vs all event player
				if (activeChar instanceof Player && ((Player) activeChar).isPlayingEvent() &&
						((Player) activeChar).getEvent().getConfig().isAllVsAll() &&
						!(target instanceof Summon && ((Summon) target).getOwner() == activeChar)) {
					continue;
				}
			}

			// Doors and flags can't be healed in any way
			if (hp && (target instanceof DoorInstance || target instanceof SiegeFlagInstance)) {
				continue;
			}

			// Only players have CP
			if (cp && targetPlayer) {
				if (full) {
					amount = target.getMaxCp();
				} else {
					amount = target.getMaxCp() * percent / 100.0;
				}

				if (skill.getId() == 10270) // Second Wind
				{
					amount = target.getMaxCp() * 15.0 / 100.0;
				}

				amount = Math.min(amount, target.getMaxCp() - target.getCurrentCp());
				target.setCurrentCp(amount + target.getCurrentCp());

				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
				sm.addNumber((int) amount);
				target.sendPacket(sm);
			}

			if (hp) {
				if (full) {
					amount = target.getMaxHp();
				} else {
					amount = target.getMaxHp() * percent / 100.0;
				}

				amount = Math.min(amount, target.getMaxHp() - target.getCurrentHp());

				if (chainHp != 0) {
					amount = chainHp *= 0.98;
				}

				/*
				if (skill.getId() == 11828) // Progressive Heal
				{
					if (activeChar.getName().equals("Chuter"))
					{
						System.out.println("target.getMaxHp() * 0.4 = " + target.getMaxHp() * 0.4);
						System.out.println("target.getMaxHp() * 0.4 - target.getCurrentHp() = " + (target.getMaxHp() * 0.4 - target.getCurrentHp()));
						System.out.println("target.getMaxHp() = " + target.getMaxHp());
					}

					if (amount > target.getMaxHp() * 0.4 - target.getCurrentHp())
						amount = target.getMaxHp() * 0.4 - target.getCurrentHp();
				}

				if (amount < 0)
					continue;*/

				amount = Math.min(target.calcStat(Stats.GAIN_HP_LIMIT, target.getMaxHp(), null, null), target.getCurrentHp() + amount) -
						target.getCurrentHp();
				if (amount < 0) {
					amount = 0;
				}

				//activeChar.sendMessage("Giving " + chainHp + " to " + target.getName());
				target.setCurrentHp(amount + target.getCurrentHp());

				if (targetPlayer) {
					if (activeChar != target) {
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
						sm.addCharName(activeChar);
					} else {
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
					}
					sm.addNumber((int) amount);
					sm.addHpChange(target.getObjectId(), activeChar.getObjectId(), (int) amount);
					target.sendPacket(sm);
				}
			}

			if (mp) {
				if (full) {
					amount = target.getMaxMp();
				} else {
					amount = target.getMaxMp() * percent / 100.0;
				}

				amount = Math.min(amount, target.getMaxMp() - target.getCurrentMp());
				target.setCurrentMp(amount + target.getCurrentMp());

				if (targetPlayer) {
					if (activeChar != target) {
						sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
						sm.addCharName(activeChar);
					} else {
						sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
					}
					sm.addNumber((int) amount);
					target.sendPacket(sm);
				}
			}

			target.broadcastStatusUpdate();
		}

		if (skill.isSuicideAttack()) {
			activeChar.doDie(activeChar);
		}
	}

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public SkillType[] getSkillIds() {
		return SKILL_IDS;
	}
}
