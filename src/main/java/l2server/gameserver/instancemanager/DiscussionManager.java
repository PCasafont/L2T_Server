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

import java.util.ArrayList;
import java.util.List;

public class DiscussionManager {
	private List<Integer> voted = new ArrayList<>();
	private int[] votes = new int[10];
	private boolean votesEnabled = false;
	private boolean globalChatDisabled = false;
	
	public static DiscussionManager getInstance() {
		return SingletonHolder.instance;
	}
	
	public boolean vote(int objectId, byte option) {
		if (voted.contains(objectId)) {
			return false;
		}
		voted.add(objectId);
		votes[option]++;
		return true;
	}
	
	public void startVotations() {
		voted.clear();
		for (int i = 0; i < votes.length; i++) {
			votes[i] = 0;
		}
		votesEnabled = true;
	}
	
	public int[] endVotations() {
		voted.clear();
		votesEnabled = false;
		return votes;
	}
	
	public boolean areVotesEnabled() {
		return votesEnabled;
	}
	
	public void setGlobalChatDisabled(boolean chatDisabled) {
		globalChatDisabled = chatDisabled;
	}
	
	public boolean isGlobalChatDisabled() {
		return globalChatDisabled;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final DiscussionManager instance = new DiscussionManager();
	}
}
