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

import l2tserver.gameserver.datatables.PlayerClassTable;
import l2tserver.gameserver.handler.IBypassHandler;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ExChangeToAwakenedClass;

public class Awake implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Awake"
	};

	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
			return false;
		
		int classId = PlayerClassTable.getInstance().getAwakening(activeChar.getCurrentClass().getId());
		if (Integer.parseInt(command.split(" ")[1]) != classId)
			return false;
		
		activeChar.setLastCheckedAwakeningClassId(classId);
		activeChar.sendPacket(new ExChangeToAwakenedClass(classId));
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}