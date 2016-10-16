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

package l2server.gameserver.idfactory;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/04/11 10:06:12 $
 */
public class StackIDFactory extends IdFactory
{

	private int curOID;
	private int tempOID;

	private Stack<Integer> freeOIDStack = new Stack<>();

	protected StackIDFactory()
	{
		super();
		this.curOID = FIRST_OID;
		this.tempOID = FIRST_OID;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			//con.createStatement().execute("drop table if exists tmp_obj_id");

			int[] tmp_obj_ids = extractUsedObjectIDTable();
			if (tmp_obj_ids.length > 0)
			{
				this.curOID = tmp_obj_ids[tmp_obj_ids.length - 1];
			}
			Log.info("Max Id = " + this.curOID);

			int N = tmp_obj_ids.length;
			for (int idx = 0; idx < N; idx++)
			{
				N = insertUntil(tmp_obj_ids, idx, N, con);
			}

			this.curOID++;
			Log.info("IdFactory: Next usable Object ID is: " + this.curOID);
			this.initialized = true;
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "ID Factory could not be initialized correctly:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private int insertUntil(int[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException
	{
		int id = tmp_obj_ids[idx];
		if (id == this.tempOID)
		{
			this.tempOID++;
			return N;
		}
		// check these IDs not present in DB
		if (Config.BAD_ID_CHECKING)
		{
			for (String check : ID_CHECKS)
			{
				PreparedStatement ps = con.prepareStatement(check);
				ps.setInt(1, this.tempOID);
				//ps.setInt(1, this.curOID);
				ps.setInt(2, id);
				ResultSet rs = ps.executeQuery();
				if (rs.next())
				{
					int badId = rs.getInt(1);
					Log.severe("Bad ID " + badId + " in DB found by: " + check);
					throw new RuntimeException();
				}
				rs.close();
				ps.close();
			}
		}

		//int hole = id - this.curOID;
		int hole = id - this.tempOID;
		if (hole > N - idx)
		{
			hole = N - idx;
		}
		for (int i = 1; i <= hole; i++)
		{
			//log.info("Free ID added " + (this.tempOID));
			this.freeOIDStack.push(this.tempOID);
			this.tempOID++;
			//_curOID++;
		}
		if (hole < N - idx)
		{
			this.tempOID++;
		}
		return N - hole;
	}

	public static IdFactory getInstance()
	{
		return instance;
	}

	@Override
	public synchronized int getNextId()
	{
		int id;
		if (!this.freeOIDStack.empty())
		{
			id = this.freeOIDStack.pop();
		}
		else
		{
			id = this.curOID;
			this.curOID = this.curOID + 1;
		}
		return id;
	}

	/**
	 * return a used Object ID back to the pool
	 */
	@Override
	public synchronized void releaseId(int id)
	{
		this.freeOIDStack.push(id);
	}

	@Override
	public int size()
	{
		return FREE_OBJECT_ID_SIZE - this.curOID + FIRST_OID + this.freeOIDStack.size();
	}
}
