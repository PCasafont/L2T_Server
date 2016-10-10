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

import l2server.gameserver.datatables.AdminCommandAccessRights;
import l2server.gameserver.handler.AdminCommandHandler;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.logging.Logger;

/**
 * @author poltomb
 */
public class AdminSummon implements IAdminCommandHandler
{
    Logger _log = Logger.getLogger(AdminSummon.class.getName());

    public static final String[] ADMIN_COMMANDS = {"admin_summon"};

    /**
     * @see l2server.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    @Override
    public String[] getAdminCommandList()
    {

        return ADMIN_COMMANDS;
    }

    /**
     * @see l2server.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, l2server.gameserver.model.actor.instance.L2PcInstance)
     */
    @Override
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        int id;
        long count = 1;
        String[] data = command.split(" ");
        try
        {
            id = Integer.parseInt(data[1]);
            if (data.length > 2)
            {
                count = Long.parseLong(data[2]);
            }
        }
        catch (NumberFormatException nfe)
        {
            activeChar.sendMessage("Incorrect format for command 'summon'");
            return false;
        }

        String subCommand;
        if (id < 1000000)
        {
            subCommand = "admin_create_item";
            if (!AdminCommandAccessRights.getInstance().hasAccess(subCommand, activeChar.getAccessLevel()))
            {
                activeChar.sendMessage("You don't have the access right to use this command!");
                _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + subCommand +
                        ", but have no access to it!");
                return false;
            }
            IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(subCommand);
            ach.useAdminCommand(subCommand + " " + id + " " + count, activeChar);
        }
        else
        {
            subCommand = "admin_spawn_once";
            if (!AdminCommandAccessRights.getInstance().hasAccess(subCommand, activeChar.getAccessLevel()))
            {
                activeChar.sendMessage("You don't have the access right to use this command!");
                _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + subCommand +
                        ", but have no access to it!");
                return false;
            }
            IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(subCommand);

            activeChar.sendMessage("This is only a temporary spawn.  The mob(s) will NOT respawn.");
            id -= 1000000;
            ach.useAdminCommand(subCommand + " " + id + " " + count, activeChar);
        }
        return true;
    }
}
