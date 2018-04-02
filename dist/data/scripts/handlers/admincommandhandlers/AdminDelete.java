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

import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;

/**
 * This class handles following admin commands: - delete = deletes target
 *
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler {
	private static final String[] ADMIN_COMMANDS = {"admin_delete"};

	@Override
	public boolean useAdminCommand(String command, Player activeChar) {
		if (command.equals("admin_delete")) {
			handleDelete(activeChar);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private void handleDelete(Player activeChar) {
		WorldObject obj = activeChar.getTarget();
		if (obj instanceof Npc) {
			Npc target = (Npc) obj;
			target.deleteMe();

			L2Spawn spawn = target.getSpawn();
			if (spawn != null) {
				spawn.stopRespawn();
				SpawnTable.getInstance().deleteSpawn(spawn, true);
			}

			activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
		} else {
			activeChar.sendMessage("Incorrect target.");
		}
	}
}
