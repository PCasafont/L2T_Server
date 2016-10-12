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
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.FriendList;
import l2server.gameserver.network.serverpackets.FriendPacket;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestFriendDel extends L2GameClientPacket
{

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		SystemMessage sm;

		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		int id = CharNameTable.getInstance().getIdByName(_name);

		if (id == -1)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}

		if (!activeChar.getFriendList().contains(id))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_NOT_ON_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);
			return;
		}

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement(
					"DELETE FROM character_friends WHERE (charId=? AND friendId=?) OR (charId=? AND friendId=?)");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, id);
			statement.setInt(4, activeChar.getObjectId());
			statement.execute();
			statement.close();

			// Player deleted from your friendlist
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST);
			sm.addString(_name);
			activeChar.sendPacket(sm);

			activeChar.getFriendList().remove(new Integer(id));
			activeChar.sendPacket(new FriendPacket(false, id, activeChar));
			activeChar.sendPacket(new FriendList(activeChar));

			L2PcInstance player = L2World.getInstance().getPlayer(_name);
			if (player != null)
			{
				activeChar.removeFriendMemo(player.getObjectId());
				player.getFriendList().remove(Integer.valueOf(activeChar.getObjectId()));
				player.removeFriendMemo(activeChar.getObjectId());
				player.sendPacket(new FriendPacket(false, activeChar.getObjectId(), player));
				player.sendPacket(new FriendList(player));
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "could not del friend objectid: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
