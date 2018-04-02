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
import l2server.gameserver.model.Abnormal;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExMentorList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

/**
 * @author Erlandys
 */
public class RequestMentorCancel extends L2GameClientPacket {
	private static Logger log = LoggerFactory.getLogger(RequestMentorCancel.class.getName());


	
	private String name;
	boolean isMentor;
	
	@Override
	protected void readImpl() {
		isMentor = readD() == 1;
		name = readS();
	}
	
	@Override
	protected void runImpl() {
		SystemMessage sm;
		
		Player activeChar = getClient().getActiveChar();
		if (activeChar == null) {
			return;
		}
		
		int id = CharNameTable.getInstance().getIdByName(name);
		
		Connection con = null;
		
		try {
			if (!isMentor) {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement("DELETE FROM character_mentees WHERE (charId=? AND menteeId=?)");
				statement.setInt(1, id);
				statement.setInt(2, activeChar.getObjectId());
				statement.execute();
				statement.close();
				
				// Mentee cancelled mentoring with mentor
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED);
				sm.addString(name);
				activeChar.sendPacket(sm);
				
				for (Abnormal e : activeChar.getAllEffects()) {
					if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233) {
						e.exit();
					}
				}
				activeChar.removeSkill(9379);
				if (World.getInstance().getPlayer(id) != null) {
					Player player = World.getInstance().getPlayer(id);
					player.sendPacket(new ExMentorList(player));
					sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_BOND_WITH_A_NEW_MENTEE_IN_S1_DAY_S2_HOUR_S3_MINUTE);
					sm.addString("0"); // TODO: Days
					sm.addString("0"); // TODO: Hours
					sm.addString("0"); // TODO: Minutes
					player.sendPacket(sm);
					player.giveMentorBuff();
				}
				activeChar.sendPacket(new ExMentorList(activeChar));
			} else {
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement;
				statement = con.prepareStatement("DELETE FROM character_mentees WHERE (charId=? AND menteeId=?)");
				statement.setInt(1, activeChar.getObjectId());
				statement.setInt(2, id);
				statement.execute();
				statement.close();
				
				// Mentor cancelled mentoring with mentee
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_CAN_BOND_WITH_A_NEW_MENTEE_IN_S1_DAY_S2_HOUR_S3_MINUTE);
				sm.addString("0"); // TODO: Days
				sm.addString("0"); // TODO: Hours
				sm.addString("0"); // TODO: Minutes
				activeChar.sendPacket(sm);
				
				activeChar.sendPacket(new ExMentorList(activeChar));
				if (World.getInstance().getPlayer(id) != null) {
					Player player = World.getInstance().getPlayer(id);
					player.sendPacket(new ExMentorList(player));
					sm = SystemMessage.getSystemMessage(SystemMessageId.THE_MENTORING_RELATIONSHIP_WITH_S1_HAS_BEEN_CANCELED);
					sm.addString(activeChar.getName());
					player.sendPacket(sm);
					for (Abnormal e : player.getAllEffects()) {
						if (e.getSkill().getId() >= 9227 && e.getSkill().getId() <= 9233) {
							e.exit();
						}
					}
					player.removeSkill(9379);
				}
				activeChar.giveMentorBuff();
			}
		} catch (Exception e) {
			log.warn("could not del friend objectid: ", e);
		} finally {
			L2DatabaseFactory.close(con);
		}
	}
}
