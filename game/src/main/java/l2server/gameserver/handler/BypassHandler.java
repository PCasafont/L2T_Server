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
 * @author nBd
 */
public class BypassHandler {
	private static Logger log = LoggerFactory.getLogger(BypassHandler.class.getName());

	private Map<Integer, IBypassHandler> datatable;

	public static BypassHandler getInstance() {
		return SingletonHolder.instance;
	}

	private BypassHandler() {
		datatable = new HashMap<>();
	}

	public void registerBypassHandler(IBypassHandler handler) {
		for (String element : handler.getBypassList()) {
			if (Config.DEBUG) {
				log.debug("Adding handler for command " + element);
			}

			datatable.put(element.toLowerCase().hashCode(), handler);
		}
	}

	public IBypassHandler getBypassHandler(String BypassCommand) {
		String command = BypassCommand;

		if (BypassCommand.contains(" ")) {
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}

		if (Config.DEBUG) {
			log.debug("getting handler for command: " + command + " -> " + (datatable.get(command.hashCode()) != null));
		}

		return datatable.get(command.toLowerCase().hashCode());
	}

	public int size() {
		return datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final BypassHandler instance = new BypassHandler();
	}
}
