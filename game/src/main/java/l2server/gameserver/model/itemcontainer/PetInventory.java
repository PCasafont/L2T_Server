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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Item.ItemLocation;
import l2server.gameserver.model.actor.instance.PetInstance;
import l2server.gameserver.templates.item.EtcItemType;
import l2server.gameserver.templates.item.ItemTemplate;

public class PetInventory extends Inventory {
	private final PetInstance owner;
	
	public PetInventory(PetInstance owner) {
		this.owner = owner;
	}
	
	@Override
	public PetInstance getOwner() {
		return owner;
	}
	
	@Override
	public int getOwnerId() {
		// gets the Player-owner's ID
		int id;
		try {
			id = owner.getOwner().getObjectId();
		} catch (NullPointerException e) {
			return 0;
		}
		return id;
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	@Override
	protected void refreshWeight() {
		super.refreshWeight();
		getOwner().updateAndBroadcastStatus(1);
	}
	
	public boolean validateCapacity(Item item) {
		int slots = 0;
		
		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != EtcItemType.HERB) {
			slots++;
		}
		
		return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(long slots) {
		return items.size() + slots <= owner.getInventoryLimit();
	}
	
	public boolean validateWeight(Item item, long count) {
		int weight = 0;
		ItemTemplate template = ItemTable.getInstance().getTemplate(item.getItemId());
		if (template == null) {
			return false;
		}
		weight += count * template.getWeight();
		return validateWeight(weight);
	}
	
	@Override
	public boolean validateWeight(long weight) {
		return totalWeight + weight <= owner.getMaxLoad();
	}
	
	@Override
	protected ItemLocation getBaseLocation() {
		return ItemLocation.PET;
	}
	
	@Override
	protected ItemLocation getEquipLocation() {
		return ItemLocation.PET_EQUIP;
	}
	
	@Override
	public void restore() {
		super.restore();
		// check for equiped items from other pets
		for (Item item : items.values()) {
			if (item.isEquipped()) {
				if (!item.getItem().checkCondition(getOwner(), getOwner(), false)) {
					unEquipItemInSlot(item.getLocationSlot());
				}
			}
		}
	}
	
	public void transferItemsToOwner() {
		for (Item item : items.values()) {
			getOwner().transferItem("return",
					item.getObjectId(),
					item.getCount(),
					getOwner().getOwner().getInventory(),
					getOwner().getOwner(),
					getOwner());
		}
	}
}
