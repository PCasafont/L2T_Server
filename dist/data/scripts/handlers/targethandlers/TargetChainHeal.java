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

package handlers.targethandlers;

import l2server.gameserver.GeoEngine;
import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Used by all skills that affects nearby players around the target.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetChainHeal implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		final Player aPlayer = activeChar.getActingPlayer();
		final ArrayList<Creature> result = new ArrayList<Creature>();

		// Check for null target or any other invalid target
		if (target == null || target.isDead()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}
		//add self only when targeted self
		if (target == aPlayer) {
			result.add(aPlayer);
		} else {
			if (isReachableTarget(aPlayer, target)) {
				result.add(target);
			} else {
				return null;
			}
		}

		//get objects in radius of target
		for (Creature o : target.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
			if (!GeoEngine.getInstance().canSeeTarget(target, o)) {
				continue;
			}

			final Player kTarget = o.getActingPlayer();
			if (kTarget != null) {
				//dont add self when not targeted self (worked this way on retail))
				if (kTarget == aPlayer) {
					continue;
				}
				final Summon kPet = kTarget.getPet();
				if (kPet != null) {
					if (!isReachableTarget(aPlayer, kPet)) {
						continue;
					}

					result.add(kPet);
				}
			}
			if (isReachableTarget(aPlayer, o)) {
				result.add(o);
			}
		}
		if (result.size() <= 11) //target + 10 allies
		{
			return result.toArray(new Creature[result.size()]);
		} else {
			SortedMap<Double, Creature> map = new TreeMap<Double, Creature>();
			for (Creature obj : result) {
				double percentlost = obj.getCurrentHp() / obj.getMaxHp();
				map.put(percentlost, obj);
			}
			result.clear();
			Iterator<Double> iterator = map.keySet().iterator();
			int i = 0;
			while (iterator.hasNext() && i < 11) {
				Object key = iterator.next();
				i++;
				result.add(map.get(key));
			}
			return result.toArray(new Creature[result.size()]);
		}
	}

	private final boolean isReachableTarget(final Creature activeChar, final Creature target) {
		if (target.isDead()) {
			return false;
		}

		if (target instanceof Playable) {
			final Player pTarget = target.getActingPlayer();

			if (pTarget.isPlayingEvent()) {
				if (!((Player) activeChar).isPlayingEvent()) {
					return false;
				}

				if (pTarget.getEvent().getConfig().isAllVsAll()) {
					return false;
				}

				if (pTarget.getEvent().getParticipantTeamId(pTarget.getObjectId()) !=
						pTarget.getEvent().getParticipantTeamId(activeChar.getObjectId())) {
					return false;
				}
			}

			if (activeChar instanceof Player) {
				final Player player = (Player) activeChar;
				if (player.getDuelId() != 0) {
					if (((Player) activeChar).getDuelId() != pTarget.getDuelId()) {
						return false;
					}
				}

				if (((Player) activeChar).isInSameClanWar(pTarget) || ((Player) activeChar).isInOlympiadMode()) {
					return false;
				}

				if (player.isInSameParty(pTarget) || player.isInSameChannel(pTarget) || player.isInSameClan(pTarget) ||
						player.isInSameAlly(pTarget)) {
					return true;
				}
			} else if (pTarget.getDuelId() != 0) {
				return false;
			}
			if (pTarget.isAvailableForCombat() || pTarget.isInsidePvpZone()) {
				return false;
			}
			if (target.isInsideZone(Creature.ZONE_TOWN)) {
				return true;
			}
		} else if (target instanceof NpcInstance) {
			final NpcInstance npc = (NpcInstance) target;
			if (!npc.isInsideZone(Creature.ZONE_TOWN)) {
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_CHAIN_HEAL;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetChainHeal());
	}
}
