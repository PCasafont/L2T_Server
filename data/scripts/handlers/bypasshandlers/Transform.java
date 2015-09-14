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
package handlers.bypasshandlers;

import l2server.gameserver.datatables.MultiSell;
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2TransformManagerInstance;
import l2server.gameserver.model.quest.QuestState;
import l2server.gameserver.model.quest.State;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class Transform implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"transformskilllist",
		"buytransform",
		"transformtokukuru"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
			return false;
		
		if (command.toLowerCase().startsWith(COMMANDS[0])) // skills list
		{
			if (canTransform(activeChar))
			{
				L2TransformManagerInstance.showTransformSkillList(activeChar);
				return true;
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
				html.setFile(activeChar.getHtmlPrefix(), "default/" + target.getNpcId() + "-cantlearn.htm");
				activeChar.sendPacket(html);
			}
		}
		else if (command.toLowerCase().startsWith(COMMANDS[1]))
		{
			if (canTransform(activeChar))
			{
				MultiSell.getInstance().separateAndSend("32323001", activeChar, target, false);
				return true;
			}
			else
			{
				NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
				html.setFile(activeChar.getHtmlPrefix(), "default/" + target.getNpcId() + "-cantbuy.htm");
				activeChar.sendPacket(html);
			}
		}
		else if (command.toLowerCase().startsWith(COMMANDS[2]))
		{
			if (activeChar.isTransformed())
				return false;
			
			activeChar.doSimultaneousCast(SkillTable.getInstance().getInfo(9204,1));
			
			QuestState st = activeChar.getQuestState("Q10329_BackupSeekers");
			if (st != null)
				st.getQuest().notifyEvent("MountKookaru", target, activeChar);
			
			return true;
		}
		return false;
	}
	
	private static boolean canTransform(L2PcInstance player)
	{
		QuestState st = player.getQuestState("136_MoreThanMeetsTheEye");
		if (st != null && st.getState() == State.COMPLETED)
			return true;
		
		return false;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}