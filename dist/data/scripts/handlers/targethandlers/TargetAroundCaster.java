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
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.MonsterInstance;
import l2server.gameserver.model.actor.instance.NpcInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;

/**
 * Used by all skills that affects nearby players around the caster.
 *
 * @author Rewritten by ZaKaX.
 * @author Unhardcodded by nBd.
 */
public class TargetAroundCaster implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		final ArrayList<Creature> result = new ArrayList<Creature>();

		Creature actualCaster = activeChar;
		if (activeChar instanceof NpcInstance && ((NpcInstance) activeChar).getOwner() != null) {
			actualCaster = ((NpcInstance) activeChar).getOwner();
		}
		for (Creature obj : activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
			if (skill.getSkillSafeRadius() != 0) {
				int safeRadius = skill.getSkillSafeRadius();
				int distance = (int) Util.calculateDistance(activeChar.getX(), activeChar.getY(), obj.getX(), obj.getY());
				if (distance < safeRadius) {
					continue;
				}
			}

			if (activeChar == obj) {
				continue;
			} else if (!isReachableTarget(activeChar, obj, skill.getTargetDirection()) || !actualCaster.isAbleToCastOnTarget(obj, skill, true)) {
				continue;
			}

			if (!GeoEngine.getInstance().canSeeTarget(activeChar, obj)) {
				continue;
			}

			if (actualCaster instanceof Player && !((Player) actualCaster).checkPvpSkill(obj, skill)) {
				continue;
			}

			if (result.size() > 20 && skill.getSkillType() != SkillType.AGGDAMAGE) {
				break;
			}

			result.add(obj);
		}

		return result.toArray(new Creature[result.size()]);
	}

	private final boolean isReachableTarget(final Creature activeChar, final Creature target, SkillTargetDirection td) {
		if (activeChar instanceof NpcInstance) {
			final NpcInstance aNpc = (NpcInstance) activeChar;

			if (target instanceof Playable) {
				final Player tPlayer = target.getActingPlayer();

				if (tPlayer == aNpc.getOwner()) {
					return false;
				}
			}
		}

		if (!target.isDead()) {
			if (td == SkillTargetDirection.UNDEAD) {
				if (target.isUndead()) {
					return true;
				}
			} else if (td == SkillTargetDirection.FRONT) {
				if (target.isInFrontOf(activeChar)) {
					return true;
				}
			} else if (td == SkillTargetDirection.BEHIND) {
				if (target.isBehind(activeChar)) {
					return true;
				}
			} else if (td == SkillTargetDirection.DEFAULT || td == SkillTargetDirection.AROUND) {
				return true;
			} else if (td == SkillTargetDirection.PLAYER) {
				if (target instanceof Playable) {
					return true;
				}
			}
		} else {
			if (td == SkillTargetDirection.DEAD_MONSTER) {
				if (target instanceof MonsterInstance && target.isDead()) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_AROUND_CASTER;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAroundCaster());
	}
}
