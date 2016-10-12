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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:33 $
 */
public final class RequestGiveItemToPet extends L2GameClientPacket
{

	private int _objectId;

	private long _amount;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("giveitemtopet"))
		{
			player.sendMessage("You give items to pet too fast.");
			return;
		}

		if (player.getActiveEnchantItem() != null)
		{
			return;
		}
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getReputation() < 0)
		{
			return;
		}

		if (player.getPrivateStoreType() != 0)
		{
			player.sendMessage("Cannot exchange items while trading");
			return;
		}

		// Exploit Fix for Hero weapons Uses pet Inventory to buy New One.
		// [L2JOneo]
		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);

		if (item == null)
		{
			return;
		}

		if (item.isHeroItem())
		{
			player.sendMessage("Duo To Hero Weapons Protection u Canot Use Pet's Inventory");
			return;
		}

		if (item.isAugmented())
		{
			return;
		}

		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}

		L2PetInstance pet = player.getPet();
		if (pet.isDead())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}

		if (_amount < 0)
		{
			return;
		}
		if (!pet.getInventory().validateCapacity(item))
		{
			pet.getOwner()
					.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS));
			return;
		}
		if (!pet.getInventory().validateWeight(item, _amount))
		{
			pet.getOwner().sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED));
			return;
		}

		if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
		{
			Log.warning("Invalid item transfer request: " + pet.getName() + " (pet) --> " + player.getName());
		}
	}
}
