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
package l2tserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2tserver.Config;
import l2tserver.gameserver.model.ItemInfo;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.log.Log;

/**
 * This class ...
 *
 * @author Yme
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:57 $
 * Rebuild 23.2.2006 by Advi
 */
public class PetInventoryUpdate extends L2ItemListPacket
{
	
	private static final String _S__37_INVENTORYUPDATE = "[S] b4 InventoryUpdate";
	private List<ItemInfo> _items;
	
	/**
	 * @param items
	 */
	public PetInventoryUpdate(List<ItemInfo> items)
	{
		_items = items;
		if (Config.DEBUG)
		{
			showDebug();
		}
	}
	
	public PetInventoryUpdate()
	{
		this(new ArrayList<ItemInfo>());
	}
	
	public void addItem(L2ItemInstance item) { _items.add(new ItemInfo(item)); }
	public void addNewItem(L2ItemInstance item) { _items.add(new ItemInfo(item, 1)); }
	public void addModifiedItem(L2ItemInstance item) { _items.add(new ItemInfo(item, 2)); }
	public void addRemovedItem(L2ItemInstance item) { _items.add(new ItemInfo(item, 3)); }
	public void addItems(List<L2ItemInstance> items) { for (L2ItemInstance item : items) _items.add(new ItemInfo(item)); }
	
	private void showDebug()
	{
		for (ItemInfo item : _items)
		{
			Log.fine("oid:" + Integer.toHexString(item.getObjectId()) +
					" item:" + item.getItem().getName()+" last change:" + item.getChange());
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb4);
		int count = _items.size();
		writeH(count);
		for (ItemInfo item : _items)
		{
			writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeItem(item);
		}
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__37_INVENTORYUPDATE;
	}
}
