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

package l2server.gameserver.network.clientpackets;

import l2server.gameserver.model.PartyMatchRoom;
import l2server.gameserver.model.PartyMatchRoomList;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class RequestDismissPartyRoom extends L2GameClientPacket
{

	private int roomid;
	@SuppressWarnings("unused")
	private int data2;

	@Override
	protected void readImpl()
	{
		roomid = readD();
		data2 = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomid);

		if (room == null)
		{
			return;
		}

		PartyMatchRoomList.getInstance().deleteRoom(roomid);
	}
}
