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
import l2server.gameserver.events.DamageManager;
import l2server.gameserver.events.LotterySystem;
import l2server.gameserver.taskmanager.Task;
import l2server.gameserver.taskmanager.TaskManager;
import l2server.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2server.gameserver.taskmanager.TaskTypes;

import java.util.Calendar;

public class TaskCustomTasks extends Task
{
	public static final String NAME = "custom_tasks";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Calendar cal = Calendar.getInstance();
		int day = cal.get(Calendar.DAY_OF_WEEK);

		// Each Monday tasks
		if (day == Calendar.MONDAY)
		{
			if (Config.ENABLE_CUSTOM_LOTTERY)
			{
				LotterySystem.getInstance().giveRewardsAndReset();
			}
			if (Config.ENABLE_CUSTOM_DAMAGE_MANAGER)
			{
				DamageManager.getInstance().giveRewardsAndReset();
			}
		}
	}

	@Override
	public void initialize()
	{
		super.initialize();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:10:00", "");
	}
}
