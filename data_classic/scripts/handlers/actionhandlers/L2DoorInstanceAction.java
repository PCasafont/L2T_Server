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
import l2tserver.gameserver.handler.IActionHandler;
import l2tserver.gameserver.model.L2Object;
import l2tserver.gameserver.model.L2Object.InstanceType;
import l2tserver.gameserver.model.actor.L2Character;
import l2tserver.gameserver.model.actor.L2Npc;
import l2tserver.gameserver.model.actor.instance.L2DoorInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.network.serverpackets.ConfirmDlg;
import l2tserver.gameserver.network.serverpackets.MyTargetSelected;
import l2tserver.gameserver.network.serverpackets.StaticObject;
import l2tserver.gameserver.network.serverpackets.ValidateLocation;

public class L2DoorInstanceAction implements IActionHandler
{
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		// Check if the L2PcInstance already target the L2NpcInstance
		if (activeChar.getTarget() != target)
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);
			
			// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			
			StaticObject su = new StaticObject((L2DoorInstance)target, activeChar.isGM());			
			activeChar.sendPacket(su);
			
			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			activeChar.sendPacket(new ValidateLocation((L2Character)target));
		}
		else if (interact)
		{
			//            MyTargetSelected my = new MyTargetSelected(getObjectId(), activeChar.getLevel());
			//            activeChar.sendPacket(my);
			if (target.isAutoAttackable(activeChar))
			{
				if (Math.abs(activeChar.getZ() - target.getZ()) < 400) // this max heigth difference might need some tweaking
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			else if (activeChar.getClan() != null
					&& ((L2DoorInstance)target).getClanHall() != null
					&& activeChar.getClanId() == ((L2DoorInstance)target).getClanHall().getOwnerId())
			{
				if (!((L2Character)target).isInsideRadius(activeChar, L2Npc.DEFAULT_INTERACTION_DISTANCE, false, false))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else
				{
					activeChar.gatesRequest((L2DoorInstance)target);
					if (!((L2DoorInstance)target).getOpen())
						activeChar.sendPacket(new ConfirmDlg(1140));
					else
						activeChar.sendPacket(new ConfirmDlg(1141));
				}
			}
			else if (activeChar.getClan() != null
					&& ((L2DoorInstance)target).getFort() != null
					&& activeChar.getClan() == ((L2DoorInstance)target).getFort().getOwnerClan()
					&& ((L2DoorInstance)target).isOpenableBySkill()
					&& !((L2DoorInstance)target).getFort().getSiege().getIsInProgress())
			{
				if (!((L2Character)target).isInsideRadius(activeChar, L2Npc.DEFAULT_INTERACTION_DISTANCE, false, false))
				{
					activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
				}
				else
				{
					activeChar.gatesRequest((L2DoorInstance)target);
					if (!((L2DoorInstance)target).getOpen())
						activeChar.sendPacket(new ConfirmDlg(1140));
					else
						activeChar.sendPacket(new ConfirmDlg(1141));
				}
			}
		}
		return true;
	}
	
	public InstanceType getInstanceType()
	{
		return InstanceType.L2DoorInstance;
	}
}
