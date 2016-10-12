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
import l2server.L2DatabaseFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.L2FriendSay;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Recieve Private (Friend) Message - 0xCC
 * <p>
 * Format: c SS
 * <p>
 * S: Message
 * S: Receiving Player
 *
 * @author Tempy
 */
public final class RequestSendFriendMsg extends L2GameClientPacket
{
	//private static Logger _logChat = Logger.getLogger("chat");

	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		_message = readS();
		_reciever = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (_message == null || _message.isEmpty() || _message.length() > 300)
		{
			return;
		}

		final L2PcInstance targetPlayer = L2World.getInstance().getPlayer(_reciever);
		if (targetPlayer == null || !targetPlayer.getFriendList().contains(activeChar.getObjectId()))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
			return;
		}

		if (Config.LOG_CHAT)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = con.prepareStatement(
						"INSERT INTO log_chat(time, type, talker, listener, text) VALUES (?,?,?,?,?);");

				statement.setLong(1, System.currentTimeMillis());
				statement.setString(2, "TELL");
				statement.setString(3, activeChar.getName());
				statement.setString(4, _reciever);
				statement.setString(5, _message);
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			/*LogRecord record = new LogRecord(Level.INFO, _message);
            record.setLoggerName("chat");
			record.setParameters(new Object[]{"PRIV_MSG", "[" + activeChar.getName() + " to "+ _reciever +"]"});

			_logChat.log(record);*/
		}

		targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _reciever, _message));
	}
}
