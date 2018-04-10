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

package l2server.gameserver.network.serverpackets;

import l2server.Config;
import l2server.gameserver.model.Item;
import l2server.gameserver.model.ItemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class ...
 *
 * @author Yme
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 * Rebuild 23.2.2006 by Advi
 */
public class PetInventoryUpdate extends L2ItemListPacket {
	private static Logger log = LoggerFactory.getLogger(PetInventoryUpdate.class.getName());

	
	private List<ItemInfo> items;
	
	public PetInventoryUpdate(List<ItemInfo> items) {
		this.items = items;
		if (Config.DEBUG) {
			showDebug();
		}
	}
	
	public PetInventoryUpdate() {
		this(new ArrayList<>());
	}
	
	public void addItem(Item item) {
		items.add(new ItemInfo(item));
	}
	
	public void addNewItem(Item item) {
		items.add(new ItemInfo(item, 1));
	}
	
	public void addModifiedItem(Item item) {
		items.add(new ItemInfo(item, 2));
	}
	
	public void addRemovedItem(Item item) {
		items.add(new ItemInfo(item, 3));
	}
	
	public void addItems(List<Item> items) {
		for (Item item : items) {
			this.items.add(new ItemInfo(item));
		}
	}
	
	private void showDebug() {
		for (ItemInfo item : items) {
			log.debug("oid:" + Integer.toHexString(item.getObjectId()) + " item:" + item.getItem().getName() + " last change:" + item.getChange());
		}
	}
	
	@Override
	protected final void writeImpl() {
		int count = items.size();
		writeH(count);
		for (ItemInfo item : items) {
			writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeItem(item);
		}
	}
}
