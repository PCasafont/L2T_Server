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

public class DiscussionManager
{
	private List<Integer> _voted = new ArrayList<>();
	private int[] _votes = new int[10];
	private boolean _votesEnabled = false;
	private boolean _globalChatDisabled = false;

	public static DiscussionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public boolean vote(int objectId, byte option)
	{
		if (_voted.contains(objectId))
		{
			return false;
		}
		_voted.add(objectId);
		_votes[option]++;
		return true;
	}

	public void startVotations()
	{
		_voted.clear();
		for (int i = 0; i < _votes.length; i++)
		{
			_votes[i] = 0;
		}
		_votesEnabled = true;
	}

	public int[] endVotations()
	{
		_voted.clear();
		_votesEnabled = false;
		return _votes;
	}

	public boolean areVotesEnabled()
	{
		return _votesEnabled;
	}

	public void setGlobalChatDisabled(boolean chatDisabled)
	{
		_globalChatDisabled = chatDisabled;
	}

	public boolean isGlobalChatDisabled()
	{
		return _globalChatDisabled;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final DiscussionManager _instance = new DiscussionManager();
	}
}
