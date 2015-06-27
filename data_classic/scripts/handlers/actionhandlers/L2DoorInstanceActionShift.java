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
import l2server.gameserver.model.actor.instance.L2DoorInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.StaticObject;

public class L2DoorInstanceActionShift implements IActionHandler
{
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (activeChar.getAccessLevel().isGm())
		{
			activeChar.setTarget(target);
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel()));
			
			StaticObject su = new StaticObject((L2DoorInstance)target, activeChar.isGM());
			
			activeChar.sendPacket(su);
			
			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(activeChar.getHtmlPrefix(), "admin/doorinfo.htm");
			html.replace("%class%", target.getClass().getSimpleName());
			html.replace("%hp%",    String.valueOf((int)((L2Character)target).getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(((L2Character)target).getMaxHp()));
			html.replace("%objid%", String.valueOf(target.getObjectId()));
			html.replace("%doorid%",  String.valueOf(((L2DoorInstance)target).getDoorId()));
			
			html.replace("%minx%", String.valueOf(((L2DoorInstance)target).getX(1)));
			html.replace("%miny%", String.valueOf(((L2DoorInstance)target).getY(1)));
			// html.replace("%minx%", String.valueOf(((L2DoorInstance)target).getXMin()));
			// html.replace("%miny%", String.valueOf(((L2DoorInstance)target).getYMin()));
			// html.replace("%minz%", String.valueOf(((L2DoorInstance)target).getZMin()));
			
			html.replace("%maxx%", String.valueOf(((L2DoorInstance)target).getX(3)));
			html.replace("%maxy%", String.valueOf(((L2DoorInstance)target).getY(3)));
			// html.replace("%maxx%", String.valueOf(((L2DoorInstance)target).getXMax()));
			// html.replace("%maxy%", String.valueOf(((L2DoorInstance)target).getYMax()));
			// html.replace("%maxz%", String.valueOf(((L2DoorInstance)target).getZMax()));
			html.replace("%unlock%", ((L2DoorInstance)target).isOpenableBySkill() ? "<font color=00FF00>YES<font>" : "<font color=FF0000>NO</font>");
			// html.replace("%unlock%", ((L2DoorInstance)target).isUnlockable() ? "<font color=00FF00>YES<font>" : "<font color=FF0000>NO</font>");
			
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	public InstanceType getInstanceType()
	{
		return InstanceType.L2DoorInstance;
	}
}