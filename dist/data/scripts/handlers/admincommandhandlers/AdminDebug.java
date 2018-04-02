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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;

public class AdminDebug implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_debug"};

	@Override
	public final boolean useAdminCommand(String command, Player activeChar) {
		String[] commandSplit = command.split(" ");
		if (ADMIN_COMMANDS[0].equalsIgnoreCase(commandSplit[0])) {
			WorldObject target;
			if (commandSplit.length > 1) {
				target = World.getInstance().getPlayer(commandSplit[1].trim());
				if (target == null) {
					activeChar.sendMessage("Player not found.");
					return true;
				}
			} else {
				target = activeChar.getTarget();
			}

			if (target instanceof Creature) {
				setDebug(activeChar, (Creature) target);
			} else {
				setDebug(activeChar, activeChar);
			}
		}
		return true;
	}

	@Override
	public final String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private final void setDebug(Player activeChar, Creature target) {
		if (target.isDebug()) {
			target.setDebug(null);
			activeChar.sendMessage("Stop debugging " + target.getName());
		} else {
			target.setDebug(activeChar);
			activeChar.sendMessage("Start debugging " + target.getName());
		}
	}
}
