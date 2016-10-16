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
		this.player = activeChar;
		this.friendObjId = CharNameTable.getInstance().getIdByName(charName);
		this.name = charName;
		this.isOnline = L2World.getInstance().getPlayer(this.friendObjId) != null &&
				L2World.getInstance().getPlayer(this.friendObjId).isOnline() ? 1 : 0;
		this.memo = activeChar.getFriendMemo(this.friendObjId);
		if (this.isOnline == 1)
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(this.friendObjId);
			this.level = friend.getLevel();
			this.classId = friend.getClassId();
			this.clanId = friend.getClanId();
			this.clanCrestId = friend.getClanCrestId();
			this.clanName = friend.getClan() != null ? friend.getClan().getName() : "";
			this.allyId = friend.getAllyId();
			this.allyCrestId = friend.getAllyCrestId();
			this.allyName = friend.getClan() != null ? friend.getClan().getAllyName() : "";
			Calendar createDate = Calendar.getInstance();
			createDate.setTimeInMillis(friend.getCreateTime());
			this.createdMonth = createDate.get(Calendar.MONTH) + 1;
			this.createdDay = createDate.get(Calendar.DAY_OF_MONTH);
		}
		else
		{
			offlineFriendInfo(this.friendObjId);
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
				this.classId = rset.getInt("classid");
				bClassId = rset.getInt("base_class");
				this.clanId = rset.getInt("clanid");
				this.lastLogin = rset.getLong("lastAccess");
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
		if (this.classId != bClassId)
		{
			try
			{
				// Retrieve the L2PcInstance from the characters table of the database
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement =
						con.prepareStatement("SELECT level FROM character_subclasses WHERE charId=? AND class_id=?");
				statement.setInt(1, objId);
				statement.setInt(2, this.classId);
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
		if (this.clanId != 0)
		{
			try
			{
				// Retrieve the L2PcInstance from the characters table of the database
				con = L2DatabaseFactory.getInstance().getConnection();

				PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_data WHERE clan_id=?");
				statement.setInt(1, this.clanId);
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					this.clanName = rset.getString("clan_name");
					this.clanCrestId = rset.getInt("crest_id");
					this.allyId = rset.getInt("ally_id");
					this.allyName = rset.getString("ally_name");
					this.allyCrestId = rset.getInt("ally_crest_id");
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
		this.createdMonth = c.get(Calendar.MONTH) + 1;
		this.createdDay = c.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(this.player.getObjectId()); // Character ID
		writeS(this.name); // Name
		writeD(this.isOnline); // Online
		writeD(this.isOnline == 1 ? this.friendObjId : 0x00); // Friend OID
		writeH(this.level); // Level
		writeH(this.classId); // Class
		writeD(this.clanId); // Pledge ID
		writeD(this.clanCrestId); // Pledge crest ID
		writeS(this.clanName); // Pledge name
		writeD(this.allyId); // Alliance ID
		writeD(this.allyCrestId); // Alliance crest ID
		writeS(this.allyName); // Alliance name
		writeC(this.createdMonth); // Creation month
		writeC(this.createdDay); // Creation day
		writeD(this.isOnline == 1 ? -1 : (int) (System.currentTimeMillis() - this.lastLogin) / 1000);
		writeS(this.memo); // Memo
	}
}
