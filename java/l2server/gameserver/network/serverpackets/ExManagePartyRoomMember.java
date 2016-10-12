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
 *         <p>
 *         Mode :
 *         0 - add
 *         1 - modify
 *         2 - quit
 */
public class ExManagePartyRoomMember extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final PartyMatchRoom _room;
	private final int _mode;

	public ExManagePartyRoomMember(L2PcInstance player, PartyMatchRoom room, int mode)
	{
		_activeChar = player;
		_room = room;
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_mode);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getClassId());
		writeD(_activeChar.getLevel());
		writeD(TownManager.getClosestLocation(_activeChar));
		if (_room.getOwner().equals(_activeChar))
		{
			writeD(1);
		}
		else if (_room.getOwner().isInParty() && _activeChar.isInParty() &&
				_room.getOwner().getParty().getPartyLeaderOID() == _activeChar.getParty().getPartyLeaderOID())
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
