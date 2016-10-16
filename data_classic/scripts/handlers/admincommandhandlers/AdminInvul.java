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

package handlers.admincommandhandlers;

import l2server.Config;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.util.logging.Logger;

/**
 * This class handles following admin commands:
 * - invul = turns invulnerability on/off
 *
 * @version $Revision: 1.2.4.4 $ $Date: 2007/07/31 10:06:02 $
 */
public class AdminInvul implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminInvul.class.getName());
	private static final String[] ADMIN_COMMANDS = {"admin_invul", "admin_setinvul"};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_invul"))
		{
			handleInvul(activeChar);
			//AdminHelpPage.showHelpPage(activeChar, "gm_menu.htm");
		}
		if (command.equals("admin_setinvul"))
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
			{
				handleInvul((L2PcInstance) target);
			}
			else if (target instanceof L2Character)
			{
				((L2Character) target).setIsInvul(((L2Character) target).isInvul());
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleInvul(L2PcInstance activeChar)
	{
		String text;
		if (activeChar.isInvul())
		{
			activeChar.setIsInvul(false);
			text = activeChar.getName() + " is now mortal";
			if (Config.DEBUG)
			{
				Log.fine("GM: Gm removed invul mode from character " + activeChar.getName() + "(" +
						activeChar.getObjectId() + ")");
			}
		}
		else
		{
			activeChar.setIsInvul(true);
			text = activeChar.getName() + " is now invulnerable";
			if (Config.DEBUG)
			{
				Log.fine("GM: Gm activated invul mode for character " + activeChar.getName() + "(" +
						activeChar.getObjectId() + ")");
			}
		}
		activeChar.sendMessage(text);
	}
}
