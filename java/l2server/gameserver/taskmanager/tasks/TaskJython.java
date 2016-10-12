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

package l2server.gameserver.taskmanager.tasks;

import l2server.Config;
import l2server.gameserver.taskmanager.Task;
import l2server.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.python.util.PythonInterpreter;

/**
 * @author Layane
 */
public class TaskJython extends Task
{
	public static final String NAME = "jython";
	private final PythonInterpreter _python = new PythonInterpreter();

	/**
	 * @see l2server.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/**
	 * @see l2server.gameserver.taskmanager.Task#onTimeElapsed(l2server.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_python.cleanup();
		_python.exec("import sys");
		_python.execfile(Config.DATA_FOLDER + "scripts/cron/" + task.getParams()[2]);
	}
}
