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

package l2server.gameserver.model;

import l2server.gameserver.model.actor.instance.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gnacik
 */
public class PartyMatchWaitingList {
	private List<Player> members = new ArrayList<>();
	
	private PartyMatchWaitingList() {
	}
	
	public void addPlayer(Player player) {
		// player.setPartyWait(1);
		if (!members.contains(player)) {
			members.add(player);
		}
	}
	
	public void removePlayer(Player player) {
		//player.setPartyWait(0);
		if (members.contains(player)) {
			members.remove(player);
		}
	}
	
	public List<Player> getPlayers() {
		return members;
	}
	
	public static PartyMatchWaitingList getInstance() {
		return SingletonHolder.instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final PartyMatchWaitingList instance = new PartyMatchWaitingList();
	}
}
