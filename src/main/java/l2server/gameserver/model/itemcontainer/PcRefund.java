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
import l2server.gameserver.model.actor.instance.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

/**
 * @author DS
 */
public class PcRefund extends ItemContainer {
	private static Logger log = LoggerFactory.getLogger(PcRefund.class.getName());


	private Player owner;

	public PcRefund(Player owner) {
		this.owner = owner;
	}

	@Override
	public String getName() {
		return "Refund";
	}

	@Override
	public Player getOwner() {
		return owner;
	}

	@Override
	public ItemLocation getBaseLocation() {
		return ItemLocation.REFUND;
	}

	@Override
	protected void addItem(Item item) {
		super.addItem(item);
		try {
			if (getSize() > 12) {
				Item removedItem = null;
				synchronized (items) {
					removedItem = items.remove(0);
				}

				if (removedItem != null) {
					ItemTable.getInstance().destroyItem("ClearRefund", removedItem, getOwner(), null);
					removedItem.updateDatabase(true);
				}
			}
		} catch (Exception e) {
			log.error("addItem()", e);
		}
	}

	@Override
	public void refreshWeight() {
	}

	@Override
	public void deleteMe() {
		try {
			for (Item item : items.values()) {
				if (item != null) {
					ItemTable.getInstance().destroyItem("ClearRefund", item, getOwner(), null);
					item.updateDatabase(true);
				}
			}
		} catch (Exception e) {
			log.error("deleteMe()", e);
		}
		items.clear();
	}

	@Override
	public void restore() {
	}
}
