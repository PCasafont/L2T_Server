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
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2StatueInstance;
import l2server.gameserver.network.serverpackets.ExLoadStatHotLink;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.ValidateLocation;

public class L2StatueInstanceAction implements IActionHandler {
	/**
	 * Manage actions when a player click on the L2ArtefactInstance.<BR>
	 * <BR>
	 * <p>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the L2NpcInstance as target of the L2PcInstance player (if
	 * necessary)</li> <li>Send a Server->Client packet MyTargetSelected to the
	 * L2PcInstance player (display the select window)</li> <li>Send a
	 * Server->Client packet ValidateLocation to correct the L2NpcInstance
	 * position and heading on the client</li><BR>
	 * <BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packet : Action, AttackRequest</li><BR>
	 * <BR>
	 */
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact) {
		if (!((L2Npc) target).canTarget(activeChar)) {
			return false;
		}

		if (activeChar.getTarget() != target) {
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
			MyTargetSelected my = new MyTargetSelected(target.getObjectId(), 0);
			activeChar.sendPacket(my);

			// Send a Server->Client packet ValidateLocation to correct the L2ArtefactInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation((L2Character) target));
		} else if (interact) {
			// Send the hot link packet
			activeChar.sendPacket(new ExLoadStatHotLink(((L2StatueInstance) target).getRecordId()));
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2StatueInstance;
	}
}
