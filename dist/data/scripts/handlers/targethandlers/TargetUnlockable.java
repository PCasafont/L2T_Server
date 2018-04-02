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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.ChestInstance;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.templates.skills.SkillTargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author One
 */
public class TargetUnlockable implements ISkillTargetTypeHandler {
	/**
	 */
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (!(target instanceof DoorInstance) && !(target instanceof ChestInstance)) {
			//activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
			return null;
		}

		if (onlyFirst == false) {
			targetList.add(target);
			return targetList.toArray(new WorldObject[targetList.size()]);
		} else {
			return new Creature[]{target};
		}
	}

	/**
	 */
	@Override
	public Enum<SkillTargetType> getTargetType() {
		// TODO Auto-generated method stub
		return SkillTargetType.TARGET_UNLOCKABLE;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetUnlockable());
	}
}
