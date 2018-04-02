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

package handlers.actionhandlers;

import l2server.gameserver.handler.AdminCommandHandler;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.handler.IAdminCommandHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldObject.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.AbnormalStatusUpdateFromTarget;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.ValidateLocation;

public class L2SummonActionShift implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		if (activeChar.isGM()) {
			if (activeChar.getTarget() != target) {
				// Set the target of the Player activeChar
				activeChar.setTarget(target);

				// Send a Server->Client packet MyTargetSelected to the Player activeChar
				activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((Creature) target));
			}

			// Send a Server->Client packet ValidateLocation to correct the Player position and heading on the client
			activeChar.sendPacket(new ValidateLocation((Creature) target));

			IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler("admin_summon_info");
			if (ach != null) {
				ach.useAdminCommand("admin_summon_info", activeChar);
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2Summon;
	}
}
