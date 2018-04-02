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

import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldObject.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class L2DecoyAction implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
			return false;
		}

		activeChar.setTarget(target);
		activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((Creature) target).getLevel()));
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2Decoy;
	}
}
