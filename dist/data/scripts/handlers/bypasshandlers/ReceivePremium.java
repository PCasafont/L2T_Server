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

import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExGetPremiumItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class ReceivePremium implements IBypassHandler {
	private static final String[] COMMANDS = {"ReceivePremium"};
	
	@Override
	public boolean useBypass(String command, Player activeChar, Npc target) {
		if (target == null)
		
		{
			if (activeChar.getPremiumItemList().isEmpty()) {
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND));
				return false;
			}
		}
		
		activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
		
		return true;
	}
	
	@Override
	public String[] getBypassList() {
		return COMMANDS;
	}
}
