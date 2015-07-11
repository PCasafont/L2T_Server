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
package handlers.chathandlers;

import l2tserver.Config;
import l2tserver.gameserver.gui.ConsoleTab;
import l2tserver.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2tserver.gameserver.handler.IChatHandler;
import l2tserver.gameserver.model.BlockList;
import l2tserver.gameserver.model.L2World;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.CreatureSay;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Tell chat handler.
 *
 * @author  durgus
 */
public class ChatTell implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		2
	};
	
	/**
	 * Handle chat type 'tell'
	 * @see l2tserver.gameserver.handler.IChatHandler#handleChat(int, l2tserver.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isChatBanned())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHATTING_PROHIBITED));
			return;
		}
		
		if (Config.JAIL_DISABLE_CHAT && activeChar.isInJail() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHATTING_PROHIBITED));
			return;
		}
		
		// Return if no target is set
		if (target == null)
			return;
		
		CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
		L2PcInstance receiver = null;
		
		receiver = L2World.getInstance().getPlayer(target);
		
		if (receiver != null && (!receiver.isSilenceMode() || activeChar.isGM()))
		{
			if (Config.JAIL_DISABLE_CHAT && receiver.isInJail() && !activeChar.isGM())
			{
				activeChar.sendMessage("Player is in jail.");
				return;
			}
			if (receiver.isChatBanned())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
				return;
			}
			if (receiver.getClient() == null || receiver.getClient().isDetached())
			{
				activeChar.sendMessage("Player is in offline mode.");
				return;
			}
			if (!BlockList.isBlocked(receiver, activeChar))
			{
				receiver.sendPacket(cs);
				activeChar.sendPacket(new CreatureSay(activeChar, type, "->" + receiver.getName(), text));
				
				while (text.contains("Type=") && text.contains("Title="))
				{
					int index1 = text.indexOf("Type=");
					int index2 = text.indexOf("Title=") + 6;
					text = text.substring(0, index1) + text.substring(index2);
				}
				
				ConsoleTab.appendMessage(ConsoleFilter.WhisperChat, activeChar.getName() + "->" + receiver.getName() + ": " + text, activeChar.getName(), receiver.getName());
			}
			else
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE));
		}
		else
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
	}
	
	/**
	 * Returns the chat types registered to this handler
	 * @see l2tserver.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
