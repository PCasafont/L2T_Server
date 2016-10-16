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

import l2server.Config;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2server.gameserver.handler.IChatHandler;
import l2server.gameserver.handler.IVoicedCommandHandler;
import l2server.gameserver.handler.VoicedCommandHandler;
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.log.Log;

import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * A chat handler
 *
 * @author durgus
 */
public class ChatAll implements IChatHandler
{
	private static final int[] COMMAND_IDS = {0};

	private static Logger _log = Logger.getLogger(ChatAll.class.getName());

	/**
	 * Handle chat type 'all'
	 */
	@Override
	public void handleChat(int type, L2PcInstance activeChar, String params, String text)
	{
		boolean vcd_used = false;
		if (text.startsWith("."))
		{
			StringTokenizer st = new StringTokenizer(text);
			IVoicedCommandHandler vch;
			String command = "";

			if (st.countTokens() > 1)
			{
				command = st.nextToken().substring(1);
				params = text.substring(command.length() + 2);
				vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			else
			{
				command = text.substring(1);
				if (Config.DEBUG)
				{
					Log.info("Command: " + command);
				}
				vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
			}
			if (vch != null)
			{
				vch.useVoicedCommand(command, activeChar, params);
				vcd_used = true;
			}
			else
			{
				if (Config.DEBUG)
				{
					Log.warning("No handler registered for bypass '" + command + "'");
				}
				vcd_used = false;
			}
		}

		if (!vcd_used)
		{
			CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), text);

			Collection<L2PcInstance> plrs = activeChar.getKnownList().getKnownPlayers().values();
			for (L2PcInstance player : plrs)
			{
				if (player != null && activeChar.isInsideRadius(player, 1250, false, true) &&
						!BlockList.isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
				}
			}

			activeChar.sendPacket(cs);

			while (text.contains("Type=") && text.contains("Title="))
			{
				int index1 = text.indexOf("Type=");
				int index2 = text.indexOf("Title=") + 6;
				text = text.substring(0, index1) + text.substring(index2);
			}

			String nearTown = MapRegionTable.getInstance().getClosestTownSimpleName(activeChar);
			ConsoleTab.appendMessage(ConsoleFilter.AllChat,
					"[Somewhere near " + nearTown + "] " + activeChar.getName() + ": " + text, nearTown,
					activeChar.getName());
		}
	}

	/**
	 * Returns the chat types registered to this handler
	 *
	 * @see IChatHandler#getChatTypeList()
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}
}
