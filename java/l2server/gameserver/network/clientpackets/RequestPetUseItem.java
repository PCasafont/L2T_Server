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
import l2server.gameserver.datatables.PetDataTable;
import l2server.gameserver.handler.IItemHandler;
import l2server.gameserver.handler.ItemHandler;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.PetItemList;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

public final class RequestPetUseItem extends L2GameClientPacket
{

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		//TODO: implement me properly
		//readQ();
		//readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final L2PetInstance pet = activeChar.getPet();
		if (pet == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("pet use item"))
		{
			return;
		}

		final L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}

		if (activeChar.isAlikeDead() || pet.isDead())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}

		if (Config.DEBUG)
		{
			Log.finest(activeChar.getObjectId() + ": pet use item " + _objectId);
		}

		if (!item.isEquipped())
		{
			if (!item.getItem().checkCondition(pet, pet, true))
			{
				return;
			}
		}

		//check if the item matches the pet
		if (item.isEquipable())
		{
			// all pet items have condition
			if (!item.getItem().isConditionAttached())
			{
				activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
				return;
			}
			useItem(pet, item, activeChar);
			return;
		}
		else
		{
			final int itemId = item.getItemId();
			if (PetDataTable.isPetFood(itemId))
			{
				if (pet.canEatFoodId(itemId))
				{
					useItem(pet, item, activeChar);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
					return;
				}
			}
		}

		final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
		if (handler != null)
		{
			useItem(pet, item, activeChar);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
		}

	}

	private void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
	{
		if (item.isEquipable())
		{
			if (item.isEquipped())
			{
				pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
			}
			else
			{
				pet.getInventory().equipItem(item);
			}

			activeChar.sendPacket(new PetItemList(pet));
			pet.updateAndBroadcastStatus(1);
		}
		else
		{
			final IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getEtcItem());
			if (handler != null)
			{
				handler.useItem(pet, item, false);
				pet.updateAndBroadcastStatus(1);
			}
			else
			{
				Log.warning("no itemhandler registered for itemId:" + item.getItemId());
			}
		}
	}
}
