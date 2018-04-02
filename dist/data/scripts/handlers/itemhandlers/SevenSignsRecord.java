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

package handlers.itemhandlers;

import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.SSQStatus;

/**
 * Item Handler for Seven Signs Record
 *
 * @author Tempy
 */
public class SevenSignsRecord implements IItemHandler {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		playable.sendPacket(new SSQStatus(playable.getObjectId(), 1));
	}
}
