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
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PetNameTable
{

	private PetNameTable()
	{
	}

	public static PetNameTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = true;
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)");
			statement.setString(1, name);

			String cond = "";
			for (int it : PetDataTable.getPetItemsByNpc(petNpcId))
			{
				if (!cond.isEmpty())
				{
					cond += ", ";
				}
				cond += it;
			}
			statement.setString(2, cond);
			ResultSet rset = statement.executeQuery();
			result = rset.next();
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Could not check existing petname:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return result;
	}

	public boolean isValidPetName(String name)
	{
		boolean result = true;

		if (!isAlphaNumeric(name))
		{
			return result;
		}

		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.PET_NAME_TEMPLATE);
		}
		catch (PatternSyntaxException e) // case of illegal pattern
		{
			Log.warning("ERROR : Pet name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(name);
		if (!regexp.matches())
		{
			result = false;
		}
		return result;
	}

	private boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		char[] chars = text.toCharArray();
		for (char aChar : chars)
		{
			if (!Character.isLetterOrDigit(aChar))
			{
				result = false;
				break;
			}
		}
		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PetNameTable _instance = new PetNameTable();
	}
}
