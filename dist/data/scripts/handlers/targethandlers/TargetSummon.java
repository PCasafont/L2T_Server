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
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.skills.SkillTargetDirection;
import l2server.gameserver.templates.skills.SkillTargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nBd
 */
public class TargetSummon implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		if (skill.getTargetDirection() == SkillTargetDirection.ALL_SUMMONS) {
			if (!(activeChar instanceof Player)) {
				return null;
			}

			List<Creature> targetList = new ArrayList<Creature>();

			//LasTravel: Servitor Balance Life should balance owner too
			if (skill.getId() == 11299) {
				targetList.add(activeChar);
			}

			for (Summon summon : ((Player) activeChar).getSummons()) {
				if (!summon.isDead()) {
					targetList.add(summon);
				}
			}

			return targetList.toArray(new Creature[targetList.size()]);
		} else {
			if (!(target instanceof Summon)) {
				target = ((Player) activeChar).getSummon(0);
			}
			if (target != null && !target.isDead() && target instanceof Summon && ((Player) activeChar).getSummons().contains(target)) {
				return new Creature[]{target};
			}
		}

		return null;
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_SUMMON;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetSummon());
	}
}
