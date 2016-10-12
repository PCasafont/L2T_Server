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

package l2server.gameserver.datatables;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.clientpackets.CharacterCreate;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.2.2.1 $ $Date: 2005/03/27 15:29:18 $
 */
public class CharNameTable
{

	private final Map<Integer, String> _chars;
	private final Map<Integer, Integer> _accessLevels;

	private CharNameTable()
	{
		_chars = new ConcurrentHashMap<>();
		_accessLevels = new HashMap<>();
		if (Config.CACHE_CHAR_NAMES)
		{
			loadAll();
		}
	}

	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public final void addName(L2PcInstance player)
	{
		if (player != null)
		{
			addName(player.getObjectId(), player.getName());
			_accessLevels.put(player.getObjectId(), player.getAccessLevel().getLevel());
		}
	}

	private void addName(int objId, String name)
	{
		if (name != null)
		{
			if (!name.equalsIgnoreCase(_chars.get(objId)))
			{
				_chars.put(objId, name);
			}
		}
	}

	public final void removeName(int objId)
	{
		_chars.remove(objId);
		_accessLevels.remove(objId);
	}

	public final int getIdByName(String name)
	{
		if (name == null || name.isEmpty())
		{
			return -1;
		}

		Iterator<Entry<Integer, String>> it = _chars.entrySet().iterator();

		Map.Entry<Integer, String> pair;
		while (it.hasNext())
		{
			pair = it.next();
			if (pair.getValue().equalsIgnoreCase(name))
			{
				return pair.getKey();
			}
		}

		if (Config.CACHE_CHAR_NAMES)
		{
			return -1;
		}

		int id = -1;
		int accessLevel = 0;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT charId,accesslevel FROM characters WHERE char_name=?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				id = rset.getInt(1);
				accessLevel = rset.getInt(2);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not check existing char name: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		if (id > 0)
		{
			_chars.put(id, name);
			_accessLevels.put(id, accessLevel);
			return id;
		}

		return -1; // not found
	}

	public final String getNameById(int id)
	{
		if (id <= 0)
		{
			return null;
		}

		String name = _chars.get(id);
		if (name != null)
		{
			return name;
		}

		if (Config.CACHE_CHAR_NAMES)
		{
			return null;
		}

		int accessLevel = 0;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name,accesslevel FROM characters WHERE charId=?");
			statement.setInt(1, id);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				name = rset.getString(1);
				accessLevel = rset.getInt(2);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not check existing char id: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		if (name != null && !name.isEmpty())
		{
			_chars.put(id, name);
			_accessLevels.put(id, accessLevel);
			return name;
		}

		return null; //not found
	}

	public final int getAccessLevelById(int objectId)
	{
		if (getNameById(objectId) != null)
		{
			return _accessLevels.get(objectId);
		}
		else
		{
			return 0;
		}
	}

	public synchronized boolean doesCharNameExist(String name)
	{
		boolean result = true;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT account_name FROM characters WHERE char_name=?");
			statement.setString(1, name);
			ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not check existing charname: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return result;
	}

	public int accountCharNumber(String account)
	{
		Connection con = null;

		int number = 0;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT COUNT(char_name) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				number = rset.getInt(1);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not check existing char number: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return number;
	}

	private void loadAll()
	{
		String name;
		int id = -1;
		int accessLevel = 0;
		PreparedStatement statement = null;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT charId,char_name,accesslevel FROM characters");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				id = rset.getInt(1);
				name = rset.getString(2);
				accessLevel = rset.getInt(3);
				_chars.put(id, name);
				_accessLevels.put(id, accessLevel);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not load char name: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		Log.info(getClass().getSimpleName() + ": Loaded " + _chars.size() + " char names.");
	}

	public boolean setCharNameConditions(L2PcInstance player, String name)
	{
		if (Config.FORBIDDEN_NAMES.length > 1)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (name.toLowerCase().contains(st.toLowerCase()))
				{
					player.sendPacket(
							SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_CHARACTER_NAME_TRY_AGAIN));
					return false;
				}
			}
		}
		if (!Util.isAlphaNumeric(name) || !CharacterCreate.isValidName(name) || name.length() < 1 ||
				name.length() > 16 || getIdByName(name) > 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CHARACTER_NAME_INVALID_RENAME_CHARACTER));
			return false;
		}

		return true;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}
}
