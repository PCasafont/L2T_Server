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

import l2tserver.Config;
import l2tserver.gameserver.GeoData;
import l2tserver.gameserver.ai.CtrlIntention;
import l2tserver.gameserver.events.instanced.EventInstance.EventState;
import l2tserver.gameserver.handler.IActionHandler;
import l2tserver.gameserver.instancemanager.CustomOfflineBuffersManager;
import l2tserver.gameserver.instancemanager.InstanceManager;
import l2tserver.gameserver.model.L2Abnormal;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Object.InstanceType;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.SystemMessageId;
import l2tserver.gameserver.network.serverpackets.AbnormalStatusUpdateFromTarget;
import l2tserver.gameserver.network.serverpackets.ActionFailed;
import l2tserver.gameserver.network.serverpackets.MyTargetSelected;
import l2tserver.gameserver.network.serverpackets.SystemMessage;
import l2tserver.gameserver.network.serverpackets.ValidateLocation;
import l2tserver.gameserver.templates.skills.L2EffectType;

public class L2PcInstanceAction implements IActionHandler
{
	/**
	 * Manage actions when a player click on this L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If target L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If target L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If target L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param activeChar The player that start an action on target L2PcInstance
	 *
	 */
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		activeChar.onActionRequest();
		
		// See description in Events.java
		if (activeChar.getEvent() != null
				&& !activeChar.getEvent().onAction(activeChar, target.getObjectId()))
		{
			if (!activeChar.getEvent().isState(EventState.READY)
					&& !activeChar.getEvent().isState(EventState.STARTED))
				activeChar.setEvent(null);
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (activeChar.getCaptcha() != null && !activeChar.onActionCaptcha(false))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the L2PcInstance is confused
		if (activeChar.isOutOfControl())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Aggression target lock effect
		if (activeChar.isLockedTarget() && activeChar.getLockedTarget() != target)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CHANGE_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (activeChar != target)
		{
			// Is the player under untargetability effect?
			for (L2Abnormal e : ((L2PcInstance)target).getAllEffects())
			{
				if (e != null &&
						((e.getEffectMask() & L2EffectType.UNTARGETABLE.getMask()) > 0
						|| e.getSkill().getId() == 10528))
				{
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if (activeChar.getInstanceId() != activeChar.getObjectId())
			InstanceManager.getInstance().destroyInstance(activeChar.getObjectId());
		
		// Check if the activeChar already target this L2PcInstance
		if (activeChar.getTarget() != target)
		{
			// Set the target of the activeChar
			activeChar.setTarget(target);
			
			// Send a Server->Client packet MyTargetSelected to the activeChar
			// The color to display in the select window is White
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			if (target instanceof L2Character && target.getObjectId() != activeChar.getObjectId())
				activeChar.sendPacket(new AbnormalStatusUpdateFromTarget((L2Character)target));
			if (activeChar != target) activeChar.sendPacket(new ValidateLocation((L2Character)target));
		}
		else if (interact)
		{
			if (activeChar != target) activeChar.sendPacket(new ValidateLocation((L2Character)target));
			// Check if this L2PcInstance has a Private Store
			if (((L2PcInstance)target).getPrivateStoreType() != 0)
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			}
			else
			{
				// Check if this L2PcInstance is autoAttackable
				if (target.isAutoAttackable(activeChar))
				{
					// activeChar with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder  can't attack activeChars with lvl < 21
					if ((((L2PcInstance)target).isCursedWeaponEquipped() && activeChar.getLevel() < 21)
							|| (activeChar.isCursedWeaponEquipped() && ((L2Character)target).getLevel() < 21))
					{
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (Config.GEODATA > 0)
						{
							if (GeoData.getInstance().canSeeTarget(activeChar, target))
							{
								activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
								activeChar.onActionRequest();
							}
						}
						else
						{
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
							activeChar.onActionRequest();
						}
					}
				}
				else
				{
					// This Action Failed packet avoids activeChar getting stuck when clicking three or more times
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					if (Config.GEODATA > 0)
					{
						if (GeoData.getInstance().canSeeTarget(activeChar, target))
							activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					}
					else
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					
					if (target instanceof L2PcInstance && ((L2PcInstance)target).getClient().isDetached() && ((L2PcInstance)target).getIsOfflineBuffer())
						CustomOfflineBuffersManager.getInstance().getSpecificBufferInfo(activeChar, target.getObjectId());
				}
			}
		}
		return true;
	}
	
	public InstanceType getInstanceType()
	{
		return InstanceType.L2PcInstance;
	}
}
