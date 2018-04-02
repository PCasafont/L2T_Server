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

import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.Playable;
import l2server.gameserver.model.actor.instance.AirShipInstance;
import l2server.gameserver.model.actor.instance.ControllableAirShipInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

public class EnergyStarStone extends ItemSkills {
	/**
	 * @see l2server.gameserver.handler.IItemHandler#useItem(Playable, Item, boolean)
	 */
	@Override
	public void useItem(Playable playable, Item item, boolean forceUse) {
		if (!(playable instanceof Player)) {
			return;
		}

		final AirShipInstance ship = ((Player) playable).getAirShip();
		if (ship == null || !(ship instanceof ControllableAirShipInstance) || ship.getFuel() >= ship.getMaxFuel()) {
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			playable.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		super.useItem(playable, item, forceUse);
	}
}
