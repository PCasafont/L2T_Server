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

import java.util.Collection;

import l2server.gameserver.events.instanced.EventsManager;
import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2server.gameserver.handler.IChatHandler;
import l2server.gameserver.instancemanager.DiscussionManager;
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.CreatureSay;

/**
 * Hero chat handler.
 *
 * @author durgus
 */
public class ChatHeroVoice implements IChatHandler
{
	private static final int[] COMMAND_IDS = {17};

	/**
	 * Handle chat type 'hero voice'
	 */
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		if (activeChar.isHero() && !EventsManager.getInstance().isPlayerParticipant(activeChar.getObjectId()) &&
				activeChar.getEvent() == null || activeChar.isGM())
		{
			if (!activeChar.isGM())
			{
				if (DiscussionManager.getInstance().isGlobalChatDisabled())
				{
					activeChar.sendMessage("Global chat is disabled right now.");
					return;
				}
				else if (!activeChar.getFloodProtectors().getHeroVoice().tryPerformAction("hero voice"))
				{
					activeChar.sendMessage(
							"Action failed. Heroes are only able to speak in the global channel once every 10 seconds.");
					return;
				}
			}

			for (int i = 0; i < text.length(); i++)
			{
				if ((text.charAt(i) & (char) 0xff00) != 0)
				{
					text = text.substring(0, i) + text.substring(i + 1);
				}
			}

			CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);

			Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
			for (L2PcInstance player : pls)
			{
				if (player == null)
				{
					continue;
				}

				if (!BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
				}
			}

			while (text.contains("Type=") && text.contains("Title="))
			{
				int index1 = text.indexOf("Type=");
				int index2 = text.indexOf("Title=") + 6;
				text = text.substring(0, index1) + text.substring(index2);
			}

			ConsoleTab.appendMessage(ConsoleFilter.HeroChat, activeChar.getName() + ": " + text);
		}
	}

	/**
	 * Returns the chat types registered to this handler
	 *
	 * @see l2server.gameserver.handler.IChatHandler#getChatTypeList()
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
