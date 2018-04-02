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
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.StatueInstance;
import l2server.gameserver.network.serverpackets.ExLoadStatHotLink;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.ValidateLocation;

public class L2StatueInstanceAction implements IActionHandler {
	/**
	 * Manage actions when a player click on the ArtefactInstance.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the NpcInstance as target of the Player player (if
	 * necessary)</li> <li>Send a Server->Client packet MyTargetSelected to the
	 * Player player (display the select window)</li> <li>Send a
	 * Server->Client packet ValidateLocation to correct the NpcInstance
	 * position and heading on the client</li><BR>
	 * <BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 */
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		if (!((Npc) target).canTarget(activeChar)) {
			return false;
		}

		if (activeChar.getTarget() != target) {
			// Set the target of the Player activeChar
			activeChar.setTarget(target);

			// Send a Server->Client packet MyTargetSelected to the Player activeChar
			MyTargetSelected my = new MyTargetSelected(target.getObjectId(), 0);
			activeChar.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the ArtefactInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation((Creature) target));
		} else if (interact) {
			// Send the hot link packet
			activeChar.sendPacket(new ExLoadStatHotLink(((StatueInstance) target).getRecordId()));
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2StatueInstance;
	}
}
