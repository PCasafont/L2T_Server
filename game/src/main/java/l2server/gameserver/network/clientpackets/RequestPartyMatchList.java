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
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExPartyRoomMembers;
import l2server.gameserver.network.serverpackets.PartyMatchDetail;
import l2server.gameserver.network.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author: Gnacik
 */

public class RequestPartyMatchList extends L2GameClientPacket {
	private static Logger log = LoggerFactory.getLogger(RequestPartyMatchList.class.getName());


	
	private int roomid;
	private int membersmax;
	private int lvlmin;
	private int lvlmax;
	private int loot;
	private String roomtitle;
	
	@Override
	protected void readImpl() {
		roomid = readD();
		membersmax = readD();
		lvlmin = readD();
		lvlmax = readD();
		loot = readD();
		roomtitle = readS();
	}
	
	@Override
	protected void runImpl() {
		Player activeChar = getClient().getActiveChar();
		
		if (activeChar == null) {
			return;
		}
		
		if (roomid > 0) {
			PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(roomid);
			if (room != null) {
				log.info("PartyMatchRoom #" + room.getId() + " changed by " + activeChar.getName());
				room.setMaxMembers(membersmax);
				room.setMinLvl(lvlmin);
				room.setMaxLvl(lvlmax);
				room.setLootType(loot);
				room.setTitle(roomtitle);
				
				for (Player member : room.getPartyMembers()) {
					if (member == null) {
						continue;
					}
					
					member.sendPacket(new PartyMatchDetail(activeChar, room));
					member.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_ROOM_REVISED));
				}
			}
		} else {
			int maxid = PartyMatchRoomList.getInstance().getMaxId();
			
			PartyMatchRoom room = new PartyMatchRoom(maxid, roomtitle, loot, lvlmin, lvlmax, membersmax, activeChar);
			
			log.info("PartyMatchRoom #" + maxid + " created by " + activeChar.getName());
			// Remove from waiting list
			PartyMatchWaitingList.getInstance().removePlayer(activeChar);
			
			PartyMatchRoomList.getInstance().addPartyMatchRoom(maxid, room);
			
			if (activeChar.isInParty()) {
				for (Player ptmember : activeChar.getParty().getPartyMembers()) {
					if (ptmember == null) {
						continue;
					}
					if (ptmember == activeChar) {
						continue;
					}
					
					ptmember.setPartyRoom(maxid);
					//ptmember.setPartyMatching(1);
					
					room.addMember(ptmember);
				}
			}
			activeChar.sendPacket(new PartyMatchDetail(activeChar, room));
			activeChar.sendPacket(new ExPartyRoomMembers(activeChar, room, 1));
			
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_ROOM_CREATED));
			
			activeChar.setPartyRoom(maxid);
			//activeChar.setPartyMatching(1);
			activeChar.broadcastUserInfo();
		}
	}
}
