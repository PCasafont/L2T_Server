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

import l2server.Config;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.CreatureSay;

public class Noblesse implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Noblesse"
	};
	
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null || !Config.isServer(Config.TENKAI))
			return false;
		
		if (activeChar.isNoble())
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You are already a noble, don't make me waste my time."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		if (activeChar.getSubClasses().size() == 0)
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You must have at least a subclass to be noble."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		if (!activeChar.destroyItemByItemId("Noblesse", 6393, 10, target, true))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		activeChar.setNoble(true);
		activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "I have just made you become a noble!"));
		
		return true;
	}
	
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}