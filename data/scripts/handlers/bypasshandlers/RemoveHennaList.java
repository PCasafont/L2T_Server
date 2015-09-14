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
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2SymbolMakerInstance;
import l2server.gameserver.network.serverpackets.HennaRemoveList;
import l2server.gameserver.templates.item.L2Henna;

public class RemoveHennaList implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"RemoveList"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (!(target instanceof L2SymbolMakerInstance))
			return false;
		
		boolean hasHennas = false;
		for (int i = 1; i <= 3; i++)
		{
			L2Henna henna = activeChar.getHenna(i);
			
			if (henna != null)
				hasHennas = true;
		}
		if (hasHennas)
			activeChar.sendPacket(new HennaRemoveList(activeChar));
		
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}