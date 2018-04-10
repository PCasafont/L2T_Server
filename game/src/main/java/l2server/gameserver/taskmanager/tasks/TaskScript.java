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

import l2server.gameserver.scripting.L2ScriptEngineManager;
import l2server.gameserver.taskmanager.Task;
import l2server.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.File;

/**
 * @author janiii
 */
public class TaskScript extends Task {
	private static Logger log = LoggerFactory.getLogger(TaskScript.class.getName());

	public static final String NAME = "script";

	/**
	 * @see l2server.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * @see l2server.gameserver.taskmanager.Task#onTimeElapsed(l2server.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task) {
		File file = new File(L2ScriptEngineManager.getSCRIPT_FOLDER(), "cron/" + task.getParams()[2]);
		if (file.isFile()) {
			try {
				L2ScriptEngineManager.INSTANCE.executeScript(file);
			} catch (ScriptException e) {
				log.warn("Failed loading: " + task.getParams()[2]);
				L2ScriptEngineManager.INSTANCE.reportScriptFileError(file, e);
			} catch (Exception e) {
				log.warn("Failed loading: " + task.getParams()[2]);
			}
		} else {
			log.warn("File Not Found: " + task.getParams()[2]);
		}
	}
}
