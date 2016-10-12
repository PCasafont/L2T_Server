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

package l2server.gameserver.util;

import l2server.L2DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class GMAudit
{
	public static void auditGMAction(String gmName, String action, String target, String params)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			PreparedStatement statement =
					con.prepareStatement("INSERT INTO gm_audit(time, gm, action, target, params) VALUES (?,?,?,?,?);");

			statement.setInt(1, (int) (System.currentTimeMillis() / 1000));
			statement.setString(2, gmName);
			statement.setString(3, action);
			statement.setString(4, target);
			statement.setString(5, params);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public static void auditGMAction(String gmName, String action, String target)
	{
		auditGMAction(gmName, action, target, "");
	}
}
