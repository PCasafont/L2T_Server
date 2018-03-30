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
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExClosePartyRoom;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public final class RequestWithdrawPartyRoom extends L2GameClientPacket {
	private int roomid;
	@SuppressWarnings("unused")
	private int unk1;
	
	@Override
	protected void readImpl() {
		roomid = readD();
		unk1 = readD();
	}
	
	@Override
	protected void runImpl() {
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null) {
			return;
		}
		
		PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomid);
		if (room == null) {
			return;
		}
		
		if (activeChar.isInParty() && room.getOwner().isInParty() &&
				activeChar.getParty().getPartyLeaderOID() == room.getOwner().getParty().getPartyLeaderOID()) {
			// If user is in party with Room Owner
			// is not removed from Room
			
			//activeChar.setPartyMatching(0);
			activeChar.broadcastUserInfo();
		} else {
			room.deleteMember(activeChar);
			
			activeChar.setPartyRoom(0);
			//activeChar.setPartyMatching(0);
			
			activeChar.sendPacket(new ExClosePartyRoom());
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_ROOM_EXITED));
		}
	}
}
