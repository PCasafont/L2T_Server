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

package l2server.gameserver.model;

import l2server.L2DatabaseFactory;
import l2server.gameserver.datatables.UITable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.ActionKey;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author mrTJO
 */
public class L2UIKeysSettings
{

	private final L2PcInstance player;

	Map<Integer, List<ActionKey>> storedKeys;
	Map<Integer, List<Integer>> storedCategories;

	boolean saved = true;

	public L2UIKeysSettings(L2PcInstance player)
	{
		this.player = player;
		loadFromDB();
	}

	public void storeAll(Map<Integer, List<Integer>> catMap, Map<Integer, List<ActionKey>> keyMap)
	{
		saved = false;
		storedCategories = catMap;
		storedKeys = keyMap;
	}

	public void storeCategories(Map<Integer, List<Integer>> catMap)
	{
		saved = false;
		storedCategories = catMap;
	}

	public Map<Integer, List<Integer>> getCategories()
	{
		return storedCategories;
	}

	public void storeKeys(Map<Integer, List<ActionKey>> keyMap)
	{
		saved = false;
		storedKeys = keyMap;
	}

	public Map<Integer, List<ActionKey>> getKeys()
	{
		return storedKeys;
	}

	public void loadFromDB()
	{
		getCatsFromDB();
		getKeysFromDB();
	}

	/**
	 * Save Categories and Mapped Keys into GameServer DataBase
	 */
	public void saveInDB()
	{
		String query;
		int playerId = player.getObjectId();

		if (saved)
		{
			return;
		}

		query = "REPLACE INTO character_ui_categories (`charId`, `catId`, `order`, `cmdId`) VALUES ";
		for (int category : storedCategories.keySet())
		{
			int order = 0;
			for (int key : storedCategories.get(category))
			{
				query += "(" + playerId + ", " + category + ", " + order++ + ", " + key + "),";
			}
		}
		query = query.substring(0, query.length() - 1) + "; ";

		Connection con = null;
		PreparedStatement statement;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(query);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: saveInDB(): " + e.getMessage(), e);
		}

		query =
				"REPLACE INTO character_ui_actions (`charId`, `cat`, `order`, `cmd`, `key`, `tgKey1`, `tgKey2`, `show`) VALUES";
		for (List<ActionKey> keyLst : storedKeys.values())
		{
			int order = 0;
			for (ActionKey key : keyLst)
			{
				query += key.getSqlSaveString(playerId, order++) + ",";
			}
		}
		query = query.substring(0, query.length() - 1) + ";";

		try
		{
			if (con == null)
			{
				con = L2DatabaseFactory.getInstance().getConnection();
			}

			statement = con.prepareStatement(query);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: saveInDB(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		saved = true;
	}

	public void getCatsFromDB()
	{

		if (storedCategories != null)
		{
			return;
		}

		storedCategories = new HashMap<>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM character_ui_categories WHERE `charId` = ? ORDER BY `catId`, `order`");
			stmt.setInt(1, player.getObjectId());
			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				int cat = rs.getInt("catId");
				int cmd = rs.getInt("cmdId");
				insertCategory(cat, cmd);
			}
			stmt.close();
			rs.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: getCatsFromDB(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (storedCategories.size() < 1)
		{
			storedCategories = UITable.getInstance().getCategories();
		}
	}

	public void getKeysFromDB()
	{
		if (storedKeys != null)
		{
			return;
		}

		storedKeys = new HashMap<>();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement stmt = con.prepareStatement(
					"SELECT * FROM character_ui_actions WHERE `charId` = ? ORDER BY `cat`, `order`");
			stmt.setInt(1, player.getObjectId());
			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				int cat = rs.getInt("cat");
				int cmd = rs.getInt("cmd");
				int key = rs.getInt("key");
				int tgKey1 = rs.getInt("tgKey1");
				int tgKey2 = rs.getInt("tgKey2");
				int show = rs.getInt("show");
				insertKey(cat, cmd, key, tgKey1, tgKey2, show);
			}
			stmt.close();
			rs.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Exception: getKeysFromDB(): " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (storedKeys.size() < 1)
		{
			storedKeys = UITable.getInstance().getKeys();
		}
	}

	public void insertCategory(int cat, int cmd)
	{
		if (storedCategories.containsKey(cat))
		{
			storedCategories.get(cat).add(cmd);
		}
		else
		{
			List<Integer> tmp = new ArrayList<>();
			tmp.add(cmd);
			storedCategories.put(cat, tmp);
		}
	}

	public void insertKey(int cat, int cmdId, int key, int tgKey1, int tgKey2, int show)
	{
		ActionKey tmk = new ActionKey(cat, cmdId, key, tgKey1, tgKey2, show);
		if (storedKeys.containsKey(cat))
		{
			storedKeys.get(cat).add(tmk);
		}
		else
		{
			List<ActionKey> tmp = new ArrayList<>();
			tmp.add(tmk);
			storedKeys.put(cat, tmp);
		}
	}

	public boolean isSaved()
	{
		return saved;
	}
}
