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

import l2server.gameserver.Nodes.Node;
import l2server.gameserver.Nodes.NodesManager;
import l2server.gameserver.ai.CtrlIntention;
import l2server.gameserver.events.instanced.EventInstance.EventState;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.instancemanager.GMEventManager;
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldObject.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.Quest;
import l2server.gameserver.network.serverpackets.*;
import l2server.util.Rnd;

public class L2NpcAction implements IActionHandler {
	/**
	 * Manage actions when a player click on the Npc.<BR><BR>
	 * <p>
	 * <B><U> Actions on first click on the Npc (Select it)</U> :</B><BR><BR>
	 * <li>Set the Npc as target of the Player player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the Player player (display the select window)</li>
	 * <li>If Npc is autoAttackable, send a Server->Client packet StatusUpdate to the Player in order to update Npc HP bar </li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client </li><BR><BR>
	 * <p>
	 * <B><U> Actions on second click on the Npc (Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the Player player (display the select window)</li>
	 * <li>If Npc is autoAttackable, notify the Player AI with AI_INTENTION_ATTACK (after a height verification)</li>
	 * <li>If Npc is NOT autoAttackable, notify the Player AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR><BR>
	 * <p>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid
	 * that client wait an other packet</B></FONT><BR><BR>
	 * <p>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param activeChar The Player that start an action on the Npc
	 */
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		if (!((Npc) target).canTarget(activeChar)) {
			return false;
		}

		if (activeChar.getEvent() != null && activeChar.getEvent().isState(EventState.READY)) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if ((((Npc) target).getNpcId() >= 95000 && ((Npc) target).getNpcId() <= 95004) && !activeChar.isInsideRadius(target, 200, true, true)) {
			activeChar.sendMessage("You have to be closer from the totem.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (activeChar.getCaptcha() != null && !activeChar.onActionCaptcha(false)) {
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		activeChar.setLastFolkNPC((Npc) target);

		// Check if the Player already target the Npc
		if (target != activeChar.getTarget()) {
			// Set the target of the Player activeChar
			activeChar.setTarget(target);

			// Check if the activeChar is attackable (without a forced attack)
			if (target.isAutoAttackable(activeChar)) {
				((Npc) target).getAI(); //wake up ai
				// Send a Server->Client packet MyTargetSelected to the Player activeChar
				// The activeChar.getLevel() - getLevel() permit to display the correct color in the select window
				MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((Creature) target).getLevel());
				activeChar.sendPacket(my);
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((Creature) target));

				// Send a Server->Client packet StatusUpdate of the Npc to the Player to update its HP bar
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) ((Creature) target).getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, ((Creature) target).getMaxHp());
				activeChar.sendPacket(su);

				//TODO Temp fix for bugging paralysis bugs on monsters
				for (Abnormal e : ((Npc) target).getAllEffects()) {
					if (e.getTime() > e.getDuration() && e.getDuration() != -1) //Not if duration is defined with -1 (perm effect)
					{
						e.exit();
					}
				}
			} else {
				// Send a Server->Client packet MyTargetSelected to the Player activeChar
				MyTargetSelected my = new MyTargetSelected(target.getObjectId(), 0);
				activeChar.sendPacket(my);
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((Creature) target));
			}

			// Send a Server->Client packet ValidateLocation to correct the Npc position and heading on the client
			activeChar.sendPacket(new ValidateLocation((Creature) target));
		} else if (interact) {
			activeChar.sendPacket(new ValidateLocation((Creature) target));
			
			// Node Interact !
			if ((((Npc) target).getNpcId() >= 95000 && ((Npc) target).getNpcId() <= 95004)) {
				
				((Node) target).TalkToMe(activeChar);
				//NodesManager.getInstance().tryOwnNode(activeChar, (Npc) target);
				return true;
			}
			// Check if the activeChar is attackable (without a forced attack) and isn't dead
			if (target.isAutoAttackable(activeChar) && !((Creature) target).isAlikeDead()) {
				// Check the height difference
				if (Math.abs(activeChar.getZ() - target.getZ()) < 400) // this max heigth difference might need some tweaking
				{
					// Set the Player Intention to AI_INTENTION_ATTACK
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					// activeChar.startAttack(this);
				} else {
					// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
			} else if (!target.isAutoAttackable(activeChar)) {
				// Calculate the distance between the Player and the Npc
				if (!((Npc) target).canInteract(activeChar)) {
					// Notify the Player AI with AI_INTENTION_INTERACT
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				} else {
					if (((Npc) target).hasRandomAnimation()) {
						((Npc) target).onRandomAnimation(Rnd.get(8));
					}

					// Tenkai custom - instant action on touching certain NPC instead of html stuff etc.
					if (((Npc) target).getNpcId() == 50101) {
						NodesManager.getInstance().tryOwnNode(activeChar, (Npc) target);
						return true;
					}
					
					Quest[] qlsa = ((Npc) target).getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
					if (qlsa != null && qlsa.length > 0) {
						activeChar.setLastQuestNpcObject(target.getObjectId());
					}
					Quest[] qlst = ((Npc) target).getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
					if (qlst != null && qlst.length == 1) {
						qlst[0].notifyFirstTalk((Npc) target, activeChar);
					} else {
						((Npc) target).showChatWindow(activeChar);
					}

					GMEventManager.getInstance().onNpcTalk(target, activeChar);
				}
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2Npc;
	}
}
