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

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Playable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author LasTravel
 */

public class ScrollOfPkReduce implements IItemHandler
{
	/**
	 * 
	 * @see l2server.gameserver.handler.IItemHandler#useItem(l2server.gameserver.model.actor.L2Playable, l2server.gameserver.model.L2ItemInstance, boolean)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		L2Skill itemSkill = item.getItem().getSkills()[0].getSkill();
		
		int power = (int)itemSkill.getPower();
		
		if (activeChar.getPkKills() < power)
			return;
		
		activeChar.setPkKills(activeChar.getPkKills() - power);
		
		L2Skill skill = SkillTable.getInstance().getInfo(itemSkill.getId(), itemSkill.getLevel());
		activeChar.useMagic(skill, true, true);
						
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(item);
		activeChar.sendPacket(sm);
		
		activeChar.broadcastUserInfo();
		
		activeChar.sendMessage("PK count reduced by: " + power);
	}
}
