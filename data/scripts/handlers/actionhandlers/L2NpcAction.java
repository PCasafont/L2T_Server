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

import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.events.HiddenChests;
import l2tserver.gameserver.events.instanced.EventInstance.EventState;
import l2tserver.gameserver.handler.IActionHandler;
import l2tserver.gameserver.instancemanager.GMEventManager;
import l2tserver.gameserver.model.L2Abnormal;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Object.InstanceType;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.model.quest.Quest;
import l2tserver.gameserver.network.serverpackets.AbnormalStatusUpdateFromTarget;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.MyTargetSelected;
import l2tserver.gameserver.network.serverpackets.StatusUpdate;
import l2tserver.gameserver.network.serverpackets.ValidateLocation;
import l2tserver.util.Rnd;

public class L2NpcAction implements IActionHandler
{
	/**
	 * Manage actions when a player click on the L2Npc.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2Npc (Select it)</U> :</B><BR><BR>
	 * <li>Set the L2Npc as target of the L2PcInstance player (if necessary)</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2Npc is autoAttackable, send a Server->Client packet StatusUpdate to the L2PcInstance in order to update L2Npc HP bar </li>
	 * <li>Send a Server->Client packet ValidateLocation to correct the L2Npc position and heading on the client </li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2Npc (Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the L2PcInstance player (display the select window)</li>
	 * <li>If L2Npc is autoAttackable, notify the L2PcInstance AI with AI_INTENTION_ATTACK (after a height verification)</li>
	 * <li>If L2Npc is NOT autoAttackable, notify the L2PcInstance AI with AI_INTENTION_INTERACT (after a distance verification) and show message</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Each group of Server->Client packet must be terminated by a ActionFailed packet in order to avoid
	 * that client wait an other packet</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param activeChar The L2PcInstance that start an action on the L2Npc
	 *
	 */
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (!((L2Npc)target).canTarget(activeChar))
			return false;
		
		if (activeChar.getEvent() != null && activeChar.getEvent().isState(EventState.READY))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Chests event
		if (((L2Npc)target).getNpcId() == 50101 && !activeChar.isInsideRadius(target, 400, true, true))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (activeChar.getCaptcha() != null && !activeChar.onActionCaptcha(false))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		activeChar.setLastFolkNPC((L2Npc)target);
		
		// Check if the L2PcInstance already target the L2Npc
		if (target != activeChar.getTarget())
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);
			
			// Check if the activeChar is attackable (without a forced attack)
			if (target.isAutoAttackable(activeChar))
			{
				((L2Npc)target).getAI(); //wake up ai
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
				// The activeChar.getLevel() - getLevel() permit to display the correct color in the select window
				MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - ((L2Character)target).getLevel());
				activeChar.sendPacket(my);
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((L2Character)target));
				
				// Send a Server->Client packet StatusUpdate of the L2Npc to the L2PcInstance to update its HP bar
				StatusUpdate su = new StatusUpdate(target);
				su.addAttribute(StatusUpdate.CUR_HP, (int) ((L2Character)target).getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, ((L2Character)target).getMaxHp());
				activeChar.sendPacket(su);
				
				//TODO Temp fix for bugging paralysis bugs on monsters
				for (L2Abnormal e : ((L2Npc)target).getAllEffects())
				{
					if (e.getTime() > e.getDuration() && e.getDuration() != -1)	//Not if duration is defined with -1 (perm effect)
						e.exit();
				}
			}
			else
			{
				// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
				MyTargetSelected my = new MyTargetSelected(target.getObjectId(), 0);
				activeChar.sendPacket(my);
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((L2Character)target));
			}
			
			// Send a Server->Client packet ValidateLocation to correct the L2Npc position and heading on the client
			activeChar.sendPacket(new ValidateLocation((L2Character)target));
		}
		else if (interact)
		{
			activeChar.sendPacket(new ValidateLocation((L2Character)target));
			// Check if the activeChar is attackable (without a forced attack) and isn't dead
			if (target.isAutoAttackable(activeChar) && !((L2Character)target).isAlikeDead())
			{
				// Check the height difference
				if (Math.abs(activeChar.getZ() - target.getZ()) < 400) // this max heigth difference might need some tweaking
				{
					// Set the L2PcInstance Intention to AI_INTENTION_ATTACK
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					// activeChar.startAttack(this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else if (!target.isAutoAttackable(activeChar))
			{
				// Calculate the distance between the L2PcInstance and the L2Npc
				if (!((L2Npc)target).canInteract(activeChar))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else
				{
					if (((L2Npc)target).hasRandomAnimation())
						((L2Npc)target).onRandomAnimation(Rnd.get(8));
					
					// Tenkai custom - instant action on touching certain NPC instead of html stuff etc.
					if (((L2Npc)target).getNpcId() == 50101)
					{	
						HiddenChests.getInstance().tryOpenChest(activeChar, (L2Npc)target);
						return true;
					}
						
					Quest[] qlsa = ((L2Npc)target).getTemplate().getEventQuests(Quest.QuestEventType.QUEST_START);
					if ((qlsa != null) && qlsa.length > 0)
						activeChar.setLastQuestNpcObject(target.getObjectId());
					Quest[] qlst = ((L2Npc)target).getTemplate().getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);
					if ((qlst != null) && qlst.length == 1)
						qlst[0].notifyFirstTalk((L2Npc)target, activeChar);
					else
						((L2Npc)target).showChatWindow(activeChar);
					
					GMEventManager.getInstance().onNpcTalk(target, activeChar);
				}
			}
		}
		return true;
	}

	public InstanceType getInstanceType()
	{
		return InstanceType.L2Npc;
	}
}