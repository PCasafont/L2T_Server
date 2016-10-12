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
public class VoicedCommandHandler
{

	private TIntObjectHashMap<IVoicedCommandHandler> _datatable;

	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	private VoicedCommandHandler()
	{
		_datatable = new TIntObjectHashMap<>();
	}

	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (String id : ids)
		{
			if (Config.DEBUG)
			{
				Log.fine("Adding handler for command " + id);
			}
			_datatable.put(id.hashCode(), handler);
		}
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.contains(" "))
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (Config.DEBUG)
		{
			Log.fine("getting handler for command: " + command + " -> " + (_datatable.get(command.hashCode()) != null));
		}
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
		protected static final VoicedCommandHandler _instance = new VoicedCommandHandler();
	}
}
