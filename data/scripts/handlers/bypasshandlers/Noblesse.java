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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.clientpackets.Say2;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.CreatureSay;

public class Noblesse implements IBypassHandler
{
	private static final String[] COMMANDS = { "Noblesse" };
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null || !Config.isServer(Config.DREAMS))
			return false;
		
		if (activeChar.isNoble())
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You are already noble."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (activeChar.getSubClasses().size() == 0)
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You must have at least a subclass to be noble."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		L2ItemInstance golkondaHorn = activeChar.getInventory().getItemByItemId(50500);
		L2ItemInstance guillotineBones = activeChar.getInventory().getItemByItemId(50501);
		L2ItemInstance queenThemisSkull = activeChar.getInventory().getItemByItemId(50502);
		
		if (golkondaHorn == null)
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You do not have a Golkonda Horn Piece."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (guillotineBones == null)
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You do not have a Watchman Guillotine Bones."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (queenThemisSkull == null)
		{
			activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "You do not have a Queen Themis Skull."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!activeChar.destroyItemByItemId("Noblesse", golkondaHorn.getItemId(), 1, target, true) || !activeChar.destroyItemByItemId("Noblesse", guillotineBones.getItemId(), 1, target, true) || !activeChar.destroyItemByItemId("Noblesse", queenThemisSkull.getItemId(), 1, target, true))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		activeChar.setNoble(true);
		activeChar.sendPacket(new CreatureSay(target.getObjectId(), Say2.TELL, target.getName(), "Excellent. Thank you. And enjoy!"));
		
		return true;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
