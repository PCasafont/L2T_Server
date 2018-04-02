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
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.skills.SkillTargetType;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author nBd
 */
public class TargetMultiface implements ISkillTargetTypeHandler {
	@Override
	public WorldObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
		List<Creature> targetList = new ArrayList<Creature>();

		if (!(target instanceof Attackable) && !(target instanceof Player)) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return null;
		}

		if (onlyFirst == false) {
			targetList.add(target);
		} else {
			return new Creature[]{target};
		}

		int radius = skill.getSkillRadius();

		Collection<WorldObject> objs = activeChar.getKnownList().getKnownObjects().values();
		//synchronized (activeChar.getKnownList().getKnownObjects())
		{
			for (WorldObject obj : objs) {
				if (!Util.checkIfInRange(radius, activeChar, obj, true)) {
					continue;
				}

				if (obj instanceof Attackable && obj != target) {
					targetList.add((Creature) obj);
				}

				if (targetList.size() == 0) {
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
					return null;
				}
			}
		}
		return targetList.toArray(new Creature[targetList.size()]);
		//TODO multiface targets all around right now.  need it to just get targets the character is facing.
	}

	@Override
	public Enum<SkillTargetType> getTargetType() {
		return SkillTargetType.TARGET_MULTIFACE;
	}

	public static void main(String[] args) {
		SkillTargetTypeHandler.getInstance().registerSkillTargetType(new TargetMultiface());
	}
}
