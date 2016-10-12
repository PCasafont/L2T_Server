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

import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.taskmanager.Task;
import l2server.gameserver.taskmanager.TaskManager;
import l2server.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2server.gameserver.taskmanager.TaskTypes;
import l2server.log.Log;

/**
 * Updates all data of Olympiad nobles in db
 *
 * @author godson
 */
public class TaskOlympiadSave extends Task
{

	public static final String NAME = "olympiad_save";

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
		if (Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			Log.info("Olympiad System: Data updated.");
		}
	}

	/**
	 * @see l2server.gameserver.taskmanager.Task#initialize()
	 */
	@Override
	public void initialize()
	{
		super.initialize();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
	}
}
