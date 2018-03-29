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
	private boolean action, online;
	private int objid;
	private String name;
	private L2PcInstance player;
	private int level = 0;
	private int classId = 0;
	private String memo;

	/**
	 * @param action - true for adding, false for remove
	 */
	public FriendPacket(boolean action, int objId, L2PcInstance activeChar)
	{
		this.action = action;
		this.objid = objId;
		name = CharNameTable.getInstance().getNameById(objId);
		online = L2World.getInstance().getPlayer(objId) != null;
		player = L2World.getInstance().getPlayer(objId);
		memo = activeChar.getFriendMemo(objId);
		if (player != null)
		{
			level = player.getLevel();
			classId = player.getClassId();
		}
		else
		{
			offlineFriendInfo(objId);
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(action ? 1 : 3); // 1-add 3-remove
		writeD(objid);
		writeS(name);
		writeD(online ? 1 : 0);
		writeD(online ? objid : 0);
		writeD(level);
		writeD(classId);
		writeS(memo);
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
				classId = rset.getInt("classid");
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
			this.level = level;
		}
	}
}
