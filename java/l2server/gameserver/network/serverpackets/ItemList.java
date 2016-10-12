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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.log.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * sample
 * <p>
 * 11 // packet ID
 * 00 00 // show window
 * 15 00 // item count
 * <p>
 * 04 00 // item type id
 * <p>
 * 48 B8 B9 40 // object id
 * 47 09 00 00 //item id
 * 0C 00 00 00 // location slot
 * 01 00 00 00 00 00 00 00 // amount
 * 05 00 // item type 2
 * 00 00 // custom type 1
 * 00 00 // is equipped?
 * 00 00 00 00 // body part
 * 0F 00 // enchant level
 * 00 00 // custom type 2
 * 00 00 // augmentation data
 * 00 00 // augmentation data
 * FF FF FF FF //mana
 * FE FF // attack element
 * 00 00 // attack element power
 * 00 00 // fire defence element power
 * 00 00 // water defence element power
 * 00 00 // wind defence element power
 * 00 00 // earth defence element power
 * 00 00 // holy defence element power
 * 00 00 // unholy defence element power
 * F1 D8 FF FF // remaining time = -9999
 * <p>
 * <p>
 * format   hh (h dddQhhhdhhhhdhhhhhhhhd)
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public final class ItemList extends L2ItemListPacket
{
	private PcInventory _inventory;
	private L2ItemInstance[] _items;
	private boolean _showWindow;
	private int length;
	private ArrayList<L2ItemInstance> _questItems;

	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_inventory = cha.getInventory();
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
		_questItems = new ArrayList<>();
		for (int i = 0; i < _items.length; i++)
		{
			if (_items[i] != null && _items[i].isQuestItem())
			{
				_questItems.add(_items[i]); // add to questinv
				_items[i] = null; // remove from list
			}
			else
			{
				length++; // increase size
			}
		}

		// Sort here because the client doesn't give a damn about location slots
		Arrays.sort(_items, (i1, i2) ->
		{
			if (i1 == null)
			{
				if (i2 == null)
				{
					return 0;
				}
				return -1;
			}
			if (i2 == null)
			{
				return 1;
			}
			return i1.getLocationSlot() - i2.getLocationSlot();
		});
	}

	@SuppressWarnings("unused")
	private void showDebug()
	{
		for (L2ItemInstance temp : _items)
		{
			Log.fine("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" +
					temp.getItem().getType2());
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeH(_showWindow ? 0x01 : 0x00);

		//int count = _items.length;
		writeH(length);

		for (L2ItemInstance item : _items)
		{
			if (item == null || item.getItem() == null)
			{
				continue;
			}

			writeItem(item);
		}
		if (_inventory.hasInventoryBlock())
		{
			writeH(_inventory.getBlockItems().length);
			writeC(_inventory.getBlockMode());
			for (int i : _inventory.getBlockItems())
			{
				writeD(i);
			}
		}
		else
		{
			writeH(0x00);
		}
	}

	@Override
	public void runImpl()
	{
		getClient().sendPacket(new ExQuestItemList(_questItems, getClient().getActiveChar().getInventory()));
		getClient().sendPacket(new ExAdenaInvenCount(getClient().getActiveChar().getAdena(),
				getClient().getActiveChar().getInventory().getSize(false)));
	}
}
