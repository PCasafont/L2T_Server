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

package l2server.gameserver.instancemanager;

import l2server.L2DatabaseFactory;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class GlobalVariablesManager
{

	private static final String LOAD_VAR = "SELECT var,value FROM global_variables";
	private static final String SAVE_VAR =
			"INSERT INTO global_variables (var,value) VALUES (?,?) ON DUPLICATE KEY UPDATE value=?";

	private final Map<String, String> _variablesMap;

	private GlobalVariablesManager()
	{
		_variablesMap = new HashMap<>();

		loadVars();
	}

	private void loadVars()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset;
		String var, value;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(LOAD_VAR);

			rset = statement.executeQuery();
			while (rset.next())
			{
				var = rset.getString(1);
				value = rset.getString(2);

				_variablesMap.put(var, value);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.warning("GlobalVariablesManager: problem while loading variables: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final void saveVars()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SAVE_VAR);

			for (String var : _variablesMap.keySet())
			{
				statement.setString(1, var);
				statement.setString(2, _variablesMap.get(var));
				statement.setString(3, _variablesMap.get(var));
				statement.execute();
			}
			statement.close();
			//Log.info("GlobalVariablesManager: Database updated.");
		}
		catch (Exception e)
		{
			Log.warning("GlobalVariablesManager: problem while saving variables: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void storeVariable(String var, String value)
	{
		_variablesMap.put(var, value);
	}

	public boolean isVariableStored(String var)
	{
		return _variablesMap.containsKey(var);
	}

	public String getStoredVariable(String var)
	{
		return _variablesMap.get(var);
	}

	public static GlobalVariablesManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final GlobalVariablesManager _instance = new GlobalVariablesManager();
	}
}
