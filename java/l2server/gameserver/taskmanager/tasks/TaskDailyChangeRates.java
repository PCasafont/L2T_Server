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

import l2server.gameserver.taskmanager.Task;
import l2server.gameserver.taskmanager.TaskManager;
import l2server.gameserver.taskmanager.TaskManager.ExecutedTask;
import l2server.gameserver.taskmanager.TaskTypes;

/**
 * @author Soul
 */
public class TaskDailyChangeRates extends Task
{

	private static final String NAME = "daily_rate_change";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		/*String day = "fail";

		// First, return params to its default values
		Config.RATE_XP = Config.RATE_XP_REAL;
		Config.RATE_SP = Config.RATE_SP_REAL;
		Config.RATE_DROP_ITEMS_ID.put(57, Config.RATE_DROP_ADENA_REAL);
		Config.RATE_PARTY_XP = Config.RATE_PARTY_XP_REAL;
		Config.RATE_PARTY_SP = Config.RATE_PARTY_SP_REAL;
		Config.RATE_DROP_ITEMS = Config.RATE_DROP_ITEMS_REAL;
		Config.RATE_DROP_SPOIL = Config.RATE_DROP_SPOIL_REAL;

		// +20% -> 1.2 ; -20% = 0.80

		// Now, upgrade rates according to bonus!
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK))
		{
			case Calendar.MONDAY:
				Config.RATE_DROP_ITEMS_ID.put(57, Config.RATE_DROP_ADENA_REAL * 1.2f);
				day = "Adena";
				break;
			case Calendar.TUESDAY:
				Config.RATE_XP *= 6.0f/5.0f;
				Config.RATE_PARTY_XP *= 5.0f/6.0f;
				day = "XP";
				break;
			case Calendar.WEDNESDAY:
				Config.RATE_DROP_ITEMS *= 1.2f;
				day = "Drop";
				break;
			case Calendar.THURSDAY:
				Config.RATE_SP *= 6.0f/5.0f;
				Config.RATE_PARTY_SP *= 5.0f/6.0f;
				day = "SP";
				break;
			case Calendar.FRIDAY:
				Config.RATE_DROP_SPOIL *= 1.2f;
				day = "Spoil";
				break;
			case Calendar.SATURDAY:
				Config.RATE_PARTY_XP *= 1.2f;
				day = "Party XP";
				break;
			case Calendar.SUNDAY:
				Config.RATE_PARTY_SP *= 1.2f;
				day = "Party SP";
				break;
			default:
				Log.warning("[" + NAME + "]: For some reason, we're out the 7 days of the week. That's akward.");
				return;
		}

		Log.config("[" + NAME + "]: Rates Changed. Today's " + day + " day!");*/
	}

	@Override
	public void initialize()
	{
		super.initialize();
		// This task executes once every 24h (00:00:01 to be exact)
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:00:01", "");
	}
}
