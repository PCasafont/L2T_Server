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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.PartyMatchRoom;
import l2server.gameserver.model.PartyMatchRoomList;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;

/**
 * @author Gnacik
 */
public class ListPartyWaiting extends L2GameServerPacket
{
	private L2PcInstance _cha;
	private int _loc;
	private int _lim;
	private ArrayList<PartyMatchRoom> _rooms;

	public ListPartyWaiting(L2PcInstance player, int auto, int location, int limit)
	{
		_cha = player;
		_loc = location;
		_lim = limit;
		_rooms = new ArrayList<>();
		for (PartyMatchRoom room : PartyMatchRoomList.getInstance().getRooms())
		{
			if (room.getMembers() < 1 || room.getOwner() == null || !room.getOwner().isOnline() ||
					room.getOwner().getPartyRoom() != room.getId())
			{
				PartyMatchRoomList.getInstance().deleteRoom(room.getId());
				continue;
			}
			if (_loc > 0 && _loc != room.getLocation())
			{
				continue;
			}
			if (_lim == 0 && (_cha.getLevel() < room.getMinLvl() || _cha.getLevel() > room.getMaxLvl()))
			{
				continue;
			}
			_rooms.add(room);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_rooms.size() > 0 ? 1 : 0);

		writeD(_rooms.size());
		for (PartyMatchRoom room : _rooms)
		{
			writeD(room.getId());
			writeS(room.getTitle());
			writeD(room.getLocation());
			writeD(room.getMinLvl());
			writeD(room.getMaxLvl());
			writeD(room.getMaxMembers());
			writeS(room.getOwner().getName());
			writeD(room.getMembers());
			for (L2PcInstance member : room.getPartyMembers())
			{
				writeD(member.getClassId());
				writeS(member.getName());
			}
		}
	}
}
