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

import l2server.DatabasePool;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.Item.ItemLocation;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author DS
 */
public class Mail extends ItemContainer {
	private static Logger log = LoggerFactory.getLogger(Mail.class.getName());

	private final int ownerId;
	private int messageId;

	public Mail(int objectId, int messageId) {
		ownerId = objectId;
		this.messageId = messageId;
	}

	@Override
	public String getName() {
		return "Mail";
	}

	@Override
	public Player getOwner() {
		return null;
	}

	@Override
	public ItemLocation getBaseLocation() {
		return ItemLocation.MAIL;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setNewMessageId(int messageId) {
		this.messageId = messageId;
		for (Item item : items.values()) {
			if (item == null) {
				continue;
			}

			item.setLocation(getBaseLocation(), messageId);
		}

		updateDatabase();
	}

	public void returnToWh(ItemContainer wh) {
		for (Item item : items.values()) {
			if (item == null) {
				continue;
			}
			if (wh == null) {
				item.setLocation(ItemLocation.WAREHOUSE);
			} else {
				transferItem("Expire", item.getObjectId(), item.getCount(), wh, null, null);
			}
		}
	}

	@Override
	protected void addItem(Item item) {
		super.addItem(item);
		item.setLocation(getBaseLocation(), messageId);
	}

	/*
	 * Allow saving of the items without owner
	 */
	@Override
	public void updateDatabase() {
		for (Item item : items.values()) {
			if (item != null) {
				item.updateDatabase(true);
			}
		}
	}

	@Override
	public void restore() {
		Connection con = null;
		PreparedStatement statement = null;
		try {
			con = DatabasePool.getInstance().getConnection();
			statement = con.prepareStatement(
					"SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, appearance, mob_id FROM items WHERE owner_id=? AND loc=? AND loc_data=?");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			statement.setInt(3, getMessageId());
			ResultSet inv = statement.executeQuery();

			Item item;
			while (inv.next()) {
				item = Item.restoreFromDb(getOwnerId(), inv);
				if (item == null) {
					continue;
				}

				World.getInstance().storeObject(item);

				// If stackable item is found just add to current quantity
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null) {
					addItem("Restore", item, null, null);
				} else {
					addItem(item);
				}
			}
			statement.close();
		} catch (Exception e) {
			log.warn("could not restore container:", e);
		} finally {
			DatabasePool.close(con);
		}
	}

	@Override
	public int getOwnerId() {
		return ownerId;
	}
}
