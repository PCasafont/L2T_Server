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

import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.FriendAddRequest;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendInvite extends L2GameClientPacket
{
	//

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		final L2PcInstance friend = L2World.getInstance().getPlayer(_name);

		SystemMessage sm;

		// can't use friend invite for locating invisible characters
		if (friend == null || !friend.isOnline() || friend.getAppearance().getInvisible())
		{
			//Target is not found in the game.
			activeChar.sendPacket(SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME);
			return;
		}
		else if (friend == activeChar)
		{
			//You cannot add yourself to your own friend list.
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ADD_YOURSELF_TO_OWN_FRIEND_LIST);
			return;
		}
		else if (BlockList.isBlocked(activeChar, friend))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.BLOCKED_C1);
			sm.addCharName(friend);
			activeChar.sendPacket(sm);
			return;
		}
		else if (BlockList.isBlocked(friend, activeChar))
		{
			activeChar.sendMessage("You are in target's block list.");
			return;
		}

		if (activeChar.getFriendList().contains(friend.getObjectId()))
		{
			// Player already is in your friendlist
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_IN_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}

		if (!friend.isProcessingRequest())
		{
			// requets to become friend
			activeChar.onTransactionRequest(friend);
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_REQUESTED_C1_TO_BE_FRIEND);
			sm.addString(_name);
			FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
			friend.sendPacket(ajf);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(_name);
		}

		activeChar.sendPacket(sm);
	}
}
