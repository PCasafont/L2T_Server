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
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetAreaUndead implements ISkillTargetTypeHandler {
	/**
	 */
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		Creature cha;

		int radius = skill.getSkillRadius();

		if (skill.getCastRange() >= 0 && (target instanceof Npc || target instanceof SummonInstance) && target.isUndead() &&
				!target.isAlikeDead()) {
			cha = target;

			if (onlyFirst == false) {
				targetList.add(cha); // Add target to target list
			} else {
				return new Creature[]{cha};
			}
		} else {
			cha = activeChar;
		}

		Collection<WorldObject> objs = cha.getKnownList().getKnownObjects().values();
		//synchronized (cha.getKnownList().getKnownObjects())
		{
			for (WorldObject obj : objs) {
				if (obj instanceof Npc) {
					target = (Npc) obj;
				} else if (obj instanceof SummonInstance) {
					target = (SummonInstance) obj;
				} else {
					continue;
				}

				if (!GeoEngine.getInstance().canSeeTarget(activeChar, target)) {
					continue;
				}

				if (!target.isAlikeDead()) // If target is not dead/fake death and not self
				{
					if (!target.isUndead()) {
						continue;
					}

					if (!Util.checkIfInRange(radius, cha, obj, true)) {
						continue;
					}

					if (onlyFirst == false) {
						targetList.add((Creature) obj);
					} else {
						return new Creature[]{(Creature) obj};
					}
				}
			}
		}

		if (targetList.size() == 0) {
			return null;
		}

		return targetList.toArray(new Creature[targetList.size()]);
	}

	/**
	 */
	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_AREA_UNDEAD;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetAreaUndead());
	}
}
