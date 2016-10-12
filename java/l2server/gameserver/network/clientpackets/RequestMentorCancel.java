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
import l2server.gameserver.model.L2Abnormal;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExMentorList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

/**
 * @author Erlandys
 */
public class RequestMentorCancel extends L2GameClientPacket
{

	private String _name;
	boolean _isMentor;

	@Override
	protected void readImpl()
	{
		_isMentor = readD() == 1;
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

		Connection con = null;

		try
		{
			if (!_isMentor)
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement("DELETE FROM character_mentees WHERE (charId=? AND menteeId=?)");
				statement.setInt(1, id);
				statement.setInt(2, activeChar.getObjectId());
				statement.execute();
				statement.close();

				// Mentee cancelled mentoring with mentor
				sm = SystemMessage
						.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED);
				sm.addString(_name);
				activeChar.sendPacket(sm);

				for (L2Abnormal e : activeChar.getAllEffects())
				{
					if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233)
					{
						e.exit();
					}
				}
				activeChar.removeSkill(9379);
				if (L2World.getInstance().getPlayer(id) != null)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(id);
					player.sendPacket(new ExMentorList(player));
					sm = SystemMessage.getSystemMessage(
							SystemMessageId.YOU_CAN_BOND_WITH_A_NEW_MENTEE_IN_S1_DAY_S2_HOUR_S3_MINUTE);
					sm.addString("0"); // TODO: Days
					sm.addString("0"); // TODO: Hours
					sm.addString("0"); // TODO: Minutes
					player.sendPacket(sm);
					player.giveMentorBuff();
				}
				activeChar.sendPacket(new ExMentorList(activeChar));
			}
			else
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement("DELETE FROM character_mentees WHERE (charId=? AND menteeId=?)");
				statement.setInt(1, activeChar.getObjectId());
				statement.setInt(2, id);
				statement.execute();
				statement.close();

				// Mentor cancelled mentoring with mentee
				sm = SystemMessage
						.getSystemMessage(SystemMessageId.YOU_CAN_BOND_WITH_A_NEW_MENTEE_IN_S1_DAY_S2_HOUR_S3_MINUTE);
				sm.addString("0"); // TODO: Days
				sm.addString("0"); // TODO: Hours
				sm.addString("0"); // TODO: Minutes
				activeChar.sendPacket(sm);

				activeChar.sendPacket(new ExMentorList(activeChar));
				if (L2World.getInstance().getPlayer(id) != null)
				{
					L2PcInstance player = L2World.getInstance().getPlayer(id);
					player.sendPacket(new ExMentorList(player));
					sm = SystemMessage
							.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED);
					sm.addString(activeChar.getName());
					player.sendPacket(sm);
					for (L2Abnormal e : player.getAllEffects())
					{
						if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233)
						{
							e.exit();
						}
					}
					player.removeSkill(9379);
				}
				activeChar.giveMentorBuff();
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
