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

import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.ApInstance;
import l2server.gameserver.model.actor.instance.Player;

import java.util.Collection;
import java.util.StringTokenizer;

public class AdminKick implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_kick", "admin_kick_non_gm"};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.startsWith("admin_kick")) {
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1) {
				st.nextToken();
				String player = st.nextToken();
				Player plyr = World.getInstance().getPlayer(player);
				if (plyr != null) {
					plyr.logout();
					activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
				}
			} else if (activeChar.getTarget() instanceof Player) {
				Player target = (Player) activeChar.getTarget();
				if (target instanceof ApInstance) {
					target.deleteMe();
				} else {
					target.logout();
				}
				activeChar.sendMessage("You kicked " + target.getName() + " from the game.");
			}
		}
		if (command.startsWith("admin_kick_non_gm")) {
			int counter = 0;
			Collection<Player> pls = World.getInstance().getAllPlayers().values();
			//synchronized (World.getInstance().getAllPlayers())
			{
				for (Player player : pls) {
					if (!player.isGM()) {
						counter++;
						player.logout();
					}
				}
			}
			activeChar.sendMessage("Kicked " + counter + " players");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}
}
