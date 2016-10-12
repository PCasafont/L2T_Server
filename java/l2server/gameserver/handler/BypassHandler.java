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

import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.log.Log;

import java.util.logging.Level;

/**
 * @author nBd
 */
public class BypassHandler
{

	private TIntObjectHashMap<IBypassHandler> _datatable;

	public static BypassHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private BypassHandler()
	{
		_datatable = new TIntObjectHashMap<>();
	}

	public void registerBypassHandler(IBypassHandler handler)
	{
		for (String element : handler.getBypassList())
		{
			if (Config.DEBUG)
			{
				Log.log(Level.FINE, "Adding handler for command " + element);
			}

			_datatable.put(element.toLowerCase().hashCode(), handler);
		}
	}

	public IBypassHandler getBypassHandler(String BypassCommand)
	{
		String command = BypassCommand;

		if (BypassCommand.contains(" "))
		{
			command = BypassCommand.substring(0, BypassCommand.indexOf(" "));
		}

		if (Config.DEBUG)
		{
			Log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
		}

		return _datatable.get(command.toLowerCase().hashCode());
	}

	public int size()
	{
		return _datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final BypassHandler _instance = new BypassHandler();
	}
}
