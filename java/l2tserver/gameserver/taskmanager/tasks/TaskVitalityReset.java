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
package l2tserver.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

import l2tserver.L2DatabaseFactory;
import l2tserver.gameserver.taskmanager.Task;
import l2tserver.gameserver.taskmanager.TaskManager;
import l2tserver.gameserver.taskmanager.TaskTypes;
import l2tserver.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2tserver.log.Log;

/**
 * @author Xavi
 * 
 */
public class TaskVitalityReset extends Task
{
	
	private static final String NAME = "vitalty reset";
	
	/**
	 * 
	 * @see l2tserver.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.taskmanager.Task#onTimeElapsed(l2tserver.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM account_gsdata WHERE var=?");
			statement.setString(1, "vitality");
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Could not reset Vitalty system: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		Log.config("Vitalty system reset.");
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.taskmanager.Task#initialize()
	 */
	@Override
	public void initialize()
	{
		super.initialize();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "7", "06:30:00", "");
	}
	
}
