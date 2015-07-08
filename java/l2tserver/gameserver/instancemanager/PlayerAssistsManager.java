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
package l2tserver.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Pere
 */
public class PlayerAssistsManager
{
	public class PlayerInfo
	{
		public Map<L2PcInstance, Long> AttackTimers = new HashMap<L2PcInstance, Long>();
		public Map<L2PcInstance, Long> HelpTimers = new HashMap<L2PcInstance, Long>();
	}
	
	Map<Integer, PlayerInfo> _players = new HashMap<Integer, PlayerInfo>();
	
	public void updateAttackTimer(L2PcInstance attacker, L2PcInstance target)
	{
		synchronized (_players)
		{
			PlayerInfo attackerInfo = _players.get(attacker.getObjectId());
			if (attackerInfo == null)
			{
				attackerInfo = new PlayerInfo();
				_players.put(attacker.getObjectId(), attackerInfo);
			}
			
			synchronized (attackerInfo)
			{
				long time = System.currentTimeMillis() + 10000L;
				attackerInfo.AttackTimers.put(target, time);
			}
		}
	}
	
	public void updateHelpTimer(L2PcInstance helper, L2PcInstance target)
	{
		synchronized (_players)
		{
			PlayerInfo helperInfo = _players.get(helper.getObjectId());
			if (helperInfo == null)
			{
				helperInfo = new PlayerInfo();
				_players.put(helper.getObjectId(), helperInfo);
			}

			synchronized (helperInfo)
			{
				long time = System.currentTimeMillis() + 10000L;
				helperInfo.HelpTimers.put(target, time);
			}
		}
	}
	
	public List<L2PcInstance> getAssistants(L2PcInstance killer, L2PcInstance victim, boolean killed)
	{
		long curTime = System.currentTimeMillis();
		List<L2PcInstance> assistants = new ArrayList<L2PcInstance>();
		if (killer != null && _players.containsKey(killer.getObjectId()))
		{
			PlayerInfo killerInfo = _players.get(killer.getObjectId());
		
			// Gather the assistants
			List<L2PcInstance> toDeleteList = new ArrayList<L2PcInstance>();
			for (L2PcInstance assistant : killerInfo.HelpTimers.keySet())
			{
				if (killerInfo.HelpTimers.get(assistant) > curTime)
					assistants.add(assistant);
				else
					toDeleteList.add(assistant);
			}
			
			// Delete unnecessary assistants
			for (L2PcInstance toDelete : toDeleteList)
				killerInfo.HelpTimers.remove(toDelete);
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
						List<L2PcInstance> toDeleteList = new ArrayList<L2PcInstance>();
						for (L2PcInstance assistantsAssistant : assistantInfo.HelpTimers.keySet())
						{
							if (assistantInfo.HelpTimers.get(assistant) > curTime)
								assistants.add(assistantsAssistant);
							else
								toDeleteList.add(assistantsAssistant);
						}
						
						// Delete unnecessary assistants
						for (L2PcInstance toDelete : toDeleteList)
							assistantInfo.HelpTimers.remove(toDelete);
					}
				}
			}
			
			if (killed)
				victimInfo.AttackTimers.clear();
		}
		
		return assistants;
	}
	
	public static final PlayerAssistsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PlayerAssistsManager _instance = new PlayerAssistsManager();
	}
}
