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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2StaticObjectInstance;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.StaticObject;
import l2server.util.StringUtil;

public class L2StaticObjectInstanceActionShift implements IActionHandler
{
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if (activeChar.getAccessLevel().isGm())
		{
			activeChar.setTarget(target);
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel()));
			
			StaticObject su = new StaticObject((L2StaticObjectInstance)target);
			activeChar.sendPacket(su);
			
			NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
			final String html1 = StringUtil.concat(
					"<html><body><center><font color=\"LEVEL\">Static Object Info</font></center><br><table border=0><tr><td>Coords X,Y,Z: </td><td>",
					String.valueOf(target.getX()),
					", ",
					String.valueOf(target.getY()),
					", ",
					String.valueOf(target.getZ()),
					"</td></tr><tr><td>Object ID: </td><td>",
					String.valueOf(target.getObjectId()),
					"</td></tr><tr><td>Static Object ID: </td><td>",
					String.valueOf(((L2StaticObjectInstance)target).getStaticObjectId()),
					"</td></tr><tr><td>Mesh Index: </td><td>",
					String.valueOf(((L2StaticObjectInstance)target).getMeshIndex()),
					"</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>",
					target.getClass().getSimpleName(),
					"</td></tr></table></body></html>"
			);
			html.setHtml(html1);
			activeChar.sendPacket(html);
		}
		return true;
	}
	
	public InstanceType getInstanceType()
	{
		return InstanceType.L2StaticObjectInstance;
	}
}
