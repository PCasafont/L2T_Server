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

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.CharNameTable;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class ExMentorList extends L2GameServerPacket
{
	private class PartnerInfo
	{
		public int objId;
		public String name;
		public int classId;
		public int level;
		public boolean online;
	}

	private L2PcInstance _player;
	private List<PartnerInfo> _partners = new ArrayList<>();

	public ExMentorList(L2PcInstance activeChar)
	{
		if (activeChar.isMentor())
		{
			for (int objId : activeChar.getMenteeList())
			{
				PartnerInfo partnerInfo = new PartnerInfo();
				partnerInfo.objId = objId;
				L2PcInstance partner = L2World.getInstance().getPlayer(objId);
				if (partner != null)
				{
					partnerInfo.name = partner.getName();
					partnerInfo.classId = partner.getClassId();
					partnerInfo.level = partner.getLevel();
					partnerInfo.online = true;
				}
				else
				{
					partnerInfo.name = CharNameTable.getInstance().getNameById(objId);
					getClassIdAndLevel(partnerInfo);
					partnerInfo.online = false;
				}
				_partners.add(partnerInfo);
			}
		}
		else if (activeChar.isMentee())
		{
			PartnerInfo partnerInfo = new PartnerInfo();
			partnerInfo.objId = activeChar.getMentorId();
			L2PcInstance partner = L2World.getInstance().getPlayer(activeChar.getMentorId());
			if (partner != null)
			{
				partnerInfo.name = partner.getName();
				partnerInfo.classId = partner.getClassId();
				partnerInfo.level = partner.getLevel();
				partnerInfo.online = true;
			}
			else
			{
				partnerInfo.name = CharNameTable.getInstance().getNameById(activeChar.getMentorId());
				getClassIdAndLevel(partnerInfo);
				partnerInfo.online = false;
			}
			_partners.add(partnerInfo);
		}

		_player = activeChar;
	}

	private void getClassIdAndLevel(PartnerInfo partnerInfo)
	{
		Connection con = null;
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("SELECT level, classid, base_class FROM characters WHERE charId=?");
			statement.setInt(1, partnerInfo.objId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				partnerInfo.level = rset.getByte("level");
				partnerInfo.classId = rset.getInt("classid");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.info("Failed loading character.");
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.isMentor() ? 0x01 :
				_player.isMentee() ? 0x02 : 0x00); // 0x00 Nothing, 0x01 my mentees, 0x02 my mentor
		writeD(0x00); // ???
		writeD(_partners.size());
		for (PartnerInfo menteeInfo : _partners)
		{
			writeD(menteeInfo.objId);
			writeS(menteeInfo.name);
			writeD(menteeInfo.classId);
			writeD(menteeInfo.level);
			writeD(menteeInfo.online ? 0x01 : 0x00);
		}
	}
}
