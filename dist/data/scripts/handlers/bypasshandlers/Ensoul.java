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

import l2server.gameserver.handler.IBypassHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExShowEnsoulWindow;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.ItemList;
import l2server.gameserver.templates.item.ItemTemplate;

/**
 * @author Pere
 */
public class Ensoul implements IBypassHandler {
	private static final String[] COMMANDS = {"ensoul", "remove_ensoul",};

	@Override
	public boolean useBypass(String command, Player activeChar, Npc target) {
		if (target == null) {
			return false;
		}

		if (command.equals("ensoul")) {
			activeChar.sendPacket(new ExShowEnsoulWindow());
		} else {
			Item weapon = activeChar.getActiveWeaponInstance();
			if (weapon == null) {
				activeChar.sendPacket(new ExShowScreenMessage("You must equip a weapon in order to remove its soul crystal effect!", 5000));
				return false;
			}
			if (weapon.isSoulEnhanced()) {
				weapon.removeEnsoulEffects();
				activeChar.getInventory().unEquipItemInBodySlot(ItemTemplate.SLOT_LR_HAND);
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new ItemList(activeChar, false));
				activeChar.sendPacket(new ExShowScreenMessage("The Ensoul Effects of your " + weapon.getName() + " have been removed", 5000));
			}
		}
		return true;
	}

	@Override
	public String[] getBypassList() {
		return COMMANDS;
	}
}
