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

import l2tserver.gameserver.instancemanager.GlobalVariablesManager;
import l2tserver.gameserver.taskmanager.Task;
import l2tserver.gameserver.taskmanager.TaskManager;
import l2tserver.gameserver.taskmanager.TaskTypes;
import l2tserver.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Gigiikun
 */
public class TaskGlobalVariablesSave extends Task
{
	
	public static final String NAME = "global_varibales_save";
	
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
		GlobalVariablesManager.getInstance().saveVars();
	}
	
	/**
	 * 
	 * @see l2tserver.gameserver.taskmanager.Task#initialize()
	 */
	@Override
	public void initialize()
	{
		super.initialize();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "500000", "1800000", "");
	}
}