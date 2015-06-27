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

import java.util.StringTokenizer;

import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.CreatureSay;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class Teleport implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"teleto",
		"pvpzone"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
			return false;
		
		StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		
		if (command.startsWith("teleto"))	// Tenkai custom - raw teleport coordinates, only check for TW ward
		{
			if (activeChar.isCombatFlagEquipped())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD));
				return false;
			}
			
			if (activeChar.getPvpFlag() > 0)
			{
				activeChar.sendMessage("You can't teleport while flagged!");
				return false;
			}
			
			int[] coords = new int[3];
			try
			{
				for (int i = 0; i < 3; i++)
				{
					coords[i] = Integer.valueOf(st.nextToken());
				}
				activeChar.teleToLocation(coords[0], coords[1], coords[2]);
				activeChar.setInstanceId(0);
			}
			catch (Exception e)
			{
				_log.warning("L2Teleporter - " + target.getName()+"("+ target.getNpcId() +") - failed to parse raw teleport coordinates from html");
				e.printStackTrace();
			}
			
			return true;
		}
		else if (command.startsWith("pvpzone"))
		{
			boolean parties = st.nextToken().equals("1");
			boolean artificialPlayers = st.nextToken().equals("1");
			
			if (!parties && activeChar.isInParty() && !activeChar.isGM())
			{
				activeChar.sendPacket(new CreatureSay(0, Say2.TELL, target.getName(), "You can't go there being in a party."));
				return true;
			}
			
			if (activeChar.getPvpFlag() > 0)
			{
				activeChar.sendMessage("You can't teleport while flagged!");
				return false;
			}
			
			L2PcInstance mostPvP = L2World.getInstance().getMostPvP(parties, artificialPlayers);
			if (mostPvP != null)
			{
				activeChar.teleToLocation(mostPvP.getX(), mostPvP.getY(), mostPvP.getZ());
				activeChar.setInstanceId(0);
			}
			else
				activeChar.sendPacket(new CreatureSay(0, Say2.TELL, target.getName(), "Sorry, I can't find anyone in flag status right now."));
			
			return true;
		}
		return false;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}