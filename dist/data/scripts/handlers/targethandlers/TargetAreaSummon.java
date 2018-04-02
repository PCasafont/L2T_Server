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

import l2server.gameserver.handler.ISkillTargetTypeHandler;
import l2server.gameserver.handler.SkillTargetTypeHandler;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Attackable;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sandro
 */
public class TargetAreaSummon implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();
		// FIXME target = activeChar.getPet();
		if (target == null || !(target instanceof SummonInstance) || target.isDead()) {
			return null;
		}

		if (onlyFirst) {
			return new Creature[]{target};
		}

		final Collection<Creature> objs = target.getKnownList().getKnownCharacters();
		final int radius = skill.getSkillRadius();

		for (Creature obj : objs) {
			if (obj == null || obj == target || obj == activeChar) {
				continue;
			}

			if (!Util.checkIfInRange(radius, target, obj, true)) {
				continue;
			}

			if (!(obj instanceof Attackable || obj instanceof Playable)) {
				continue;
			}

			targetList.add(obj);
		}

		if (targetList.isEmpty()) {
			return null;
		}

		return targetList.toArray(new Creature[targetList.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_AREA_SUMMON;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAreaSummon());
	}
}
