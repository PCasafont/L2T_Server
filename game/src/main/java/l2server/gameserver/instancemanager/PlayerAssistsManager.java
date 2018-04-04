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

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Pere
 */
public class PlayerAssistsManager {
	public class PlayerInfo {
		public Map<Player, Long> AttackTimers = new HashMap<>();
		public Map<Player, Long> HelpTimers = new HashMap<>();
	}

	Map<Integer, PlayerInfo> players = new HashMap<>();

	public void updateAttackTimer(Player attacker, Player target) {
		synchronized (players) {
			PlayerInfo playerInfo = players.get(target.getObjectId());
			if (playerInfo == null) {
				playerInfo = new PlayerInfo();
				players.put(target.getObjectId(), playerInfo);
			}

			synchronized (playerInfo) {
				long time = System.currentTimeMillis() + 10000L;
				playerInfo.AttackTimers.put(attacker, time);
			}
		}
	}

	public void updateHelpTimer(Player helper, Player target) {
		synchronized (players) {
			PlayerInfo playerInfo = players.get(target.getObjectId());
			if (playerInfo == null) {
				playerInfo = new PlayerInfo();
				players.put(target.getObjectId(), playerInfo);
			}

			synchronized (playerInfo) {
				long time = System.currentTimeMillis() + 10000L;
				playerInfo.HelpTimers.put(helper, time);
			}
		}
	}

	public List<Player> getAssistants(Player killer, Player victim, boolean killed) {
		long curTime = System.currentTimeMillis();
		Set<Player> assistants = new HashSet<>();
		if (killer != null && players.containsKey(killer.getObjectId())) {
			PlayerInfo killerInfo = players.get(killer.getObjectId());

			// Gather the assistants
			List<Player> toDeleteList = new ArrayList<>();
			for (Player assistant : killerInfo.HelpTimers.keySet()) {
				if (killerInfo.HelpTimers.get(assistant) > curTime) {
					assistants.add(assistant);
				} else {
					toDeleteList.add(assistant);
				}
			}

			// Delete unnecessary assistants
			for (Player toDelete : toDeleteList) {
				killerInfo.HelpTimers.remove(toDelete);
			}
		}

		if (victim != null && players.containsKey(victim.getObjectId())) {
			PlayerInfo victimInfo = players.get(victim.getObjectId());

			// Gather more assistants
			for (Player assistant : victimInfo.AttackTimers.keySet()) {
				if (victimInfo.AttackTimers.get(assistant) > curTime) {
					assistants.add(assistant);
					if (players.containsKey(assistant.getObjectId())) {
						PlayerInfo assistantInfo = players.get(assistant.getObjectId());

						// Gather the assistant's assistants
						List<Player> toDeleteList = new ArrayList<>();
						for (Entry<Player, Long> assistantsAssistant : assistantInfo.HelpTimers.entrySet()) {
							if (assistantsAssistant.getValue() > curTime) {
								assistants.add(assistantsAssistant.getKey());
							} else {
								toDeleteList.add(assistantsAssistant.getKey());
							}
						}

						// Delete unnecessary assistants
						for (Player toDelete : toDeleteList) {
							assistantInfo.HelpTimers.remove(toDelete);
						}
					}
				}
			}

			if (killed) {
				victimInfo.AttackTimers.clear();
			}
		}

		assistants.remove(killer);
		assistants.remove(victim);
		return new ArrayList<>(assistants);
	}

	public static PlayerAssistsManager getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final PlayerAssistsManager instance = new PlayerAssistsManager();
	}
}
