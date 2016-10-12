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

import java.util.ArrayList;

/**
 * @author Erlandys
 */
public class PartySearchManager
{
	ArrayList<L2PcInstance> lookingForParty;
	ArrayList<L2PcInstance> wannaToChangeThisPlayer;

	public PartySearchManager()
	{
		load();
	}

	public void load()
	{
		if (lookingForParty == null)
		{
			lookingForParty = new ArrayList<>();
		}
		else
		{
			lookingForParty.clear();
		}
		if (wannaToChangeThisPlayer == null)
		{
			wannaToChangeThisPlayer = new ArrayList<>();
		}
		else
		{
			wannaToChangeThisPlayer.clear();
		}
	}

	public void addLookingForParty(L2PcInstance player)
	{
		lookingForParty.add(player);
	}

	public void addChangeThisPlayer(L2PcInstance player)
	{
		wannaToChangeThisPlayer.add(player);
	}

	public ArrayList<L2PcInstance> getLookingForPartyPlayers()
	{
		return lookingForParty;
	}

	public ArrayList<L2PcInstance> getWannaToChangeThisPlayers()
	{
		return wannaToChangeThisPlayer;
	}

	public void removeLookingForParty(L2PcInstance player)
	{
		lookingForParty.remove(player);
	}

	public void removeChangeThisPlayer(L2PcInstance player)
	{
		wannaToChangeThisPlayer.remove(player);
	}

	public L2PcInstance getLookingForParty(int level, int classId)
	{
		for (L2PcInstance player : lookingForParty)
		{
			if (player == null)
			{
				continue;
			}

			if (player.getLevel() == level && player.getClassId() == classId)
			{
				return player;
			}
		}
		return null;
	}

	public L2PcInstance getWannaToChangeThisPlayer(int level, int classId)
	{
		for (L2PcInstance player : wannaToChangeThisPlayer)
		{
			if (player.getLevel() == level && player.getClassId() == classId)
			{
				return player;
			}
		}
		return null;
	}

	public boolean getWannaToChangeThisPlayer(int objectID)
	{
		for (L2PcInstance player : wannaToChangeThisPlayer)
		{
			if (player == null)
			{
				continue;
			}

			if (player.getObjectId() == objectID)
			{
				return true;
			}
		}
		return false;
	}

	public L2PcInstance getPlayerFromChange(int level, int classId)
	{
		for (L2PcInstance player : wannaToChangeThisPlayer)
		{
			if (player.getLevel() == level && player.getClassId() == classId)
			{
				return player;
			}
		}
		return null;
	}

	public static PartySearchManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PartySearchManager _instance = new PartySearchManager();
	}
}
