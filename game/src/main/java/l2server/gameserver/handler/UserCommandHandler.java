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

package l2server.gameserver.handler;

import l2server.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class UserCommandHandler {
	private static Logger log = LoggerFactory.getLogger(UserCommandHandler.class.getName());



	private Map<Integer, IUserCommandHandler> datatable = new HashMap<>();

	public static UserCommandHandler getInstance() {
		return SingletonHolder.instance;
	}

	private UserCommandHandler() {
	}

	public void registerUserCommandHandler(IUserCommandHandler handler) {
		int[] ids = handler.getUserCommandList();
		for (int id : ids) {
			if (Config.DEBUG) {
				log.debug("Adding handler for user command " + id);
			}
			datatable.put(id, handler);
		}
	}

	public IUserCommandHandler getUserCommandHandler(int userCommand) {
		if (Config.DEBUG) {
			log.debug("getting handler for user command: " + userCommand);
		}
		return datatable.get(userCommand);
	}

	public int size() {
		return datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final UserCommandHandler instance = new UserCommandHandler();
	}
}
