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

import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ConfirmDlg;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.StaticObject;
import l2server.gameserver.network.serverpackets.ValidateLocation;

public class L2DoorInstanceAction implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		// Check if the Player already target the NpcInstance
		if (activeChar.getTarget() != target) {
			// Set the target of the Player activeChar
			activeChar.setTarget(target);

			// Send a Server->Client packet MyTargetSelected to the Player activeChar
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));

			StaticObject su = new StaticObject((DoorInstance) target, activeChar.isGM());
			activeChar.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation((Creature) target));
		} else if (interact) {
			//            MyTargetSelected my = new MyTargetSelected(getObjectId(), activeChar.getLevel());
			//            activeChar.sendPacket(my);
			if (target.isAutoAttackable(activeChar)) {
				if (Math.abs(activeChar.getZ() - target.getZ()) < 400) // this max heigth difference might need some tweaking
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
			} else if (activeChar.getClan() != null && ((DoorInstance) target).getClanHall() != null &&
					activeChar.getClanId() == ((DoorInstance) target).getClanHall().getOwnerId()) {
				if (!((Creature) target).isInsideRadius(activeChar, Npc.DEFAULT_INTERACTION_DISTANCE, false, false)) {
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				} else {
					activeChar.gatesRequest((DoorInstance) target);
					if (!((DoorInstance) target).getOpen()) {
						activeChar.sendPacket(new ConfirmDlg(1140));
					} else {
						activeChar.sendPacket(new ConfirmDlg(1141));
					}
				}
			} else if (activeChar.getClan() != null && ((DoorInstance) target).getFort() != null &&
					activeChar.getClan() == ((DoorInstance) target).getFort().getOwnerClan() && ((DoorInstance) target).isOpenableBySkill() &&
					!((DoorInstance) target).getFort().getSiege().getIsInProgress()) {
				if (!((Creature) target).isInsideRadius(activeChar, Npc.DEFAULT_INTERACTION_DISTANCE, false, false)) {
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				} else {
					activeChar.gatesRequest((DoorInstance) target);
					if (!((DoorInstance) target).getOpen()) {
						activeChar.sendPacket(new ConfirmDlg(1140));
					} else {
						activeChar.sendPacket(new ConfirmDlg(1141));
					}
				}
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2DoorInstance;
	}
}
