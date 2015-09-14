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
import l2server.gameserver.cache.HtmCache;
import l2server.gameserver.handler.IActionHandler;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Object.InstanceType;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2StaticObjectInstanceAction implements IActionHandler
{
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (((L2StaticObjectInstance)target).getType() < 0)
			_log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: "+((L2StaticObjectInstance)target).getStaticObjectId());
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (activeChar.getTarget() != target)
		{
			// Set the target of the L2PcInstance activeChar
			activeChar.setTarget(target);
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
		}
		else if (interact)
		{
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!activeChar.isInsideRadius(target, L2Npc.DEFAULT_INTERACTION_DISTANCE, false, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			}
			else
			{
				if (((L2StaticObjectInstance)target).getType() == 2)
				{
					String filename = "signboard.htm";
					String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filename);
					NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
					
					if (content == null)
						html.setHtml("<html><body>Signboard is missing:<br>"+filename+"</body></html>");
					else
						html.setHtml(content);
					
					activeChar.sendPacket(html);
				}
				else if (((L2StaticObjectInstance)target).getType() == 0)
					activeChar.sendPacket(((L2StaticObjectInstance)target).getMap());
			}
		}
		return true;
	}
	
	public InstanceType getInstanceType()
	{
		return InstanceType.L2StaticObjectInstance;
	}
}