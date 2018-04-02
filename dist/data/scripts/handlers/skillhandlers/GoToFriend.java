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

import l2server.gameserver.handler.ISkillHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.templates.skills.SkillType;
import l2server.gameserver.util.Util;

/**
 * @author Pere
 */
public class GoToFriend implements ISkillHandler {
	//private static Logger log = Logger.getLogger(SummonFriend.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.GO_TO_FRIEND};

	/**
	 * @see l2server.gameserver.handler.ISkillHandler#useSkill(Creature, Skill, WorldObject[])
	 */
	@Override
	public void useSkill(Creature activeChar, Skill skill, WorldObject[] targets) {
		for (Creature target : (Creature[]) targets) {
			if (activeChar == target) {
				continue;
			}

			if (Util.checkIfInRange(1000, activeChar, target, false)) {
				activeChar.teleToLocation(target.getX(), target.getY(), target.getZ(), true);
				return;
			}
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
