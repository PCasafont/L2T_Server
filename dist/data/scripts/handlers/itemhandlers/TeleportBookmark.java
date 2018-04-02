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
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * Teleport Bookmark Slot Handler
 *
 * @author ShanSoft
 */
public class TeleportBookmark implements IItemHandler {

	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (playable == null || item == null || !(playable instanceof Player)) {
			return;
		}

		Player player = (Player) playable;

		if (player.getBookMarkSlot() >= 9) {
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT));
			return;
		}

		player.destroyItem("Consume", item.getObjectId(), 1, null, false);

		player.setBookMarkSlot(player.getBookMarkSlot() + 3);
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED));

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(item.getItemId());
		player.sendPacket(sm);
	}
}
