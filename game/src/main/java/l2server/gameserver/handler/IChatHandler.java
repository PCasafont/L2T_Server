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

import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for chat handlers
 *
 * @author durgus
 */
public interface IChatHandler {
	Logger log = LoggerFactory.getLogger(IChatHandler.class.getName());
	
	/**
	 * Handles a specific type of chat messages
	 *
	 */
	void handleChat(int type, Player activeChar, String target, String text);

	/**
	 * Returns a list of all chat types registered to this handler
	 *
	 */
	int[] getChatTypeList();
}
