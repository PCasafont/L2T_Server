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

import l2server.L2DatabaseFactory;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.FriendList;
import l2server.gameserver.network.serverpackets.FriendPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * sample
 * 5F
 * 01 00 00 00
 * <p>
 * format  cdd
 *
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAnswerFriendInvite extends L2GameClientPacket {
	
	private int response;
	
	@Override
	protected void readImpl() {
		readC(); // Unknown, usually 1
		response = readD();
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player != null) {
			Player requestor = player.getActiveRequester();
			if (requestor == null) {
				return;
			}
			
			if (response == 1) {
				Connection con = null;
				try {
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId) VALUES (?, ?), (?, ?)");
					statement.setInt(1, requestor.getObjectId());
					statement.setInt(2, player.getObjectId());
					statement.setInt(3, player.getObjectId());
					statement.setInt(4, requestor.getObjectId());
					statement.execute();
					statement.close();
					SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND);
					requestor.sendPacket(msg);
					
					//Player added to your friendlist
					msg = SystemMessage.getSystemMessage(SystemMessageId.S1_ADDED_TO_FRIENDS);
					msg.addString(player.getName());
					requestor.sendPacket(msg);
					requestor.getFriendList().add(player.getObjectId());
					
					msg = SystemMessage.getSystemMessage(SystemMessageId.S1_JOINED_AS_FRIEND);
					msg.addString(requestor.getName());
					player.sendPacket(msg);
					player.getFriendList().add(requestor.getObjectId());
					
					//Send notificacions for both player in order to show them online
					player.sendPacket(new FriendPacket(true, requestor.getObjectId(), player));
					requestor.sendPacket(new FriendPacket(true, player.getObjectId(), requestor));
					player.sendPacket(new FriendList(player));
					requestor.sendPacket(new FriendList(requestor));
				} catch (Exception e) {
					log.warn("Could not add friend objectid: " + e.getMessage(), e);
				} finally {
					L2DatabaseFactory.close(con);
				}
			} else {
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_INVITE_A_FRIEND);
				requestor.sendPacket(msg);
			}
			
			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
}
