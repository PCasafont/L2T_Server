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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.model.InstanceType;
import l2server.Config;
import l2server.gameserver.model.actor.Npc;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.templates.chars.NpcTemplate;

/**
 * Olympiad Npc's Instance
 *
 * @author godson
 */
public class OlympiadManagerInstance extends Npc {
	public OlympiadManagerInstance(int objectId, NpcTemplate template) {
		super(objectId, template);
		setInstanceType(InstanceType.L2OlympiadManagerInstance);
	}
	
	public void showChatWindow(Player player, int val, String suffix) {
		// Tenkai custom for easy disabling of Oly npc when something is wrong with the Olympiad system
		if (!Config.ALT_OLY_NPC_REACTS) {
			return;
		}
		
		String filename = Olympiad.OLYMPIAD_HTML_PATH;
		
		filename += "noble_desc" + val;
		filename += suffix != null ? suffix + ".htm" : ".htm";
		
		if (filename.equals(Olympiad.OLYMPIAD_HTML_PATH + "noble_desc0.htm")) {
			if (player.isNoble()) {
				filename = Olympiad.OLYMPIAD_HTML_PATH + "noble_main.htm";
			} else {
				filename = "default/" + getNpcId() + ".htm";
			}
		}
		showChatWindowByFileName(player, filename);
	}
}
