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

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{

	private TIntObjectHashMap<IAdminCommandHandler> _datatable;

	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new TIntObjectHashMap<>();
	}

	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String id : ids)
		{
			if (Config.DEBUG)
			{
				Log.fine("Adding handler for command " + id);
			}
			_datatable.put(id.hashCode(), handler);
		}
	}

	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.contains(" "))
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		if (Config.DEBUG)
		{
			Log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
		}
		//Log.info(command);

		return _datatable.get(command.hashCode());
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}
