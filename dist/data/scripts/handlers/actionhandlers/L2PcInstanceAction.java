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
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.instancemanager.CustomOfflineBuffersManager;
import l2server.gameserver.instancemanager.InstanceManager;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldObject.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.skills.EffectType;

public class L2PcInstanceAction implements IActionHandler {
	/**
	 * Manage actions when a player click on this Player.<BR><BR>
	 * <p>
	 * <B><U> Actions on first click on the Player (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 * <p>
	 * <B><U> Actions on second click on the Player (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If target Player has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If target Player is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If target Player is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param activeChar The player that start an action on target Player
	 */
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		activeChar.onActionRequest();

		// See description in Events.java
		if (activeChar.getEvent() != null && !activeChar.getEvent().onAction(activeChar, target.getObjectId())) {
			if (!activeChar.getEvent().isState(EventState.READY) && !activeChar.getEvent().isState(EventState.STARTED)) {
				activeChar.setEvent(null);
			}

			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (activeChar.getCaptcha() != null && !activeChar.onActionCaptcha(false)) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the Player is confused
		if (activeChar.isOutOfControl()) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Aggression target lock effect
		if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target) {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (activeChar != target && (activeChar.getParty() == null || activeChar.getParty() != ((Player) target).getParty()) &&
				((Player) target).isAffected(EffectType.UNTARGETABLE.getMask()) && !activeChar.isGM()) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (activeChar.getInstanceId() != activeChar.getObjectId()) {
			InstanceManager.getInstance().destroyInstance(activeChar.getObjectId());
		}

		// Check if the activeChar already target this Player
		if (activeChar.getTarget() != target) {
			// Set the target of the activeChar
			activeChar.setTarget(target);

			// Send a Server->Client packet MyTargetSelected to the activeChar
			// The color to display in the select window is White
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			if (target instanceof Creature) {
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((Creature) target));
			}
			if (activeChar != target) {
				activeChar.sendPacket(new ValidateLocation((Creature) target));
			}
		} else if (interact) {
			if (activeChar != target) {
				activeChar.sendPacket(new ValidateLocation((Creature) target));
			}
			// Check if this Player has a Private Store
			if (((Player) target).getPrivateStoreType() != 0) {
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			} else {
				// Check if this Player is autoAttackable
				if (target.isAutoAttackable(activeChar)) {
					// activeChar with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder  can't attack activeChars with lvl < 21
					if (((Player) target).isCursedWeaponEquipped() && activeChar.getLevel() < 21 ||
							activeChar.isCursedWeaponEquipped() && ((Creature) target).getLevel() < 21) {
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					} else {
						if (Config.GEODATA > 0) {
							if (GeoData.getInstance().canSeeTarget(activeChar, target)) {
								activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								activeChar.onActionRequest();
							}
						} else {
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							activeChar.onActionRequest();
						}
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

					if (Config.OFFLINE_BUFFERS_ENABLE && target instanceof Player && ((Player) target).getClient() != null &&
							((Player) target).getClient().isDetached() && ((Player) target).getIsOfflineBuffer()) {
						CustomOfflineBuffersManager.getInstance().getSpecificBufferInfo(activeChar, target.getObjectId());
					}
				}
			}
		}

		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2PcInstance;
	}
}
