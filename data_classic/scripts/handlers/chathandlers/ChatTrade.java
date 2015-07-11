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

import l2tserver.Config;
import l2tserver.gameserver.datatables.MapRegionTable;
import l2tserver.gameserver.gui.ConsoleTab;
import l2tserver.gameserver.gui.ConsoleTab.ConsoleFilter;
import l2tserver.gameserver.handler.IChatHandler;
import l2tserver.gameserver.instancemanager.DiscussionManager;
import l2tserver.gameserver.model.BlockList;
import l2tserver.gameserver.model.L2World;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.CreatureSay;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Trade chat handler.
 *
 * @author  durgus
 */
public class ChatTrade implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		8
	};
	
	/**
	 * Handle chat type 'trade'
	 * @see l2tserver.gameserver.handler.IChatHandler#handleChat(int, l2tserver.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	public void handleChat(int type, L2PcInstance activeChar, String target, String text)
	{
		/*if (!text.startsWith("WTS ") && !text.startsWith("WTB ") && !text.startsWith("WTT "))
		{
			activeChar.sendMessage("Trade chat is reserved to trade! Your sentence must begin with WTS, WTB or WTT.");
			return;
		}*/
		
		if (!activeChar.isGM() && (DiscussionManager.getInstance().isGlobalChatDisabled() || !activeChar.getFloodProtectors().getTradeChat().tryPerformAction("trade chat")))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHATTING_PROHIBITED));
			return;
		}
		
		for (int i = 0; i < text.length(); i++)
		{
			if ((text.charAt(i) & (char)0xff00) != 0)
				text = text.substring(0, i) + text.substring(i + 1);
		}
		
		CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
		CreatureSay csReg = new CreatureSay(activeChar, type, activeChar.getName(), "[" + MapRegionTable.getInstance().getClosestTownSimpleName(activeChar) + "]" + text);
		
		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
		
		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM()))
		{
			int region = MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
			for (L2PcInstance player : pls)
			{
				if (region == MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY())
						&& !BlockList.isBlocked(player, activeChar)
						&& player.getInstanceId() == activeChar.getInstanceId()
						&& activeChar.getEvent() == null)
					player.sendPacket(cs);
				else if (player.isGM())
					player.sendPacket(csReg);
			}
		}
		else if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("global"))
		{	
			for (L2PcInstance player : pls)
			{
				if (!BlockList.isBlocked(player, activeChar))
					player.sendPacket(cs);
			}
		}
		
		if (text.contains("Type=") && text.contains("Title="))
		{
			int index1 = text.indexOf("Type=");
			int index2 = text.indexOf("Title=") + 6;
			text = text.substring(0, index1) + text.substring(index2);
		}

		String nearTown = MapRegionTable.getInstance().getClosestTownSimpleName(activeChar);
		if (!Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("global"))
			text = "[" + nearTown + "]" + text;
		
		ConsoleTab.appendMessage(ConsoleFilter.TradeChat, activeChar.getName() + ": " + text, nearTown);
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