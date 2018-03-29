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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExClosePartyRoom;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gnacik
 */
public class PartyMatchRoomList
{
	private int maxid = 1;
	private Map<Integer, PartyMatchRoom> rooms;

	private PartyMatchRoomList()
	{
		rooms = new HashMap<>();
	}

	public synchronized void addPartyMatchRoom(int id, PartyMatchRoom room)
	{
		rooms.put(id, room);
		maxid++;
	}

	public void deleteRoom(int id)
	{
		for (L2PcInstance member : getRoom(id).getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}

			member.sendPacket(new ExClosePartyRoom());
			member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_ROOM_DISBANDED));

			member.setPartyRoom(0);
			//member.setPartyMatching(0);
			member.broadcastUserInfo();
		}
		rooms.remove(id);
	}

	public PartyMatchRoom getRoom(int id)
	{
		return rooms.get(id);
	}

	public PartyMatchRoom[] getRooms()
	{
		return rooms.values().toArray(new PartyMatchRoom[rooms.size()]);
	}

	public int getPartyMatchRoomCount()
	{
		return rooms.size();
	}

	public int getMaxId()
	{
		return maxid;
	}

	public PartyMatchRoom getPlayerRoom(L2PcInstance player)
	{
		for (PartyMatchRoom room : rooms.values())
		{
			for (L2PcInstance member : room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return room;
				}
			}
		}
		return null;
	}

	public int getPlayerRoomId(L2PcInstance player)
	{
		for (PartyMatchRoom room : rooms.values())
		{
			for (L2PcInstance member : room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return room.getId();
				}
			}
		}
		return -1;
	}

	public static PartyMatchRoomList getInstance()
	{
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PartyMatchRoomList instance = new PartyMatchRoomList();
	}
}
