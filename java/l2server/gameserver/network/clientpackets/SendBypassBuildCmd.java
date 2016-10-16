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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.datatables.AdminCommandAccessRights;
import l2server.gameserver.handler.AdminCommandHandler;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.util.GMAudit;
import l2server.log.Log;

/**
 * This class handles all GM commands triggered by //command
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:29 $
 */
public final class SendBypassBuildCmd extends L2GameClientPacket
{

	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;

	private String _command;

	@Override
	protected void readImpl()
	{
		_command = readS();
		if (_command != null)
		{
			_command = _command.trim();
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		String command = "admin_" + _command.split(" ")[0];

		IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);

		if (ach == null)
		{
			if (activeChar.isGM())
			{
				activeChar.sendMessage("The command " + command.substring(6) + " does not exists!");
			}

			Log.warning("No handler registered for admin command '" + command + "'");
			return;
		}

		if (!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command!");
			Log.warning("Character " + activeChar.getName() + " tryed to use admin command " + command +
					", but have no access to it!");
			return;
		}

		if (Config.GMAUDIT)
		{
			GMAudit.auditGMAction(activeChar.getName(), _command,
					activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
		}

		ach.useAdminCommand("admin_" + _command, activeChar);
	}
}
