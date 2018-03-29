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

	private L2PcInstance player;
	int friendObjId;
	private String name;
	private int isOnline;
	private int level;
	private int classId;
	private int clanId;
	private int clanCrestId;
	private String clanName;
	private int allyId;
	private int allyCrestId;
	private String allyName;
	private int createdMonth;
	private int createdDay;
	private long lastLogin;
	private String memo;

	public ExFriendDetailInfo(L2PcInstance activeChar, String charName)
	{
		player = activeChar;
		friendObjId = CharNameTable.getInstance().getIdByName(charName);
		name = charName;
		isOnline = L2World.getInstance().getPlayer(friendObjId) != null &&
				L2World.getInstance().getPlayer(friendObjId).isOnline() ? 1 : 0;
		memo = activeChar.getFriendMemo(friendObjId);
		if (isOnline == 1)
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(friendObjId);
			level = friend.getLevel();
			classId = friend.getClassId();
			clanId = friend.getClanId();
			clanCrestId = friend.getClanCrestId();
			clanName = friend.getClan() != null ? friend.getClan().getName() : "";
			allyId = friend.getAllyId();
			allyCrestId = friend.getAllyCrestId();
			allyName = friend.getClan() != null ? friend.getClan().getAllyName() : "";
			Calendar createDate = Calendar.getInstance();
			createDate.setTimeInMillis(friend.getCreateTime());
			createdMonth = createDate.get(Calendar.MONTH) + 1;
			createdDay = createDate.get(Calendar.DAY_OF_MONTH);
		}
		else
		{
			offlineFriendInfo(friendObjId);
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
				classId = rset.getInt("classid");
				bClassId = rset.getInt("base_class");
				clanId = rset.getInt("clanid");
				lastLogin = rset.getLong("lastAccess");
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
		if (classId != bClassId)
		{
			try
			{
				// Retrieve the L2PcInstance from the characters table of the database
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement =
						con.prepareStatement("SELECT level FROM character_subclasses WHERE charId=? AND class_id=?");
				statement.setInt(1, objId);
				statement.setInt(2, classId);
				ResultSet rset = statement.executeQuery();

				while (rset.next())
				{
					this.level = rset.getByte("level");
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
			this.level = level;
		}
		if (clanId != 0)
		{
			try
			{
				// Retrieve the L2PcInstance from the characters table of the database
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_data WHERE clan_id=?");
				statement.setInt(1, clanId);
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					clanName = rset.getString("clan_name");
					clanCrestId = rset.getInt("crest_id");
					allyId = rset.getInt("ally_id");
					allyName = rset.getString("ally_name");
					allyCrestId = rset.getInt("ally_crest_id");
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
		createdMonth = c.get(Calendar.MONTH) + 1;
		createdDay = c.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(player.getObjectId()); // Character ID
		writeS(name); // Name
		writeD(isOnline); // Online
		writeD(isOnline == 1 ? friendObjId : 0x00); // Friend OID
		writeH(level); // Level
		writeH(classId); // Class
		writeD(clanId); // Pledge ID
		writeD(clanCrestId); // Pledge crest ID
		writeS(clanName); // Pledge name
		writeD(allyId); // Alliance ID
		writeD(allyCrestId); // Alliance crest ID
		writeS(allyName); // Alliance name
		writeC(createdMonth); // Creation month
		writeC(createdDay); // Creation day
		writeD(isOnline == 1 ? -1 : (int) (System.currentTimeMillis() - lastLogin) / 1000);
		writeS(memo); // Memo
	}
}
