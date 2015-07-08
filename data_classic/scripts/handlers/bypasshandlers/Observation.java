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

import l2tserver.gameserver.handler.IBypassHandler;
import l2tserver.gameserver.instancemanager.SiegeManager;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.ItemList;
import l2tserver.gameserver.network.serverpackets.SystemMessage;

public class Observation implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"observesiege",
		"observeoracle",
		"observe"
	};

	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
			return false;
		
		try
		{
			if (command.toLowerCase().startsWith(COMMANDS[0])) // siege
			{
				String val = command.substring(13);
				StringTokenizer st = new StringTokenizer(val);
				st.nextToken(); // Bypass cost
				
				if (SiegeManager.getInstance().getSiege(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())) != null)
					doObserve(activeChar, target, val);
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_VIEW_SIEGE));
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[1])) // oracle
			{
				String val = command.substring(13);
				StringTokenizer st = new StringTokenizer(val);
				st.nextToken(); // Bypass cost
				doObserve(activeChar, target, val);
				return true;
			}
			else if (command.toLowerCase().startsWith(COMMANDS[2])) // observe
			{
				doObserve(activeChar, target, command.substring(8));
				return true;
			}
			
			return false;
		}
		catch (Exception e)
		{
			_log.info("Exception in " + getClass().getSimpleName());
		}
		return false;
	}
	
	private static final void doObserve(L2PcInstance player, L2Npc npc, String val)
	{
		StringTokenizer st = new StringTokenizer(val);
		long cost = Long.parseLong(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		
		if (player.reduceAdena("Broadcast", cost, npc, true))
		{
			// enter mode
			player.enterObserverMode(x, y, z);
			player.sendPacket(new ItemList(player, false));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}