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
import java.util.Calendar;

/**
 * @author Erlando
 */
public class ExFriendDetailInfo extends L2GameServerPacket
{

	private L2PcInstance _player;
	int _friendObjId;
	private String _name;
	private int _isOnline;
	private int _level;
	private int _classId;
	private int _clanId;
	private int _clanCrestId;
	private String _clanName;
	private int _allyId;
	private int _allyCrestId;
	private String _allyName;
	private int _createdMonth;
	private int _createdDay;
	private long _lastLogin;
	private String _memo;

	public ExFriendDetailInfo(L2PcInstance activeChar, String charName)
	{
		_player = activeChar;
		_friendObjId = CharNameTable.getInstance().getIdByName(charName);
		_name = charName;
		_isOnline = L2World.getInstance().getPlayer(_friendObjId) != null &&
				L2World.getInstance().getPlayer(_friendObjId).isOnline() ? 1 : 0;
		_memo = activeChar.getFriendMemo(_friendObjId);
		if (_isOnline == 1)
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(_friendObjId);
			_level = friend.getLevel();
			_classId = friend.getClassId();
			_clanId = friend.getClanId();
			_clanCrestId = friend.getClanCrestId();
			_clanName = friend.getClan() != null ? friend.getClan().getName() : "";
			_allyId = friend.getAllyId();
			_allyCrestId = friend.getAllyCrestId();
			_allyName = friend.getClan() != null ? friend.getClan().getAllyName() : "";
			Calendar createDate = Calendar.getInstance();
			createDate.setTimeInMillis(friend.getCreateTime());
			_createdMonth = createDate.get(Calendar.MONTH) + 1;
			_createdDay = createDate.get(Calendar.DAY_OF_MONTH);
		}
		else
		{
			offlineFriendInfo(_friendObjId);
		}
	}

	private void offlineFriendInfo(int objId)
	{
		long createDate = 0;
		int level = 0;
		int bClassId = 0;
		Connection con = null;

		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE charId=?");
			statement.setInt(1, objId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				level = rset.getByte("level");
				_classId = rset.getInt("classid");
				bClassId = rset.getInt("base_class");
				_clanId = rset.getInt("clanid");
				_lastLogin = rset.getLong("lastAccess");
				createDate = rset.getLong("createTime");
			}
			statement.execute();

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.warning("Failed loading character. " + e);
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

				statement.execute();
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				Log.warning("Failed loading character_subclasses. " + e);
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
		if (_clanId != 0)
		{
			try
			{
				// Retrieve the L2PcInstance from the characters table of the database
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_data WHERE clan_id=?");
				statement.setInt(1, _clanId);
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					_clanName = rset.getString("clan_name");
					_clanCrestId = rset.getInt("crest_id");
					_allyId = rset.getInt("ally_id");
					_allyName = rset.getString("ally_name");
					_allyCrestId = rset.getInt("ally_crest_id");
				}
				statement.execute();
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				Log.warning("Failed loading clan_data. " + e);
				e.printStackTrace();
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(createDate);
		_createdMonth = c.get(Calendar.MONTH) + 1;
		_createdDay = c.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_player.getObjectId()); // Character ID
		writeS(_name); // Name
		writeD(_isOnline); // Online
		writeD(_isOnline == 1 ? _friendObjId : 0x00); // Friend OID
		writeH(_level); // Level
		writeH(_classId); // Class
		writeD(_clanId); // Pledge ID
		writeD(_clanCrestId); // Pledge crest ID
		writeS(_clanName); // Pledge name
		writeD(_allyId); // Alliance ID
		writeD(_allyCrestId); // Alliance crest ID
		writeS(_allyName); // Alliance name
		writeC(_createdMonth); // Creation month
		writeC(_createdDay); // Creation day
		writeD(_isOnline == 1 ? -1 : (int) (System.currentTimeMillis() - _lastLogin) / 1000);
		writeS(_memo); // Memo
	}
}
