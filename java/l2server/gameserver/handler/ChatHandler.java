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
 * This class handles all chat handlers
 *
 * @author durgus
 */
public class ChatHandler
{

	private TIntObjectHashMap<IChatHandler> _datatable;

	public static ChatHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Singleton constructor
	 */
	private ChatHandler()
	{
		_datatable = new TIntObjectHashMap<>();
	}

	/**
	 * Register a new chat handler
	 *
	 * @param handler
	 */
	public void registerChatHandler(IChatHandler handler)
	{
		int[] ids = handler.getChatTypeList();
		for (int id : ids)
		{
			if (Config.DEBUG)
			{
				Log.fine("Adding handler for chat type " + id);
			}
			_datatable.put(id, handler);
		}
	}

	/**
	 * Get the chat handler for the given chat type
	 *
	 * @param chatType
	 * @return
	 */
	public IChatHandler getChatHandler(int chatType)
	{
		return _datatable.get(chatType);
	}

	/**
	 * Returns the size
	 *
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ChatHandler _instance = new ChatHandler();
	}
}
