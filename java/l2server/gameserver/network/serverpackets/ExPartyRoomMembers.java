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
	private final PartyMatchRoom room;
	private final int mode;

	public ExPartyRoomMembers(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		this.room = room;
		this.mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(mode);
		writeD(room.getMembers());
		for (L2PcInstance member : room.getPartyMembers())
		{
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD(member.getClassId());
			writeD(member.getLevel());
			writeD(TownManager.getClosestLocation(member));
			if (room.getOwner().equals(member))
			{
				writeD(1);
			}
			else if (room.getOwner().isInParty() && member.isInParty() &&
					room.getOwner().getParty().getPartyLeaderOID() == member.getParty().getPartyLeaderOID())
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
