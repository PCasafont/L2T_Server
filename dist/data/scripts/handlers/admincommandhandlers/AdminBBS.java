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

// import java.util.logging.Logger;

import l2server.gameserver.communitybbs.Manager.AdminBBSManager;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public class AdminBBS implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {"admin_bbs"};

	/**
	 * @see l2server.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, l2server.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		AdminBBSManager.getInstance().parsecmd(command, activeChar);
		return true;
	}

	/**
	 * @see l2server.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
