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
import l2server.gameserver.ai.SummonAI;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldObject.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Summon;
import l2server.gameserver.model.actor.instance.CloneInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.SummonInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.util.Util;

public class L2SummonAction implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
			return false;
		}

		if (activeChar == ((Summon) target).getOwner() && !(target instanceof CloneInstance)) {
			if (target instanceof SummonInstance) {
				activeChar.setActiveSummon((SummonInstance) target);
			}

			if (activeChar.getTarget() == target) {
				for (SummonInstance summon : activeChar.getSummons()) {
					if (Util.checkIfInRange(5000, activeChar, target, true) || ((Summon) target).isOutOfControl()) {
						continue;
					}

					summon.setFollowStatus(false);
					summon.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
					((SummonAI) summon.getAI()).setStartFollowController(true);
					summon.setFollowStatus(true);
					summon.updateAndBroadcastStatus(0);
				}
				activeChar.sendPacket(new PetStatusShow((Summon) target));
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return true;
			}
		}
		if (activeChar.getTarget() != target) {
			if (Config.DEBUG) {
				log.debug("new target selected:" + target.getObjectId());
			}

			if (((Summon) target).getTemplate().Targetable || ((Summon) target).getOwner() == activeChar) {
				activeChar.setTarget(target);
				activeChar.sendPacket(new ValidateLocation((Creature) target));
				MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((Creature) target).getLevel());
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((Creature) target));
				activeChar.sendPacket(my);

				//sends HP/MP status of the summon to other characters
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) ((Creature) target).getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, ((Creature) target).getMaxHp());
				activeChar.sendPacket(su);
			}
		} else if (interact) {
			activeChar.sendPacket(new ValidateLocation((Creature) target));
			if (target.isAutoAttackable(activeChar)) {
				if (Config.GEODATA > 0) {
					if (GeoData.getInstance().canSeeTarget(activeChar, target)) {
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						activeChar.onActionRequest();
					}
				} else {
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					activeChar.onActionRequest();
				}
			} else {
				// This Action Failed packet avoids activeChar getting stuck when clicking three or more times
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				if (Config.GEODATA > 0) {
					if (GeoData.getInstance().canSeeTarget(activeChar, target)) {
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					}
				} else {
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
				}
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2Summon;
	}
}
