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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestGiveItemToPet extends L2GameClientPacket {
	
	private int objectId;
	
	private long amount;
	
	@Override
	protected void readImpl() {
		objectId = readD();
		amount = readQ();
	}
	
	@Override
	protected void runImpl() {
		Player player = getClient().getActiveChar();
		if (player == null) {
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("giveitemtopet")) {
			player.sendMessage("You give items to pet too fast.");
			return;
		}
		
		if (player.getActiveEnchantItem() != null) {
			return;
		}
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getReputation() < 0) {
			return;
		}
		
		if (player.getPrivateStoreType() != 0) {
			player.sendMessage("Cannot exchange items while trading");
			return;
		}
		
		// Exploit Fix for Hero weapons Uses pet Inventory to buy New One.
		// [L2JOneo]
		Item item = player.getInventory().getItemByObjectId(objectId);
		
		if (item == null) {
			return;
		}
		
		if (item.isHeroItem()) {
			player.sendMessage("Duo To Hero Weapons Protection u Canot Use Pet's Inventory");
			return;
		}
		
		if (item.isAugmented()) {
			return;
		}
		
		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable()) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}
		
		PetInstance pet = player.getPet();
		if (pet.isDead()) {
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}
		
		if (amount < 0) {
			return;
		}
		if (!pet.getInventory().validateCapacity(item)) {
			pet.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS));
			return;
		}
		if (!pet.getInventory().validateWeight(item, amount)) {
			pet.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED));
			return;
		}
		
		if (player.transferItem("Transfer", objectId, amount, pet.getInventory(), pet) == null) {
			log.warn("Invalid item transfer request: " + pet.getName() + " (pet) --> " + player.getName());
		}
	}
}
