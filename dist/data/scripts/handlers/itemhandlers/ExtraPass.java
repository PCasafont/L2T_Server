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

package handlers.itemhandlers;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.stats.SkillHolder;

import java.util.Map;

public class ExtraPass implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		Player activeChar = (Player) playable;

		SkillHolder[] skills = item.getItem().getSkills();
		if (skills == null) {
			return;
		}

		Skill instanceSkill = skills[0].getSkill();
		if (instanceSkill == null) {
			return;
		}

		if (activeChar.isSkillDisabled(instanceSkill)) {
			activeChar.sendMessage(item.getName() + " is currently under reuse!");
			return;
		}

		int instanceId = (int) instanceSkill.getPower();
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());
		if (instanceTimes.isEmpty() || !instanceTimes.containsKey(instanceId)) {
			activeChar.sendMessage("You don't have reuse!");
			return;
		}

		for (int instId : instanceTimes.keySet()) {
			if (instId == instanceId) {
				activeChar.doCast(instanceSkill);
				InstanceManager.getInstance().deleteInstanceTime(activeChar.getObjectId(), instId);
				break;
			}
		}

		activeChar.sendMessage("Instance reuse were restarted!");
	}
}
