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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class Hi5EventLetters implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"CheckLetters"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (command.equalsIgnoreCase("CheckLetters"))
		{
			boolean hasLetters[] = new boolean[5];
			for (int i = 0; i < 5; i++)
				hasLetters[i] = false;
			for (L2ItemInstance item : activeChar.getInventory().getItems())
			{
				switch (item.getItemId())
				{
				case 3882:
					hasLetters[0] = true;
					break;
				case 3888:
					hasLetters[1] = true;
					break;
				case 3887:
					hasLetters[2] = true;
					break;
				case 3883:
					hasLetters[3] = true;
					break;
				case 3886:
					hasLetters[4] = true;
				}
			}
			for (int i = 0; i < 5; i++)
			{
				if (!hasLetters[i])
				{
					activeChar.sendPacket(new NpcHtmlMessage(0, "<html><body>You don't have all the required letters!<br>Take your time to find all of them and come back.</body></html>"));
					return true;
				}
			}

			activeChar.destroyItemByItemId("High Five Event", 3882, 1, target, true);
			activeChar.destroyItemByItemId("High Five Event", 3888, 1, target, true);
			activeChar.destroyItemByItemId("High Five Event", 3887, 1, target, true);
			activeChar.destroyItemByItemId("High Five Event", 3883, 1, target, true);
			activeChar.destroyItemByItemId("High Five Event", 3886, 1, target, true);
			activeChar.addItem("High Five Event", 4037, 1, target, true);
			
			activeChar.sendPacket(new NpcHtmlMessage(0, "<html><body>Congratulations! Here your prize.</body></html>"));
		}
		
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}