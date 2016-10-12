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

import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Pere
 */
public class PlayerAssistsManager
{
	public class PlayerInfo
	{
		public Map<L2PcInstance, Long> AttackTimers = new HashMap<>();
		public Map<L2PcInstance, Long> HelpTimers = new HashMap<>();
	}

	Map<Integer, PlayerInfo> _players = new HashMap<>();

	public void updateAttackTimer(L2PcInstance attacker, L2PcInstance target)
	{
		synchronized (_players)
		{
			PlayerInfo playerInfo = _players.get(target.getObjectId());
			if (playerInfo == null)
			{
				playerInfo = new PlayerInfo();
				_players.put(target.getObjectId(), playerInfo);
			}

			synchronized (playerInfo)
			{
				long time = System.currentTimeMillis() + 10000L;
				playerInfo.AttackTimers.put(attacker, time);
			}
		}
	}

	public void updateHelpTimer(L2PcInstance helper, L2PcInstance target)
	{
		synchronized (_players)
		{
			PlayerInfo playerInfo = _players.get(target.getObjectId());
			if (playerInfo == null)
			{
				playerInfo = new PlayerInfo();
				_players.put(target.getObjectId(), playerInfo);
			}

			synchronized (playerInfo)
			{
				long time = System.currentTimeMillis() + 10000L;
				playerInfo.HelpTimers.put(helper, time);
			}
		}
	}

	public List<L2PcInstance> getAssistants(L2PcInstance killer, L2PcInstance victim, boolean killed)
	{
		long curTime = System.currentTimeMillis();
		Set<L2PcInstance> assistants = new HashSet<>();
		if (killer != null && _players.containsKey(killer.getObjectId()))
		{
			PlayerInfo killerInfo = _players.get(killer.getObjectId());

			// Gather the assistants
			List<L2PcInstance> toDeleteList = new ArrayList<>();
			for (L2PcInstance assistant : killerInfo.HelpTimers.keySet())
			{
				if (killerInfo.HelpTimers.get(assistant) > curTime)
				{
					assistants.add(assistant);
				}
				else
				{
					toDeleteList.add(assistant);
				}
			}

			// Delete unnecessary assistants
			for (L2PcInstance toDelete : toDeleteList)
			{
				killerInfo.HelpTimers.remove(toDelete);
			}
		}

		if (victim != null && _players.containsKey(victim.getObjectId()))
		{
			PlayerInfo victimInfo = _players.get(victim.getObjectId());

			// Gather more assistants
			for (L2PcInstance assistant : victimInfo.AttackTimers.keySet())
			{
				if (victimInfo.AttackTimers.get(assistant) > curTime)
				{
					assistants.add(assistant);
					if (_players.containsKey(assistant.getObjectId()))
					{
						PlayerInfo assistantInfo = _players.get(assistant.getObjectId());

						// Gather the assistant's assistants
						List<L2PcInstance> toDeleteList = new ArrayList<>();
						for (Entry<L2PcInstance, Long> assistantsAssistant : assistantInfo.HelpTimers.entrySet())
						{
							if (assistantsAssistant.getValue() > curTime)
							{
								assistants.add(assistantsAssistant.getKey());
							}
							else
							{
								toDeleteList.add(assistantsAssistant.getKey());
							}
						}

						// Delete unnecessary assistants
						for (L2PcInstance toDelete : toDeleteList)
						{
							assistantInfo.HelpTimers.remove(toDelete);
						}
					}
				}
			}

			if (killed)
			{
				victimInfo.AttackTimers.clear();
			}
		}

		assistants.remove(killer);
		assistants.remove(victim);
		return new ArrayList<>(assistants);
	}

	public static PlayerAssistsManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerAssistsManager _instance = new PlayerAssistsManager();
	}
}
