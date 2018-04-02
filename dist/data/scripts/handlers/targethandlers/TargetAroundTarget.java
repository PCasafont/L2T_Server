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
import l2server.gameserver.model.actor.Trap;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillBehaviorType;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;

/**
 * Used by all skills that affects nearby players around the target.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetAroundTarget implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		final ArrayList<Creature> result = new ArrayList<Creature>();
		final Player src = activeChar.getActingPlayer();
		boolean isAttackingPlayer = false;

		if (activeChar == target || target == null ||
				src != null && (!isReachableTarget(activeChar, target, skill, false) || !src.isAbleToCastOnTarget(target, skill, false))) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return null;
		} else {
			if (target instanceof Playable) {
				isAttackingPlayer = true;
			}

			result.add(target);
		}

		if (target instanceof Playable) {
			for (Creature obj : target.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
				if (!isReachableTarget(activeChar, obj, skill, true) || !activeChar.isAbleToCastOnTarget(obj, skill, true)) {
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj)) {
					continue;
				}

				result.add(obj);
			}
		} else {
			for (WorldObject obj : activeChar.getKnownList().getKnownObjects().values()) {
				if (activeChar == obj || obj == target || !(obj instanceof Creature) || !isAttackingPlayer && obj instanceof Playable) {
					continue;
				}

				if (!Util.checkIfInRange(skill.getSkillRadius(), obj, target, true)) {
					continue;
				}

				if (!isReachableTarget(target, (Creature) obj, skill, true) || !activeChar.isAbleToCastOnTarget(obj, skill, true)) {
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj)) {
					continue;
				}

				result.add((Creature) obj);
			}
		}

		return result.toArray(new Creature[result.size()]);
	}

	private final boolean isReachableTarget(final Creature activeChar,
	                                        final Creature target,
	                                        final Skill skill,
	                                        final boolean isMassiveCheck) {
		if (target instanceof Trap) {
			return false;
		}

		final SkillTargetDirection td = skill.getTargetDirection();

		if (td == SkillTargetDirection.DEAD_MONSTER) {
			if (target instanceof MonsterInstance) {
				if (skill.getSkillBehavior() == SkillBehaviorType.ATTACK) {
					if (!isMassiveCheck && !target.isDead()) {
						return false;
					}
				} else {
					if (!target.isDead()) {
						return false;
					}
				}

				return true;
			}
		} else {
			if (!target.isDead()) {
				if (td == SkillTargetDirection.UNDEAD) {
					if (target.isUndead()) {
						return true;
					}
				}

				if (isMassiveCheck) {
					if (td == SkillTargetDirection.FRONT) {
						if (activeChar.isFacing(target, 180)) {
							return true;
						}
						//else
						//	Broadcast.toGameMasters(target.getName() + " was unreachable");
					} else if (td == SkillTargetDirection.BEHIND) {
						if (!target.isFacing(activeChar, 140)) {
							return true;
						}
					} else if (td == SkillTargetDirection.DEFAULT || td == SkillTargetDirection.AROUND) {
						return true;
					} else if (td == SkillTargetDirection.PLAYER) {
						if (target instanceof Playable) {
							return true;
						}
					} else if (td == SkillTargetDirection.ALL_SUMMONS) {
						if (target instanceof Summon) {
							return true;
						}
					}
				} else {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_AROUND_TARGET;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAroundTarget());
	}
}
