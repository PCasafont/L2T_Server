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
import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;
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
public class ConfirmMenteeAdd extends L2GameClientPacket
{
	int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			L2PcInstance requestor = player.getActiveRequester();
			if (requestor == null)
			{
				return;
			}

			if (_response == 1)
			{
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement =
							con.prepareStatement("INSERT INTO character_mentees (charId, menteeId) VALUES (?, ?)");
					statement.setInt(1, requestor.getObjectId());
					statement.setInt(2, player.getObjectId());
					statement.execute();
					statement.close();
					SystemMessage msg;

					// Mentor has added player as mentee
					msg = SystemMessage.getSystemMessage(SystemMessageId.FROM_NOW_ON_S1_WILL_BE_YOUR_MENTEE);
					msg.addString(player.getName());
					requestor.sendPacket(msg);
					requestor.getMenteeList().add(player.getObjectId());

					// Player is now mentors mentee.
					msg = SystemMessage.getSystemMessage(SystemMessageId.FROM_NOW_ON_S1_WILL_BE_YOUR_MENTOR);
					msg.addString(requestor.getName());
					player.sendPacket(msg);

					// Send information to both players about them mentees/mentors
					requestor.sendPacket(new ExMentorList(requestor));
					player.sendPacket(new ExMentorList(player));
					player.setMentorId(requestor.getObjectId());

					L2Skill s = SkillTable.getInstance().getInfo(9379, 1);
					player.addSkill(s, false); //Dont Save Mentee skill to database
					requestor.giveMentorBuff();
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Could not add friend objectid: " + e.getMessage(), e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
			else
			{
				SystemMessage msg =
						SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DECLINED_BECOMING_YOUR_MENTEE);
				msg.addCharName(player);
				requestor.sendPacket(msg);
				SystemMessage msg1 =
						SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_DECLINED_S1_MENTORING_OFFER);
				msg1.addCharName(requestor);
				player.sendPacket(msg1);
			}

			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
}
