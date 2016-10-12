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
import l2server.gameserver.Announcements;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.Collection;

/**
 * This class handles following admin commands:
 * - announce text = announces text to all players
 * - list_announcements = show menu
 * - reload_announcements = reloads announcements from txt file
 * - announce_announcements = announce all stored announcements to all players
 * - add_announcement text = adds text to startup announcements
 * - del_announcement id = deletes announcement with respective id
 *
 * @version $Revision: 1.4.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminAnnouncements implements IAdminCommandHandler
{

    private static final String[] ADMIN_COMMANDS = {
            "admin_list_announcements",
            "admin_reload_announcements",
            "admin_announce_announcements",
            "admin_add_announcement",
            "admin_del_announcement",
            "admin_announce",
            "admin_announce_menu"
    };

    @Override
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (command.equals("admin_list_announcements"))
        {
            Announcements.getInstance().listAnnouncements(activeChar);
        }
        else if (command.equals("admin_reload_announcements"))
        {
            Announcements.getInstance().loadAnnouncements();
            Announcements.getInstance().listAnnouncements(activeChar);
        }
        else if (command.startsWith("admin_announce_menu"))
        {
            if (Config.GM_ANNOUNCER_NAME && command.length() > 20)
            {
                command += " (" + activeChar.getName() + ")";
            }
            Announcements.getInstance().handleAnnounce(command, 20);
            AdminHelpPage.showHelpPage(activeChar, "gm_menu.htm");
        }
        else if (command.equals("admin_announce_announcements"))
        {
            Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
            // synchronized (L2World.getInstance().getAllPlayers())
            {
                for (L2PcInstance player : pls)
                {
                    Announcements.getInstance().showAnnouncements(player);
                }
            }
            Announcements.getInstance().listAnnouncements(activeChar);
        }
        else if (command.startsWith("admin_add_announcement"))
        {
            // FIXME the player can send only 16 chars (if you try to send more
            // it sends null), remove this function or not?
            if (!command.equals("admin_add_announcement"))
            {
                try
                {
                    String val = command.substring(23);
                    Announcements.getInstance().addAnnouncement(val);
                    Announcements.getInstance().listAnnouncements(activeChar);
                }
                catch (StringIndexOutOfBoundsException ignored)
                {
                }// ignore errors
            }
        }
        else if (command.startsWith("admin_del_announcement"))
        {
            try
            {
                int val = Integer.parseInt(command.substring(23));
                Announcements.getInstance().delAnnouncement(val);
                Announcements.getInstance().listAnnouncements(activeChar);
            }
            catch (StringIndexOutOfBoundsException ignored)
            {
            }
        }

        // Command is admin announce
        else if (command.startsWith("admin_announce"))
        {
            command = command.substring(15);
            // Call method from another class
            Announcements.getInstance().handleAnnounce(command, 0);
        }
        return true;
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
}
