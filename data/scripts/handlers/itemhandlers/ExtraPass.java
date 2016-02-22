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

import java.util.Map;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.SkillHolder;

public class ExtraPass implements IItemHandler
{
	/**
	 *
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		SkillHolder[] skills = item.getItem().getSkills();
		if (skills == null)
			return;
		
		L2Skill instanceSkill = skills[0].getSkill();
		if (instanceSkill == null)
			return;
		
		if (activeChar.isSkillDisabled(instanceSkill))
		{
			activeChar.sendMessage(item.getName() + " is currently under reuse!");
			return;
		}
		
		int instanceId = (int) instanceSkill.getPower();
		Map<Integer, Long> _instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(activeChar.getObjectId());
		if (_instanceTimes.isEmpty() || !_instanceTimes.containsKey(instanceId))
		{
			activeChar.sendMessage("You don't have reuse!");
			return;
		}
		
		for (int instId : _instanceTimes.keySet())
		{
			if (instId == instanceId)
			{
				activeChar.doCast(instanceSkill);
				InstanceManager.getInstance().deleteInstanceTime(activeChar.getObjectId(), instId);
				break;
			}
		}
		
		activeChar.sendMessage("Instance reuse were restarted!");
	}
}
