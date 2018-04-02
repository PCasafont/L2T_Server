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

package l2server.gameserver.instancemanager;

import l2server.gameserver.model.actor.instance.Player;
import l2server.util.loader.annotations.Load;

import java.util.ArrayList;

/**
 * @author Erlandys
 */
public class PartySearchManager {
	ArrayList<Player> lookingForParty;
	ArrayList<Player> wannaToChangeThisPlayer;

	public PartySearchManager() {
	}

	@Load
	public void load() {
		if (lookingForParty == null) {
			lookingForParty = new ArrayList<>();
		} else {
			lookingForParty.clear();
		}
		if (wannaToChangeThisPlayer == null) {
			wannaToChangeThisPlayer = new ArrayList<>();
		} else {
			wannaToChangeThisPlayer.clear();
		}
	}

	public void addLookingForParty(Player player) {
		lookingForParty.add(player);
	}

	public void addChangeThisPlayer(Player player) {
		wannaToChangeThisPlayer.add(player);
	}

	public ArrayList<Player> getLookingForPartyPlayers() {
		return lookingForParty;
	}

	public ArrayList<Player> getWannaToChangeThisPlayers() {
		return wannaToChangeThisPlayer;
	}

	public void removeLookingForParty(Player player) {
		lookingForParty.remove(player);
	}

	public void removeChangeThisPlayer(Player player) {
		wannaToChangeThisPlayer.remove(player);
	}

	public Player getLookingForParty(int level, int classId) {
		for (Player player : lookingForParty) {
			if (player == null) {
				continue;
			}

			if (player.getLevel() == level && player.getClassId() == classId) {
				return player;
			}
		}
		return null;
	}

	public Player getWannaToChangeThisPlayer(int level, int classId) {
		for (Player player : wannaToChangeThisPlayer) {
			if (player.getLevel() == level && player.getClassId() == classId) {
				return player;
			}
		}
		return null;
	}

	public boolean getWannaToChangeThisPlayer(int objectID) {
		for (Player player : wannaToChangeThisPlayer) {
			if (player == null) {
				continue;
			}

			if (player.getObjectId() == objectID) {
				return true;
			}
		}
		return false;
	}

	public Player getPlayerFromChange(int level, int classId) {
		for (Player player : wannaToChangeThisPlayer) {
			if (player.getLevel() == level && player.getClassId() == classId) {
				return player;
			}
		}
		return null;
	}

	public static PartySearchManager getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final PartySearchManager instance = new PartySearchManager();
	}
}
