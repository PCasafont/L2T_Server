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

import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.BlockListPacket;
import l2server.gameserver.network.serverpackets.FriendList;
import l2server.gameserver.network.serverpackets.FriendPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendList extends L2GameClientPacket
{
	//

	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		SystemMessage sm;

		// ======<Friend List>======
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_HEADER);

		L2PcInstance friend = null;
		for (int id : activeChar.getFriendList())
		{
			// int friendId = rset.getInt("friendId");
			String friendName = CharNameTable.getInstance().getNameById(id);

			if (friendName == null)
			{
				continue;
			}

			friend = L2World.getInstance().getPlayer(friendName);

			if (friend == null || !friend.isOnline())
			{
				// (Currently: Offline)
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_OFFLINE);
				sm.addString(friendName);
			}
			else
			{
				// (Currently: Online)
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ONLINE);
				sm.addString(friendName);
			}

			activeChar.sendPacket(sm);
		}

		// =========================
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
		activeChar.sendPacket(new FriendList(activeChar));
		if (activeChar.getFriendList().size() > 0)
		{
			for (int objId : activeChar.getFriendList())
			{
				activeChar.sendPacket(new FriendPacket(true, objId, activeChar));
			}
		}
		activeChar.sendPacket(new BlockListPacket(activeChar));
	}
}
