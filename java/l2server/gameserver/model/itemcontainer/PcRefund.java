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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;

import java.util.logging.Level;

/**
 * @author DS
 */
public class PcRefund extends ItemContainer
{
	private L2PcInstance _owner;

	public PcRefund(L2PcInstance owner)
	{
		_owner = owner;
	}

	@Override
	public String getName()
	{
		return "Refund";
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.REFUND;
	}

	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		try
		{
			if (getSize() > 12)
			{
				L2ItemInstance removedItem = null;
				synchronized (_items)
				{
					removedItem = _items.remove(0);
				}

				if (removedItem != null)
				{
					ItemTable.getInstance().destroyItem("ClearRefund", removedItem, getOwner(), null);
					removedItem.updateDatabase(true);
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "addItem()", e);
		}
	}

	@Override
	public void refreshWeight()
	{
	}

	@Override
	public void deleteMe()
	{
		try
		{
			for (L2ItemInstance item : _items.values())
			{
				if (item != null)
				{
					ItemTable.getInstance().destroyItem("ClearRefund", item, getOwner(), null);
					item.updateDatabase(true);
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "deleteMe()", e);
		}
		_items.clear();
	}

	@Override
	public void restore()
	{
	}
}
