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

import l2server.gameserver.datatables.PlayerClassTable;
import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExChangeToAwakenedClass;

public class Awake implements IBypassHandler {
	private static final String[] COMMANDS = {"Awake"};

	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Npc target) {
		if (target == null) {
			return false;
		}

		int awakenAtLevel = 85;
		if (activeChar.getCurrentClass().getLevel() != 76) {
			activeChar.sendMessage("In order to awake you must have at least 3rd profession.");
			return false;
		} else if (activeChar.getLevel() < awakenAtLevel) {
			activeChar.sendMessage("You must be level " + awakenAtLevel + " to awaken.");
			return false;
		}

		int classId = PlayerClassTable.getInstance().getAwakening(activeChar.getCurrentClass().getId());

		activeChar.setLastCheckedAwakeningClassId(classId);
		activeChar.sendPacket(new ExChangeToAwakenedClass(classId));
		return true;
	}

	@Override
	public String[] getBypassList() {
		return COMMANDS;
	}
}
