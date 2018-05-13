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

import l2server.Config;
import l2server.gameserver.GeoData;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;

public class L2PetInstanceAction implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
			return false;
		}

		boolean isOwner = activeChar.getObjectId() == ((PetInstance) target).getOwner().getObjectId();

		activeChar.sendPacket(new ValidateLocation((Creature) target));
		if (isOwner && activeChar != ((PetInstance) target).getOwner()) {
			((PetInstance) target).updateRefOwner(activeChar);
		}

		if (activeChar.getTarget() != target) {
			if (Config.DEBUG) {
				log.debug("new target selected:" + target.getObjectId());
			}

			// Set the target of the Player activeChar
			activeChar.setTarget(target);

			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((Creature) target).getLevel()));

			// Send a Server->Client packet StatusUpdate of the PetInstance to the Player to update its HP bar
			StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.CUR_HP, (int) ((Creature) target).getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, ((Creature) target).getMaxHp());
			activeChar.sendPacket(su);
		} else if (interact) {
			// Check if the pet is attackable (without a forced attack) and isn't dead
			if (target.isAutoAttackable(activeChar) && !isOwner) {
				if (Config.GEODATA > 0) {
					if (GeoData.getInstance().canSeeTarget(activeChar, target)) {
						// Set the Player Intention to AI_INTENTION_ATTACK
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						activeChar.onActionRequest();
					}
				} else {
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					activeChar.onActionRequest();
				}
			} else if (!((Creature) target).isInsideRadius(activeChar, 150, false, false)) {
				if (Config.GEODATA > 0) {
					if (GeoData.getInstance().canSeeTarget(activeChar, target)) {
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
						activeChar.onActionRequest();
					}
				} else {
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
					activeChar.onActionRequest();
				}
			} else {
				if (isOwner) {
					activeChar.sendPacket(new PetStatusShow((PetInstance) target));
				}
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2PetInstance;
	}
}
