/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
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

/**
 * Support for "Chat with Friends" dialog.
 * <p>
 * Add new friend or delete.
 * <BR>
 * Format: cddSdd <BR>
 * d: action <BR>
 * d: Player Object ID <BR>
 * S: Friend Name <BR>
 * d: Online/Offline <BR>
 * d: Unknown (0 if offline)<BR>
 *
 * @author JIV
 */
public class FriendPacket extends L2GameServerPacket
{
	//
	private boolean _action, _online;
	private int _objid;
	private String _name;
	private L2PcInstance _player;
	private int _level = 0;
	private int _classId = 0;
	private String _memo;

	/**
	 * @param action - true for adding, false for remove
	 */
	public FriendPacket(boolean action, int objId, L2PcInstance activeChar)
	{
		_action = action;
		_objid = objId;
		_name = CharNameTable.getInstance().getNameById(objId);
		_online = L2World.getInstance().getPlayer(objId) != null;
		_player = L2World.getInstance().getPlayer(objId);
		_memo = activeChar.getFriendMemo(objId);
		if (_player != null)
		{
			_level = _player.getLevel();
			_classId = _player.getClassId();
		}
		else
		{
			offlineFriendInfo(objId);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_action ? 1 : 3); // 1-add 3-remove
		writeD(_objid);
		writeS(_name);
		writeD(_online ? 1 : 0);
		writeD(_online ? _objid : 0);
		writeD(_level);
		writeD(_classId);
		writeS(_memo);
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
