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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.model.quest.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket {
	String bypass;
	
	@Override
	protected void readImpl() {
		readD(); // GoD ???
		bypass = readS();
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		
		QuestState qs = player.getQuestState("Q255_Tutorial");
		if (qs != null) {
			qs.getQuest().notifyEvent(bypass, null, player);
		}
	}
}
