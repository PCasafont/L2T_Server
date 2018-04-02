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
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.taskmanager.DecayTaskManager;
import l2server.gameserver.templates.skills.SkillTargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nBd
 */
public class TargetCorpseMob implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (!(target instanceof Attackable) || !target.isDead()) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		// Corpse mob only available for half time
		switch (skill.getSkillType()) {
			case DRAIN:
			case SUMMON: {
				if (DecayTaskManager.getInstance().getTasks().containsKey(target) &&
						System.currentTimeMillis() - DecayTaskManager.getInstance().getTasks().get(target) >
								DecayTaskManager.ATTACKABLE_DECAY_TIME / 2) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED));
					return null;
				}
				break;
			}
			default:
		}

		if (!onlyFirst) {
			targetList.add(target);
			return targetList.toArray(new WorldObject[targetList.size()]);
		} else {
			return new Creature[]{target};
		}
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_CORPSE_MOB;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetCorpseMob());
	}
}
