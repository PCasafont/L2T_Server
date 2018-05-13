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
import l2server.gameserver.instancemanager.GMEventManager;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.InstanceType;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.actor.instance.StaticObjectInstance;
import l2server.gameserver.network.serverpackets.MyTargetSelected;
import l2server.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2StaticObjectInstanceAction implements IActionHandler {
	@Override
	public boolean action(Player activeChar, WorldObject target, boolean interact) {
		if (((StaticObjectInstance) target).getType() < 0) {
			log.info("StaticObjectInstance: StaticObject with invalid type! StaticObjectId: " +
					((StaticObjectInstance) target).getStaticObjectId());
		}

		// Check if the Player already target the NpcInstance
		if (activeChar.getTarget() != target) {
			// Set the target of the Player activeChar
			activeChar.setTarget(target);
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
		} else if (interact) {
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));

			// Calculate the distance between the Player and the NpcInstance
			if (!activeChar.isInsideRadius(target, Npc.DEFAULT_INTERACTION_DISTANCE, false, false)) {
				// Notify the Player AI with AI_INTENTION_INTERACT
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, target);
			} else {
				if (((StaticObjectInstance) target).getType() == 2) {
					String filename = "signboard.htm";
					String content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filename);
					NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());

					if (content == null) {
						html.setHtml("<html><body>Signboard is missing:<br>" + filename + "</body></html>");
					} else {
						html.setHtml(content);
					}

					activeChar.sendPacket(html);

					GMEventManager.getInstance().onNpcTalk(target, activeChar);
				} else if (((StaticObjectInstance) target).getType() == 0) {
					activeChar.sendPacket(((StaticObjectInstance) target).getMap());
				}
			}
		}
		return true;
	}

	@Override
	public InstanceType getInstanceType() {
		return InstanceType.L2StaticObjectInstance;
	}
}
