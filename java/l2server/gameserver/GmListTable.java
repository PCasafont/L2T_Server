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

package l2server.gameserver;

import l2server.Config;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores references to all online game masters. (access level > 100)
 *
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable
{
	/**
	 * Set(L2PcInstance>) containing all the GM in game
	 */
	private ConcurrentHashMap<L2PcInstance, Boolean> gmList;

	public static GmListTable getInstance()
	{
		return SingletonHolder.instance;
	}

	public ArrayList<L2PcInstance> getAllGms(boolean includeHidden)
	{
		ArrayList<L2PcInstance> tmpGmList = new ArrayList<>();
		for (Entry<L2PcInstance, Boolean> n : gmList.entrySet())
		{
			if (includeHidden || !n.getValue())
			{
				tmpGmList.add(n.getKey());
			}
		}

		return tmpGmList;
	}

	public ArrayList<String> getAllGmNames(boolean includeHidden)
	{
		ArrayList<String> tmpGmList = new ArrayList<>();
		for (Entry<L2PcInstance, Boolean> n : gmList.entrySet())
		{
			if (!n.getValue())
			{
				tmpGmList.add(n.getKey().getName());
			}
			else if (includeHidden)
			{
				tmpGmList.add(n.getKey().getName() + " (invis)");
			}
		}

		return tmpGmList;
	}

	private GmListTable()
	{
		gmList = new ConcurrentHashMap<>();
	}

	/**
	 * Add a L2PcInstance player to the Set _gmList
	 */
	public void addGm(L2PcInstance player, boolean hidden)
	{
		if (Config.DEBUG)
		{
			Log.fine("added gm: " + player.getName());
		}
		gmList.put(player, hidden);
	}

	public void deleteGm(L2PcInstance player)
	{
		if (Config.DEBUG)
		{
			Log.fine("deleted gm: " + player.getName());
		}

		gmList.remove(player);
	}

	/**
	 * GM will be displayed on clients gmlist
	 *
	 * @param player
	 */
	public void showGm(L2PcInstance player)
	{
		if (gmList.containsKey(player))
		{
			gmList.put(player, false);
		}
	}

	/**
	 * GM will no longer be displayed on clients gmlist
	 *
	 * @param player
	 */
	public void hideGm(L2PcInstance player)
	{
		if (gmList.containsKey(player))
		{
			gmList.put(player, true);
		}
	}

	public boolean isGmOnline(boolean includeHidden)
	{
		for (boolean gmStatus : gmList.values())
		{
			if (includeHidden || !gmStatus)
			{
				return true;
			}
		}

		return false;
	}

	public void sendListToPlayer(L2PcInstance player)
	{
		if (isGmOnline(player.isGM()))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GM_LIST));

			for (String name : getAllGmNames(player.isGM()))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.GM_C1);
				sm.addString(name);
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW));
		}
	}

	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}

	public static void broadcastMessageToGMs(String message)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendMessage(message);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GmListTable instance = new GmListTable();
	}
}
