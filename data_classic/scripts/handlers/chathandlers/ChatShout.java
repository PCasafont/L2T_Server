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

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.Server;
import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2server.gameserver.handler.IChatHandler;
import l2server.gameserver.instancemanager.DiscussionManager;
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.CreatureSay;

/**
 * Shout chat handler.
 *
 * @author durgus
 */
public class ChatShout implements IChatHandler
{
    private static final int[] COMMAND_IDS = {1};

    /**
     * Handle chat type 'shout'
     *
     * @see l2server.gameserver.handler.IChatHandler#handleChat(int, l2server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    public void handleChat(int type, L2PcInstance activeChar, String target, String text)
    {
        if (Config.isServer(Config.TENKAI) && activeChar.isGM())
        {
            Announcements.getInstance().handleAnnounce(activeChar.getName() + ": " + text, 0);
            return;
        }

		/*if (activeChar.isGM())
		{
			type = Say2.PARTYROOM_ALL;
		}
		else*/
        if (DiscussionManager.getInstance().isGlobalChatDisabled())
        {
            activeChar.sendMessage("Global chat is disabled right now.");
            return;
        }
        else if (!activeChar.getFloodProtectors().getShoutChat().tryPerformAction("shout chat"))
        {
            activeChar.sendMessage("Do not spam shout channel.");
            return;
        }

        CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
        CreatureSay csReg = new CreatureSay(activeChar, type, activeChar.getName(), "[" + MapRegionTable.getInstance()
                .getClosestTownSimpleName(activeChar) + "]" + text);

        Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();

        if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_GLOBAL_CHAT
                .equalsIgnoreCase("gm") && activeChar.isGM()))
        {
            int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
            for (L2PcInstance player : pls)
            {
                if (activeChar.isGM() || (region == MapRegionTable.getInstance()
                        .getMapRegion(player.getX(), player.getY()) && !BlockList
                        .isBlocked(player, activeChar) && player.getInstanceId() == activeChar
                        .getInstanceId()) && activeChar.getEvent() == null)
                {
                    player.sendPacket(cs);
                }
                else if (player.isGM())
                {
                    player.sendPacket(csReg);
                }
            }
        }
        else if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
        {
            for (L2PcInstance player : pls)
            {
                if (!BlockList.isBlocked(player, activeChar))
                {
                    player.sendPacket(cs);
                }
            }
        }

        if (Server.gui != null)
        {
            while (text.contains("Type=") && text.contains("Title="))
            {
                int index1 = text.indexOf("Type=");
                int index2 = text.indexOf("Title=") + 6;
                text = text.substring(0, index1) + text.substring(index2);
            }

            if (!Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("global"))
            {
                text = "[" + MapRegionTable.getInstance().getClosestTownSimpleName(activeChar) + "]" + text;
            }

            if (!activeChar.isGM())
            {
                ConsoleTab.appendMessage(ConsoleFilter.ShoutChat, activeChar.getName() + ": " + text);
            }
        }
    }

    /**
     * Returns the chat types registered to this handler
     *
     * @see l2server.gameserver.handler.IChatHandler#getChatTypeList()
     */
    public int[] getChatTypeList()
    {
        return COMMAND_IDS;
    }
}
