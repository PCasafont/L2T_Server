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
import l2server.gameserver.network.serverpackets.ExManagePartyRoomMember;
import l2server.gameserver.network.serverpackets.ExPartyRoomMembers;
import l2server.gameserver.network.serverpackets.PartyMatchDetail;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */

public final class RequestPartyMatchDetail extends L2GameClientPacket {
	private int roomid;
	@SuppressWarnings("unused")
	private int unk1;
	@SuppressWarnings("unused")
	private int unk2;
	@SuppressWarnings("unused")
	private int unk3;
	
	@Override
	protected void readImpl() {
		roomid = readD();
		/*
		 * IF player click on Room all unk are 0
		 * IF player click AutoJoin values are -1 1 1
		 */
		unk1 = readD();
		unk2 = readD();
		unk3 = readD();
	}
	
	@Override
	protected void runImpl() {
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomid);
		if (room == null) {
			return;
		}
		
		if (activeChar.getLevel() >= room.getMinLvl() && activeChar.getLevel() <= room.getMaxLvl()) {
			// Remove from waiting list
			PartyMatchWaitingList.getInstance().removePlayer(activeChar);
			
			activeChar.setPartyRoom(roomid);
			
			activeChar.sendPacket(new PartyMatchDetail(activeChar, room));
			activeChar.sendPacket(new ExPartyRoomMembers(activeChar, room, 0));
			
			for (L2PcInstance member : room.getPartyMembers()) {
				if (member == null) {
					continue;
				}
				
				member.sendPacket(new ExManagePartyRoomMember(activeChar, room, 0));
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ENTERED_PARTY_ROOM);
				sm.addCharName(activeChar);
				member.sendPacket(sm);
			}
			room.addMember(activeChar);
			
			// Info Broadcast
			activeChar.broadcastUserInfo();
		} else {
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_ENTER_PARTY_ROOM));
		}
	}
}
