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

package l2server.gameserver.taskmanager;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.taskmanager.tasks.*;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;

import static l2server.gameserver.taskmanager.TaskTypes.*;

/**
 * @author Layane
 */
public final class TaskManager
{

	protected static final String[] SQL_STATEMENTS = {
			"SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks",
			"UPDATE global_tasks SET last_activation=? WHERE id=?",
			"SELECT id FROM global_tasks WHERE task=?",
			"INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)"
	};

	private final HashMap<Integer, Task> _tasks = new HashMap<>();
	protected final ArrayList<ExecutedTask> _currentTasks = new ArrayList<>();

	public class ExecutedTask implements Runnable
	{
		int id;
		long lastActivation;
		Task task;
		TaskTypes type;
		String[] params;
		ScheduledFuture<?> scheduled;

		public ExecutedTask(Task ptask, TaskTypes ptype, ResultSet rset) throws SQLException
		{
			task = ptask;
			type = ptype;
			id = rset.getInt("id");
			lastActivation = rset.getLong("last_activation");
			params = new String[]{rset.getString("param1"), rset.getString("param2"), rset.getString("param3")};
		}

		@Override
		public void run()
		{
			task.onTimeElapsed(this);
			lastActivation = System.currentTimeMillis();

			Connection con = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[1]);
				statement.setLong(1, lastActivation);
				statement.setInt(2, id);
				statement.executeUpdate();
				statement.close();
			}
			catch (SQLException e)
			{
				Log.log(Level.WARNING, "Cannot updated the Global Task " + id + ": " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

			if (type == TYPE_SHEDULED || type == TYPE_TIME)
			{
				stopTask();
			}
		}

		@Override
		public boolean equals(Object object)
		{
			return id == ((ExecutedTask) object).id;
		}

		public Task getTask()
		{
			return task;
		}

		public TaskTypes getType()
		{
			return type;
		}

		public int getId()
		{
			return id;
		}

		public String[] getParams()
		{
			return params;
		}

		public long getLastActivation()
		{
			return lastActivation;
		}

		public void stopTask()
		{
			task.onDestroy();

			if (scheduled != null)
			{
				scheduled.cancel(true);
			}

			_currentTasks.remove(this);
		}
	}

	public static TaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private TaskManager()
	{
		initialize();
		startAllTasks();
	}

	private void initialize()
	{
		registerTask(new TaskCleanUp());
		registerTask(new TaskScript());
		registerTask(new TaskJython());
		registerTask(new TaskGlobalVariablesSave());
		registerTask(new TaskOlympiadSave());
		registerTask(new TaskRaidPointsReset());
		registerTask(new TaskRecom());
		registerTask(new TaskDailyQuestClean());
		registerTask(new TaskVitalityReset());

		if (Config.isServer(Config.TENKAI))
		{
			registerTask(new TaskDailyChangeRates());
			registerTask(new TaskCustomTasks());
		}
	}

	public void registerTask(Task task)
	{
		int key = task.getName().hashCode();
		if (!_tasks.containsKey(key))
		{
			_tasks.put(key, task);
			task.initialize();
		}
	}

	private void startAllTasks()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[0]);
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				Task task = _tasks.get(rset.getString("task").trim().toLowerCase().hashCode());

				if (task == null)
				{
					continue;
				}

				TaskTypes type = TaskTypes.valueOf(rset.getString("type"));

				if (type != TYPE_NONE)
				{
					ExecutedTask current = new ExecutedTask(task, type, rset);
					if (launchTask(current))
					{
						_currentTasks.add(current);
					}
				}
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error while loading Global Task table: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private boolean launchTask(ExecutedTask task)
	{
		final ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
		final TaskTypes type = task.getType();
		long delay, interval;

		switch (type)
		{
			case TYPE_STARTUP:
				task.run();
				return false;
			case TYPE_SHEDULED:
				delay = Long.valueOf(task.getParams()[0]);
				task.scheduled = scheduler.scheduleGeneral(task, delay);
				return true;
			case TYPE_FIXED_SHEDULED:
				delay = Long.valueOf(task.getParams()[0]);
				interval = Long.valueOf(task.getParams()[1]);
				task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
				return true;
			case TYPE_TIME:
				try
				{
					Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
					long diff = desired.getTime() - System.currentTimeMillis();
					if (diff >= 0)
					{
						task.scheduled = scheduler.scheduleGeneral(task, diff);
						return true;
					}
					Log.info("Task " + task.getId() + " is obsoleted.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case TYPE_SPECIAL:
				ScheduledFuture<?> result = task.getTask().launchSpecial(task);
				if (result != null)
				{
					task.scheduled = result;
					return true;
				}
				break;
			case TYPE_GLOBAL_TASK:
				interval = Long.valueOf(task.getParams()[0]) * 86400000L;
				String[] hour = task.getParams()[1].split(":");

				if (hour.length != 3)
				{
					Log.warning("Task " + task.getId() + " has incorrect parameters");
					return false;
				}

				Calendar check = Calendar.getInstance();
				check.setTimeInMillis(task.getLastActivation() + interval);

				Calendar min = Calendar.getInstance();
				try
				{
					if (Integer.valueOf(task.getParams()[0]) == 7)
					{
						min.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					}
					min.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour[0]));
					min.set(Calendar.MINUTE, Integer.parseInt(hour[1]));
					min.set(Calendar.SECOND, Integer.parseInt(hour[2]));
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Bad parameter on task " + task.getId() + ": " + e.getMessage(), e);
					return false;
				}

				delay = min.getTimeInMillis() - System.currentTimeMillis();

				if (check.after(min) || delay < 0)
				{
					delay += interval;
				}

				task.scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);

				return true;

			default:
				return false;
		}

		return false;
	}

	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addUniqueTask(task, type, param1, param2, param3, 0);
	}

	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[2]);
			statement.setString(1, task);
			ResultSet rset = statement.executeQuery();

			if (!rset.next())
			{
				statement = con.prepareStatement(SQL_STATEMENTS[3]);
				statement.setString(1, task);
				statement.setString(2, type.toString());
				statement.setLong(3, lastActivation);
				statement.setString(4, param1);
				statement.setString(5, param2);
				statement.setString(6, param3);
				statement.execute();
			}

			rset.close();
			statement.close();

			return true;
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Cannot add the unique task: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addTask(task, type, param1, param2, param3, 0);
	}

	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[3]);
			statement.setString(1, task);
			statement.setString(2, type.toString());
			statement.setLong(3, lastActivation);
			statement.setString(4, param1);
			statement.setString(5, param2);
			statement.setString(6, param3);
			statement.execute();

			statement.close();
			return true;
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Cannot add the task:  " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final TaskManager _instance = new TaskManager();
	}
}
