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

import l2server.gameserver.custom.fusion.MiniGamesManager;
import l2server.gameserver.custom.fusion.minigames.MiniGame;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class Fusion implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"join_mini_game",
		"magic_gem_view_page"
	};

	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (command.startsWith("join_mini_game"))
		{
			MiniGame _Event = MiniGamesManager.getCurrentMiniGame();
			
			SystemMessage s = null;
			if (_Event == null)
			{
				s = SystemMessage.getSystemMessage(SystemMessageId.RED_CHATBOX_S1);
				s.addString("There is no mini game going on.");
			}
			else if (!_Event.inRegistrations())
			{
				s = SystemMessage.getSystemMessage(SystemMessageId.RED_CHATBOX_S1);
				s.addString("The registrations for " + _Event.getName() + " are closed.");
			}
			else if (!_Event.onPlayerRegistration(activeChar))
			{
				s = SystemMessage.getSystemMessage(SystemMessageId.RED_CHATBOX_S1);
				s.addString("You are not allowed to participate to this mini game.");
			}
			else
			{
				s = SystemMessage.getSystemMessage(SystemMessageId.RED_CHATBOX_S1);
				s.addString("You are now registered to the " + _Event.getName() + ".");
			}
			
			activeChar.sendPacket(s);
			return false;
		}
		else if (command.startsWith("magic_gem_view_page"))
		{
			
		}
		
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}