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

import l2server.Config;
import l2server.DatabasePool;
import l2server.gameserver.TimeController;
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.EnsoulEffect;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Item.ItemLocation;
import l2server.gameserver.model.World;
import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Advi
 */
public abstract class ItemContainer {
	protected static Logger log = LoggerFactory.getLogger(ItemContainer.class.getName());
	
	protected final Map<Integer, Item> items = new ConcurrentHashMap<>();
	
	protected abstract Creature getOwner();
	
	protected abstract ItemLocation getBaseLocation();
	
	public String getName() {
		return "ItemContainer";
	}
	
	/**
	 * Returns the ownerID of the inventory
	 *
	 * @return int
	 */
	public int getOwnerId() {
		return getOwner() == null ? 0 : getOwner().getObjectId();
	}
	
	/**
	 * Returns the quantity of items in the inventory
	 *
	 * @return int
	 */
	public int getSize() {
		return items.size();
	}
	
	/**
	 * Returns the list of items in inventory
	 *
	 * @return Item : items in inventory
	 */
	public Item[] getItems() {
		synchronized (items) {
			return items.values().toArray(new Item[items.size()]);
		}
	}
	
	/**
	 * Returns the item from inventory by using its <B>itemId</B><BR><BR>
	 *
	 * @param itemId : int designating the ID of the item
	 * @return Item designating the item or null if not found in inventory
	 */
	public Item getItemByItemId(int itemId) {
		for (Item item : items.values()) {
			if (item != null && item.getItemId() == itemId) {
				return item;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the item's list from inventory by using its <B>itemId</B><BR><BR>
	 *
	 * @param itemId : int designating the ID of the item
	 * @return List<Item> designating the items list (empty list if not found)
	 */
	public List<Item> getItemsByItemId(int itemId) {
		List<Item> returnList = new ArrayList<>();
		for (Item item : items.values()) {
			if (item != null && item.getItemId() == itemId) {
				returnList.add(item);
			}
		}
		
		return returnList;
	}
	
	/**
	 * Returns the item from inventory by using its <B>itemId</B><BR><BR>
	 *
	 * @param itemId       : int designating the ID of the item
	 * @param itemToIgnore : used during a loop, to avoid returning the same item
	 * @return Item designating the item or null if not found in inventory
	 */
	public Item getItemByItemId(int itemId, Item itemToIgnore) {
		for (Item item : items.values()) {
			if (item != null && item.getItemId() == itemId && !item.equals(itemToIgnore)) {
				return item;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns item from inventory by using its <B>objectId</B>
	 *
	 * @param objectId : int designating the ID of the object
	 * @return Item designating the item or null if not found in inventory
	 */
	public Item getItemByObjectId(int objectId) {
		for (Item item : items.values()) {
			if (item == null) {
				continue;
			}
			
			if (item.getObjectId() == objectId) {
				return item;
			}
		}
		return null;
	}
	
	/**
	 * @see ItemContainer#getInventoryItemCount(int, int, boolean)
	 */
	public long getInventoryItemCount(int itemId, int enchantLevel) {
		return getInventoryItemCount(itemId, enchantLevel, true);
	}
	
	/**
	 * Gets count of item in the inventory
	 *
	 * @param itemId          : Item to look for
	 * @param enchantLevel    : enchant level to match on, or -1 for ANY enchant level
	 * @param includeEquipped : include equipped items
	 * @return int corresponding to the number of items matching the above conditions.
	 */
	public long getInventoryItemCount(int itemId, int enchantLevel, boolean includeEquipped) {
		long count = 0;
		
		for (Item item : items.values()) {
			if (item.getItemId() == itemId && (item.getEnchantLevel() == enchantLevel || enchantLevel < 0) &&
					(includeEquipped || !item.isEquipped())) {
				//if (item.isAvailable((Player)getOwner(), true) || item.getItem().getType2() == 3)//available or quest item
				if (item.isStackable()) {
					count = item.getCount();
				} else {
					count++;
				}
			}
		}
		
		return count;
	}
	
	/**
	 * Adds item to inventory
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param item      : Item to be added
	 * @param actor     : Player Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item addItem(String process, Item item, Player actor, Object reference) {
		Item olditem = getItemByItemId(item.getItemId());
		
		// If stackable item is found in inventory just add to current quantity
		if (olditem != null && olditem.isStackable()) {
			long count = item.getCount();
			olditem.changeCount(process, count, actor, reference);
			olditem.setLastChange(Item.MODIFIED);
			
			// And destroys the item
			ItemTable.getInstance().destroyItem(process, item, actor, reference);
			item.updateDatabase();
			item = olditem;
			
			// Updates database
			if (item.getItemId() == 57 && count < 10000 * Config.RATE_DROP_ITEMS_ID.get(57)) {
				// Small adena changes won't be saved to database all the time
				if (TimeController.getGameTicks() % 5 == 0) {
					item.updateDatabase();
				}
			} else {
				item.updateDatabase();
			}
		}
		// If item hasn't be found in inventory, create new one
		else {
			item.setOwnerId(process, getOwnerId(), actor, reference);
			item.setLocation(getBaseLocation());
			item.setLastChange(Item.ADDED);
			
			// Add item in inventory
			addItem(item);
			
			// Updates database
			item.updateDatabase();
		}
		
		refreshWeight();
		return item;
	}
	
	/**
	 * Adds item to inventory
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param itemId    : int Item Identifier of the item to be added
	 * @param count     : int Quantity of items to be added
	 * @param actor     : Player Player requesting the item add
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item addItem(String process, int itemId, long count, Player actor, Object reference) {
		Item item = getItemByItemId(itemId);
		
		// If stackable item is found in inventory just add to current quantity
		if (item != null && item.isStackable()) {
			item.changeCount(process, count, actor, reference);
			item.setLastChange(Item.MODIFIED);
			// Updates database
			if (itemId == 57 && count < 10000 * Config.RATE_DROP_ITEMS_ID.get(57)) {
				// Small adena changes won't be saved to database all the time
				if (TimeController.getGameTicks() % 5 == 0) {
					item.updateDatabase();
				}
			} else {
				item.updateDatabase();
			}
		}
		// If item hasn't be found in inventory, create new one
		else {
			for (int i = 0; i < count; i++) {
				ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
				if (template == null) {
					log.warn((actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: ", itemId);
					return null;
				}
				
				item = ItemTable.getInstance().createItem(process, itemId, template.isStackable() ? count : 1, actor, reference);
				item.setOwnerId(getOwnerId());
				item.setLocation(getBaseLocation());
				item.setLastChange(Item.ADDED);
				
				// Add item in inventory
				addItem(item);
				// Updates database
				item.updateDatabase();
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP) {
					break;
				}
			}
		}
		
		refreshWeight();
		return item;
	}
	
	/**
	 * Transfers item to another inventory
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param objectId  : int Item Identifier of the item to be transfered
	 * @param count     : int Quantity of items to be transfered
	 * @param actor     : Player Player requesting the item transfer
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the new item or the updated item in inventory
	 */
	public Item transferItem(String process, int objectId, long count, ItemContainer target, Player actor, Object reference) {
		if (target == null) {
			return null;
		}
		
		Item sourceitem = getItemByObjectId(objectId);
		if (sourceitem == null) {
			return null;
		}
		
		Item targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;
		if (targetitem != null && sourceitem.getObjectId() == targetitem.getObjectId()) {
			return null;
		}
		
		synchronized (sourceitem) {
			// check if this item still present in this container
			if (getItemByObjectId(objectId) != sourceitem) {
				return null;
			}
			
			// Check if requested quantity is available
			if (count > sourceitem.getCount()) {
				count = sourceitem.getCount();
			}
			
			// If possible, move entire item object
			if (sourceitem.getCount() == count && targetitem == null) {
				removeItem(sourceitem);
				target.addItem(process, sourceitem, actor, reference);
				targetitem = sourceitem;
			} else {
				if (sourceitem.getCount() > count) // If possible, only update counts
				{
					sourceitem.changeCount(process, -count, actor, reference);
				} else
				// Otherwise destroy old item
				{
					removeItem(sourceitem);
					ItemTable.getInstance().destroyItem(process, sourceitem, actor, reference);
				}
				
				if (targetitem != null) // If possible, only update counts
				{
					targetitem.changeCount(process, count, actor, reference);
				} else
				// Otherwise add new item
				{
					targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
				}
			}
			
			// Updates database
			sourceitem.updateDatabase(true);
			if (targetitem != sourceitem && targetitem != null) {
				targetitem.updateDatabase();
			}
			for (EnsoulEffect e : sourceitem.getEnsoulEffects()) {
				if (e != null) {
					e.removeBonus(actor);
				}
			}
			if (sourceitem.isAugmented()) {
				sourceitem.getAugmentation().removeBonus(actor);
			}
			refreshWeight();
			target.refreshWeight();
		}
		return targetitem;
	}
	
	/**
	 * Destroy item from inventory and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param item      : Item to be destroyed
	 * @param actor     : Player Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItem(String process, Item item, Player actor, Object reference) {
		return this.destroyItem(process, item, item.getCount(), actor, reference);
	}
	
	/**
	 * Destroy item from inventory and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param item      : Item to be destroyed
	 * @param actor     : Player Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItem(String process, Item item, long count, Player actor, Object reference) {
		synchronized (item) {
			// Adjust item quantity
			if (item.getCount() > count) {
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(Item.MODIFIED);
				
				// don't update often for untraced items
				if (process != null || TimeController.getGameTicks() % 10 == 0) {
					item.updateDatabase();
				}
				
				refreshWeight();
				
				return item;
			} else {
				if (item.getCount() < count) {
					return null;
				}
				
				boolean removed = removeItem(item);
				if (!removed) {
					return null;
				}
				
				ItemTable.getInstance().destroyItem(process, item, actor, reference);
				
				item.updateDatabase();
				refreshWeight();
			}
		}
		return item;
	}
	
	/**
	 * Destroy item from inventory by using its <B>objectID</B> and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param objectId  : int Item Instance identifier of the item to be destroyed
	 * @param count     : int Quantity of items to be destroyed
	 * @param actor     : Player Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItem(String process, int objectId, long count, Player actor, Object reference) {
		Item item = getItemByObjectId(objectId);
		if (item == null) {
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param itemId    : int Item identifier of the item to be destroyed
	 * @param count     : int Quantity of items to be destroyed
	 * @param actor     : Player Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return Item corresponding to the destroyed item or the updated item in inventory
	 */
	public Item destroyItemByItemId(String process, int itemId, long count, Player actor, Object reference) {
		Item item = getItemByItemId(itemId);
		if (item == null) {
			return null;
		}
		return this.destroyItem(process, item, count, actor, reference);
	}
	
	/**
	 * Destroy all items from inventory and updates database
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param actor     : Player Player requesting the item destroy
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public synchronized void destroyAllItems(String process, Player actor, Object reference) {
		for (Item item : items.values()) {
			if (item != null) {
				destroyItem(process, item, actor, reference);
			}
		}
	}
	
	/**
	 * Get warehouse adena
	 */
	public long getAdena() {
		long count = 0;
		
		for (Item item : items.values()) {
			if (item != null && item.getItemId() == 57) {
				count = item.getCount();
				return count;
			}
		}
		
		return count;
	}
	
	/**
	 * Adds item to inventory for further adjustments.
	 *
	 * @param item : Item to be added from inventory
	 */
	protected void addItem(Item item) {
		synchronized (items) {
			items.put(item.getObjectId(), item);
		}
	}
	
	/**
	 * Removes item from inventory for further adjustments.
	 *
	 * @param item : Item to be removed from inventory
	 */
	protected boolean removeItem(Item item) {
		synchronized (items) {
			return items.remove(item.getObjectId()) != null;
		}
	}
	
	/**
	 * Refresh the weight of equipment loaded
	 */
	protected void refreshWeight() {
	}
	
	/**
	 * Delete item object from world
	 */
	public void deleteMe() {
		try {
			updateDatabase();
		} catch (Exception e) {
			log.error("deletedMe()", e);
		}
		List<WorldObject> items = new ArrayList<>(this.items.values());
		items.clear();
		
		World.getInstance().removeObjects(items);
	}
	
	/**
	 * Update database with items in inventory
	 */
	public void updateDatabase() {
		if (getOwner() != null) {
			for (Item item : items.values()) {
				if (item != null) {
					if (item.getOwnerId() != getOwner().getObjectId()) {
						item.setOwnerId(getOwner().getObjectId());
						log.error(item.getName() + " had incorrect ownerId, corrected. ( " + getOwner().getName() + ")");
					}
					
					item.updateDatabase(true);
				}
			}
		}
	}
	
	/**
	 * Get back items in container from database
	 */
	public void restore() {
		Connection con = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, appearance, mob_id FROM items WHERE owner_id=? AND (loc=?)");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			ResultSet inv = statement.executeQuery();
			
			Item item;
			while (inv.next()) {
				item = Item.restoreFromDb(getOwnerId(), inv);
				if (item == null) {
					continue;
				}
				
				World.getInstance().storeObject(item);
				
				Player owner = getOwner() == null ? null : getOwner().getActingPlayer();
				
				// If stackable item is found in inventory just add to current quantity
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null) {
					addItem("Restore", item, owner, null);
				} else {
					addItem(item);
				}
			}
			
			inv.close();
			statement.close();
			refreshWeight();
		} catch (Exception e) {
			log.warn("could not restore container:", e);
		} finally {
			DatabasePool.close(con);
		}
	}
	
	public boolean validateCapacity(long slots) {
		return true;
	}
	
	public boolean validateWeight(long weight) {
		return true;
	}
	
	/**
	 * If the item is stackable validates 1 slot, if the item isn't stackable validates the item count.
	 *
	 * @param itemId the item Id to verify
	 * @param count  amount of item's weight to validate
	 * @return {@code true} if the item doesn't exists or it validates its slot count
	 */
	public boolean validateCapacityByItemId(int itemId, long count) {
		final ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
		return template == null || (template.isStackable() ? validateCapacity(1) : validateCapacity(count));
	}
	
	/**
	 * @param itemId the item Id to verify
	 * @param count  amount of item's weight to validate
	 * @return {@code true} if the item doesn't exists or it validates its weight
	 */
	public boolean validateWeightByItemId(int itemId, long count) {
		final ItemTemplate template = ItemTable.getInstance().getTemplate(itemId);
		return template == null || validateWeight(template.getWeight() * count);
	}
}
