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

import l2server.Config;
import l2server.gameserver.model.L2Party.messageType;
import l2server.gameserver.model.PartyMatchRoom;
import l2server.gameserver.model.PartyMatchRoomList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExManagePartyRoomMember;
import l2server.gameserver.network.serverpackets.JoinParty;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * sample
 * 2a
 * 01 00 00 00
 * <p>
 * format  cdd
 *
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAnswerJoinParty extends L2GameClientPacket
{
	//

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}

		requestor.sendPacket(new JoinParty(_response));

		if (_response == 1)
		{
			if (requestor.isInParty() && requestor.getParty().getMemberCount() >= Config.MAX_MEMBERS_IN_PARTY)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PARTY_FULL);
				player.sendPacket(sm);
				requestor.sendPacket(sm);
				return;
			}

			player.joinParty(requestor.getParty());

			if (requestor.isInPartyMatchRoom() && player.isInPartyMatchRoom())
			{
				final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if (list != null && list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player))
				{
					final PartyMatchRoom room = list.getPlayerRoom(requestor);
					if (room != null)
					{
						final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
						for (L2PcInstance member : room.getPartyMembers())
						{
							if (member != null)
							{
								member.sendPacket(packet);
							}
						}
					}
				}
			}
			else if (requestor.isInPartyMatchRoom() && !player.isInPartyMatchRoom())
			{
				final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if (list != null)
				{
					final PartyMatchRoom room = list.getPlayerRoom(requestor);
					if (room != null)
					{
						room.addMember(player);
						ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
						for (L2PcInstance member : room.getPartyMembers())
						{
							if (member != null)
							{
								member.sendPacket(packet);
							}
						}
						player.setPartyRoom(room.getId());
						//player.setPartyMatching(1);
						player.broadcastUserInfo();
					}
				}
			}
		}
		else if (_response == -1)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUEST);
			sm.addPcName(player);
			requestor.sendPacket(sm);

			//activate garbage collection if there are no other members in party (happens when we were creating new one)
			if (requestor.isInParty() && requestor.getParty().getMemberCount() == 1)
			{
				requestor.getParty().removePartyMember(requestor, messageType.None);
			}
		}
		else
		// 0
		{
			//requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PLAYER_DECLINED));

			//activate garbage collection if there are no other members in party (happens when we were creating new one)
			if (requestor.isInParty() && requestor.getParty().getMemberCount() == 1)
			{
				requestor.getParty().removePartyMember(requestor, messageType.None);
			}
		}

		if (requestor.isInParty())
		{
			requestor.getParty().setPendingInvitation(false); // if party is null, there is no need of decreasing
		}

		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
