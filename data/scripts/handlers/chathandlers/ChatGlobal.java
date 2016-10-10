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

import gnu.trove.TIntIntHashMap;

import java.util.Collection;

import l2server.Config;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.gui.ConsoleTab;
import l2server.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2server.gameserver.handler.IChatHandler;
import l2server.gameserver.instancemanager.DiscussionManager;
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.ExWorldChatCnt;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.stats.Stats;

/**
 * Global chat handler.
 *
 * @author Pere
 */
public class ChatGlobal implements IChatHandler
{
    private static final int[] COMMAND_IDS = {25};

    private TIntIntHashMap _messages = new TIntIntHashMap();

    public ChatGlobal()
    {
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (_messages)
                {
                    _messages.clear();
                }
            }
        }, 1000L, 24L * 3600L * 1000L);
    }

    /**
     * Handle chat type 'global'
     *
     * @see l2server.gameserver.handler.IChatHandler#handleChat(int, l2server.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
     */
    @Override
    public void handleChat(int type, L2PcInstance activeChar, String target, String text)
    {
        if (!activeChar.isGM() && (DiscussionManager.getInstance().isGlobalChatDisabled() ||
                !activeChar.getFloodProtectors().getTradeChat().tryPerformAction("global chat")))
        {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHATTING_PROHIBITED));
            return;
        }

        int messages = 0;
        synchronized (_messages)
        {
            if (_messages.containsKey(activeChar.getObjectId()))
            {
                messages = _messages.get(activeChar.getObjectId());
            }

            messages++;
            _messages.put(activeChar.getObjectId(), messages);
        }

        int maxMessages = 20 + (int) activeChar.calcStat(Stats.GLOBAL_CHAT, 0, activeChar, null);
        if (messages > maxMessages)
        {
            activeChar.sendMessage("You can't write more than " + maxMessages + " global messages a day.");
            return;
        }

        activeChar.sendPacket(new ExWorldChatCnt(maxMessages - messages));

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

        ConsoleTab.appendMessage(ConsoleFilter.GlobalChat, activeChar.getName() + ": " + text);
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
