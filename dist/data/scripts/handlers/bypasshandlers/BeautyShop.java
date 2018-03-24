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
import l2server.gameserver.network.serverpackets.ExShowBeautyList;
import l2server.gameserver.network.serverpackets.ExShowBeautyMenuPacket;
import l2server.gameserver.network.serverpackets.SocialAction;

public class BeautyShop implements IBypassHandler
{
	private static final String[] COMMANDS = {"use_beauty", "restore_beauty"};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target)
	{
		if (target == null)
		{
			return false;
		}

		if (command.equalsIgnoreCase("use_beauty"))
		{
			activeChar.sendPacket(new ExShowBeautyMenuPacket(false, activeChar));
			activeChar.sendPacket(new ExShowBeautyList(activeChar.getAdena(),
					activeChar.getInventory().getInventoryItemCount(36308, 0), false));
		}
		else if (command.equalsIgnoreCase("restore_beauty"))
		{
			//Retail way is missing, just for avoid critical errors/client stuck we will restore the char app to default values..
			activeChar.getAppearance().setFace(1);
			activeChar.getAppearance().setHairStyle(1);
			activeChar.getAppearance().setHairColor(1);
			activeChar.broadcastUserInfo();
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 30));
			//activeChar.sendPacket(new ExShowBeautyMenuPacket(true, activeChar));
		}

		return true;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
