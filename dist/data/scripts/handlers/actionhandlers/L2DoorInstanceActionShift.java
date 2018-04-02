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
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.WorldObject.InstanceType;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.DoorInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;
import l2server.gameserver.network.serverpackets.StaticObject;

public class L2DoorInstanceActionShift implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		if (activeChar.getAccessLevel().isGm()) {
			activeChar.setTarget(target);
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel()));

			StaticObject su = new StaticObject((DoorInstance) target, activeChar.isGM());
			activeChar.sendPacket(su);

			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(activeChar.getHtmlPrefix(), "admin/doorinfo.htm");
			html.replace("%class%", target.getClass().getSimpleName());
			html.replace("%hp%", String.valueOf((int) ((Creature) target).getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(((Creature) target).getMaxHp()));
			html.replace("%pdef%", String.valueOf(((Creature) target).getPDef((Creature) target)));
			html.replace("%mdef%", String.valueOf(((Creature) target).getMDef((Creature) target, null)));
			html.replace("%objid%", String.valueOf(target.getObjectId()));
			html.replace("%doorid%", String.valueOf(((DoorInstance) target).getDoorId()));
			html.replace("%minx%", String.valueOf(((DoorInstance) target).getX(1)));
			html.replace("%miny%", String.valueOf(((DoorInstance) target).getY(1)));
			html.replace("%minz%", String.valueOf(((DoorInstance) target).getZMin()));
			html.replace("%maxx%", String.valueOf(((DoorInstance) target).getX(3)));
			html.replace("%maxy%", String.valueOf(((DoorInstance) target).getY(3)));
			html.replace("%maxz%", String.valueOf(((DoorInstance) target).getZMax()));
			html.replace("%unlock%", ((DoorInstance) target).isOpenableBySkill() ? "<font color=00FF00>YES<font>" : "<font color=FF0000>NO</font>");
			activeChar.sendPacket(html);
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2DoorInstance;
	}
}
