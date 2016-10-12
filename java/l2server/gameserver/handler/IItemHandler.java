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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.L2Playable;

import java.util.logging.Logger;

/**
 * Mother class of all itemHandlers.<BR><BR>
 * an IItemHandler implementation has to be stateless
 *
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */

public interface IItemHandler
{
	Logger _log = Logger.getLogger(IItemHandler.class.getName());

	/**
	 * Launch task associated to the item.
	 *
	 * @param playable  : L2PlayableInstance designating the player
	 * @param item      : L2ItemInstance designating the item to use
	 * @param forceUse: ctrl hold on item use
	 */
	void useItem(L2Playable playable, L2ItemInstance item, boolean forceUse);
}
