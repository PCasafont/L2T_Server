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
import l2server.gameserver.model.PartyMatchWaitingList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:30 $
 */

public final class RequestPartyMatchConfig extends L2GameClientPacket
{

	private int auto, loc, lvl;

	@Override
	protected void readImpl()
	{
		auto = readD(); //
		loc = readD(); // Location
		lvl = readD(); // my level
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		if (!activeChar.isInPartyMatchRoom() && activeChar.getParty() != null &&
				activeChar.getParty().getLeader() != activeChar)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_VIEW_PARTY_ROOMS));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			PartyMatchRoomList list = PartyMatchRoomList.getInstance();
			if (list == null)
			{
				return;
			}

			PartyMatchRoom room = list.getPlayerRoom(activeChar);
			if (room == null)
			{
				return;
			}

			activeChar.sendPacket(new PartyMatchDetail(activeChar, room));
			activeChar.sendPacket(new ExPartyRoomMembers(activeChar, room, 2));

			activeChar.setPartyRoom(room.getId());
			//_activeChar.setPartyMatching(1);
			activeChar.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(activeChar);

			// Send Room list
			ListPartyWaiting matchList = new ListPartyWaiting(activeChar, auto, loc, lvl);

			activeChar.sendPacket(matchList);
		}
	}
}
