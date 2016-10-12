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

import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.PartyMatchRoom;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class ExPartyRoomMembers extends L2GameServerPacket
{
	private final PartyMatchRoom _room;
	private final int _mode;

	public ExPartyRoomMembers(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		_room = room;
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_mode);
		writeD(_room.getMembers());
		for (L2PcInstance _member : _room.getPartyMembers())
		{
			writeD(_member.getObjectId());
			writeS(_member.getName());
			writeD(_member.getClassId());
			writeD(_member.getLevel());
			writeD(TownManager.getClosestLocation(_member));
			if (_room.getOwner().equals(_member))
			{
				writeD(1);
			}
			else if (_room.getOwner().isInParty() && _member.isInParty() &&
					_room.getOwner().getParty().getPartyLeaderOID() == _member.getParty().getPartyLeaderOID())
			{
				writeD(2);
			}
			else
			{
				writeD(0);
			}

			writeD(0); // ???
		}
	}
}
