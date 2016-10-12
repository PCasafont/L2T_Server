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
 * Support for "Chat with Friends" dialog.
 * <p>
 * This packet is sent only at login.
 * <p>
 * Format: cd (dSdd)
 * d: Total Friend Count
 * <p>
 * d: Player Object ID
 * S: Friend Name
 * d: Online/Offline
 * d: Unknown (0 if offline)
 *
 * @author Tempy
 */
public class FriendList extends L2GameServerPacket
{
	//
	private List<FriendInfo> _info;
	private int _level = 0;
	private int _classId = 0;
	private String memo;

	private static class FriendInfo
	{
		int objId;
		String name;
		boolean online;
		int level;
		int classId;

		public FriendInfo(int objId, String name, boolean online, int level, int classId)
		{
			this.objId = objId;
			this.name = name;
			this.online = online;
			this.level = level;
			this.classId = classId;
		}
	}

	public FriendList(L2PcInstance player)
	{
		_info = new ArrayList<>(player.getFriendList().size());
		for (int objId : player.getFriendList())
		{
			memo = player.getFriendMemo(objId);
			String name = CharNameTable.getInstance().getNameById(objId);
			L2PcInstance player1 = L2World.getInstance().getPlayer(objId);
			boolean online = false;
			if (player1 != null && player1.isOnline())
			{
				online = true;
			}
			if (online)
			{
				_level = player1.getLevel();
				_classId = player1.getClassId();
			}
			else
			{
				offlineFriendInfo(objId);
			}
			_info.add(new FriendInfo(objId, name, online, _level, _classId));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_info.size());
		for (FriendInfo info : _info)
		{
			writeD(info.objId); // character id
			writeS(info.name);
			writeD(info.online ? 0x01 : 0x00); // online
			writeD(info.online ? info.objId : 0x00); // object id if online
			writeD(info.level);
			writeD(info.classId);
			writeS(memo);
		}
	}

	private void offlineFriendInfo(int objId)
	{
		int level = 0;
		int bClassId = 0;
		Connection con = null;

		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("SELECT level, classid, base_class FROM characters WHERE charId=?");
			statement.setInt(1, objId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				level = rset.getByte("level");
				_classId = rset.getInt("classid");
				bClassId = rset.getInt("base_class");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.warning("Failed loading character.");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		if (_classId != bClassId)
		{
			try
			{
				// Retrieve the L2PcInstance from the characters table of the database
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement =
						con.prepareStatement("SELECT level FROM character_subclasses WHERE charId=? AND class_id=?");
				statement.setInt(1, objId);
				statement.setInt(2, _classId);
				ResultSet rset = statement.executeQuery();

				while (rset.next())
				{
					_level = rset.getByte("level");
				}

				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				Log.warning("Failed loading character_subclasses.");
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		else
		{
			_level = level;
		}
	}
}
