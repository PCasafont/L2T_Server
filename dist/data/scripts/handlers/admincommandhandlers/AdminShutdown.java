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
import l2server.gameserver.Shutdown;
import l2server.gameserver.TimeController;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class handles following admin commands:
 * - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 *
 * @version $Revision: 1.5.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShutdown implements IAdminCommandHandler {
	//private static Logger log = Logger.getLogger(AdminShutdown.class.getName());
	
	private static final String[] ADMIN_COMMANDS = {"admin_server_shutdown", "admin_server_restart", "admin_server_abort"};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.startsWith("admin_server_shutdown")) {
			try {
				int val = Integer.parseInt(command.substring(22));
				serverShutdown(activeChar, val, false);
			} catch (StringIndexOutOfBoundsException e) {
				sendHtmlForm(activeChar);
			}
		} else if (command.startsWith("admin_server_restart")) {
			try {
				int val = Integer.parseInt(command.substring(21));
				serverShutdown(activeChar, val, true);
			} catch (StringIndexOutOfBoundsException e) {
				sendHtmlForm(activeChar);
			}
		} else if (command.startsWith("admin_server_abort")) {
			serverAbort(activeChar);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
	
	private void sendHtmlForm(Player activeChar) {
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		int t = TimeController.getInstance().getGameTime();
		int h = t / 60;
		int m = t % 60;
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		adminReply.setFile(activeChar.getHtmlPrefix(), "admin/shutdown.htm");
		
		int totalPlayers = World.getInstance().getAllPlayersCount();
		int actualPlayers = 0;
		
		for (Player player : World.getInstance().getAllPlayersArray()) {
			if (!player.isOnline()) {
				continue;
			}
			
			actualPlayers++;
		}
		
		adminReply.replace("%count%", totalPlayers + " (" + actualPlayers + ")");
		adminReply.replace("%used%", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
		adminReply.replace("%xp%", String.valueOf(Config.RATE_XP));
		adminReply.replace("%sp%", String.valueOf(Config.RATE_SP));
		adminReply.replace("%adena%", String.valueOf(Config.RATE_DROP_ITEMS_ID.get(57)));
		adminReply.replace("%drop%", String.valueOf(Config.RATE_DROP_ITEMS));
		adminReply.replace("%time%", String.valueOf(format.format(cal.getTime())));
		activeChar.sendPacket(adminReply);
	}
	
	private void serverShutdown(Player activeChar, int seconds, boolean restart) {
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(Player activeChar) {
		Shutdown.getInstance().abort(activeChar);
	}
}
