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
import l2server.gameserver.model.actor.instance.TrapInstance;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;

import java.util.ArrayList;

/**
 * Used by all skills that are used by Traps.
 *
 * @author ZaKaX.
 */
public class TrapTarget implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		TrapInstance aTrap = activeChar instanceof TrapInstance ? (TrapInstance) activeChar : null;

		if (aTrap == null) {
			return null;
		}

		final ArrayList<Creature> result = new ArrayList<Creature>();

		for (Creature o : aTrap.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius())) {
			if (o == aTrap.getOwner() || o.isDead()) {
				continue;
			}

			if (!aTrap.getOwner().isAbleToCastOnTarget(o, skill, skill.isUseableWithoutTarget())) {
				continue;
			} else if (!GeoEngine.getInstance().canSeeTarget(aTrap, o)) {
				continue;
			}

			if (skill.getTargetDirection() == SkillTargetDirection.SINGLE) {
				return new Creature[]{o};
			}

			result.add(o);
		}

		return result.toArray(new Creature[result.size()]);
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TRAP_TARGET;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TrapTarget());
	}
}
