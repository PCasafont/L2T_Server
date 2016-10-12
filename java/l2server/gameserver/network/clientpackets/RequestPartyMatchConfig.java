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

	private int _auto, _loc, _lvl;

	@Override
	protected void readImpl()
	{
		_auto = readD(); //
		_loc = readD(); // Location
		_lvl = readD(); // my level
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance _activeChar = getClient().getActiveChar();

		if (_activeChar == null)
		{
			return;
		}

		if (!_activeChar.isInPartyMatchRoom() && _activeChar.getParty() != null &&
				_activeChar.getParty().getLeader() != _activeChar)
		{
			_activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_VIEW_PARTY_ROOMS));
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (_activeChar.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			PartyMatchRoomList _list = PartyMatchRoomList.getInstance();
			if (_list == null)
			{
				return;
			}

			PartyMatchRoom _room = _list.getPlayerRoom(_activeChar);
			if (_room == null)
			{
				return;
			}

			_activeChar.sendPacket(new PartyMatchDetail(_activeChar, _room));
			_activeChar.sendPacket(new ExPartyRoomMembers(_activeChar, _room, 2));

			_activeChar.setPartyRoom(_room.getId());
			//_activeChar.setPartyMatching(1);
			_activeChar.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(_activeChar);

			// Send Room list
			ListPartyWaiting matchList = new ListPartyWaiting(_activeChar, _auto, _loc, _lvl);

			_activeChar.sendPacket(matchList);
		}
	}
}
