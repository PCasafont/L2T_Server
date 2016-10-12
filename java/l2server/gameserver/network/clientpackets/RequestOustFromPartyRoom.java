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

import l2server.gameserver.model.L2World;
import l2server.gameserver.model.PartyMatchRoom;
import l2server.gameserver.model.PartyMatchRoomList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExClosePartyRoom;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * format (ch) d
 *
 * @author -Wooden-
 */
public final class RequestOustFromPartyRoom extends L2GameClientPacket
{

	private int _charid;

	@Override
	protected void readImpl()
	{
		_charid = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2PcInstance member = L2World.getInstance().getPlayer(_charid);
		if (member == null)
		{
			return;
		}

		PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		if (_room == null)
		{
			return;
		}

		if (_room.getOwner() != activeChar)
		{
			return;
		}

		if (activeChar.isInParty() && member.isInParty() &&
				activeChar.getParty().getPartyLeaderOID() == member.getParty().getPartyLeaderOID())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER));
		}
		else
		{
			_room.deleteMember(member);
			member.setPartyRoom(0);
			member.sendPacket(new ExClosePartyRoom());
			member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OUSTED_FROM_PARTY_ROOM));
		}
	}
}
