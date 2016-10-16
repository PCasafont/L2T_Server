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

package l2server.gameserver.model.itemcontainer;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2ItemInstance.ItemLocation;
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.gameserver.templates.item.L2EtcItemType;
import l2server.gameserver.templates.item.L2Item;
import lombok.Getter;

public class PetInventory extends Inventory
{
	@Getter private final L2PetInstance owner;

	public PetInventory(L2PetInstance owner)
	{
		this.owner = owner;
	}

	@Override
	public int getOwnerId()
	{
		// gets the L2PcInstance-owner's ID
		int id;
		try
		{
			id = owner.getOwner().getObjectId();
		}
		catch (NullPointerException e)
		{
			return 0;
		}
		return id;
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight()
	{
		super.refreshWeight();
		getOwner().updateAndBroadcastStatus(1);
	}

	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;

		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) &&
				item.getItemType() != L2EtcItemType.HERB)
		{
			slots++;
		}

		return validateCapacity(slots);
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return items.size() + slots <= owner.getInventoryLimit();
	}

	public boolean validateWeight(L2ItemInstance item, long count)
	{
		int weight = 0;
		L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
		if (template == null)
		{
			return false;
		}
		weight += count * template.getWeight();
		return validateWeight(weight);
	}

	@Override
	public boolean validateWeight(long weight)
	{
		return totalWeight + weight <= owner.getMaxLoad();
	}

	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}

	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}

	@Override
	public void restore()
	{
		super.restore();
		// check for equiped items from other pets
		for (L2ItemInstance item : items.values())
		{
			if (item.isEquipped())
			{
				if (!item.getItem().checkCondition(getOwner(), getOwner(), false))
				{
					unEquipItemInSlot(item.getLocationSlot());
				}
			}
		}
	}

	public void transferItemsToOwner()
	{
		for (L2ItemInstance item : items.values())
		{
			getOwner().transferItem("return", item.getObjectId(), item.getCount(), getOwner().getOwner().getInventory(),
					getOwner().getOwner(), getOwner());
		}
	}
}
