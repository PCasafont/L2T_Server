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

package l2server.gameserver.network.serverpackets;

import l2server.DatabasePool;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class ExMentorList extends L2GameServerPacket {
	private static Logger log = LoggerFactory.getLogger(ExMentorList.class.getName());

	private class PartnerInfo {
		public int objId;
		public String name;
		public int classId;
		public int level;
		public boolean online;
	}
	
	private Player player;
	private List<PartnerInfo> partners = new ArrayList<>();
	
	public ExMentorList(Player activeChar) {
		if (activeChar.isMentor()) {
			for (int objId : activeChar.getMenteeList()) {
				PartnerInfo partnerInfo = new PartnerInfo();
				partnerInfo.objId = objId;
				Player partner = World.getInstance().getPlayer(objId);
				if (partner != null) {
					partnerInfo.name = partner.getName();
					partnerInfo.classId = partner.getClassId();
					partnerInfo.level = partner.getLevel();
					partnerInfo.online = true;
				} else {
					partnerInfo.name = CharNameTable.getInstance().getNameById(objId);
					getClassIdAndLevel(partnerInfo);
					partnerInfo.online = false;
				}
				partners.add(partnerInfo);
			}
		} else if (activeChar.isMentee()) {
			PartnerInfo partnerInfo = new PartnerInfo();
			partnerInfo.objId = activeChar.getMentorId();
			Player partner = World.getInstance().getPlayer(activeChar.getMentorId());
			if (partner != null) {
				partnerInfo.name = partner.getName();
				partnerInfo.classId = partner.getClassId();
				partnerInfo.level = partner.getLevel();
				partnerInfo.online = true;
			} else {
				partnerInfo.name = CharNameTable.getInstance().getNameById(activeChar.getMentorId());
				getClassIdAndLevel(partnerInfo);
				partnerInfo.online = false;
			}
			partners.add(partnerInfo);
		}
		
		player = activeChar;
	}
	
	private void getClassIdAndLevel(PartnerInfo partnerInfo) {
		Connection con = null;
		try {
			// Retrieve the Player from the characters table of the database
			con = DatabasePool.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT level, classid, base_class FROM characters WHERE charId=?");
			statement.setInt(1, partnerInfo.objId);
			ResultSet rset = statement.executeQuery();
			while (rset.next()) {
				partnerInfo.level = rset.getByte("level");
				partnerInfo.classId = rset.getInt("classid");
			}
			rset.close();
			statement.close();
		} catch (Exception e) {
			log.info("Failed loading character.");
		} finally {
			DatabasePool.close(con);
		}
	}
	
	@Override
	protected final void writeImpl() {
		writeD(player.isMentor() ? 0x01 : player.isMentee() ? 0x02 : 0x00); // 0x00 Nothing, 0x01 my mentees, 0x02 my mentor
		writeD(0x00); // ???
		writeD(partners.size());
		for (PartnerInfo menteeInfo : partners) {
			writeD(menteeInfo.objId);
			writeS(menteeInfo.name);
			writeD(menteeInfo.classId);
			writeD(menteeInfo.level);
			writeD(menteeInfo.online ? 0x01 : 0x00);
		}
	}
}
