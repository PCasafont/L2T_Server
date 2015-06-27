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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import l2server.Config;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.instancemanager.ItemAuctionManager;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemauction.ItemAuction;
import l2server.gameserver.model.itemauction.ItemAuctionInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExItemAuctionInfoPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class ItemAuctionLink implements IBypassHandler
{
	private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
	
	private static final String[] COMMANDS =
	{
		"ItemAuction"
	};

	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
			return false;
		
		if (!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_AUCTION_PERIOD));
			return true;
		}
		
		final ItemAuctionInstance au = ItemAuctionManager.getInstance().getManagerInstance(target.getNpcId());
		if (au == null)
			return false;
		
		try
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // bypass "ItemAuction"
			if (!st.hasMoreTokens())
				return false;
			
			String cmd = st.nextToken();
			if (cmd.equalsIgnoreCase("show"))
			{
				if (!activeChar.getFloodProtectors().getItemAuction().tryPerformAction("RequestInfoItemAuction"))
					return false;
				
				if (activeChar.isItemAuctionPolling())
					return false;
				
				final ItemAuction currentAuction = au.getCurrentAuction();
				final ItemAuction nextAuction = au.getNextAuction();
				
				if (currentAuction == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_AUCTION_PERIOD));
					
					if (nextAuction != null) // used only once when database is empty
						activeChar.sendMessage("The next auction will begin on the " + fmt.format(new Date(nextAuction.getStartingTime())) + ".");
					return true;
				}
				
				activeChar.sendPacket(new ExItemAuctionInfoPacket(false, currentAuction, nextAuction));
			}
			else if (cmd.equalsIgnoreCase("cancel"))
			{
				final ItemAuction[] auctions = au.getAuctionsByBidder(activeChar.getObjectId());
				boolean returned = false;
				for (final ItemAuction auction : auctions)
				{
					if (auction.cancelBid(activeChar))
						returned = true;
				}
				if (!returned)
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR));
			}
			else
				return false;
		}
		catch (Exception e)
		{
			_log.severe("Exception in: " + getClass().getSimpleName() + ":" + e.getMessage());
		}
		
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}