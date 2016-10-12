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

import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;

/**
 * @author Kerberos
 */
public class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
	private int _itemId;
	private ArrayList<L2ItemInstance> _inventoryItems;
	private byte _attribute;
	private int _level;
	private long _maxCount;

	public ExChooseInventoryAttributeItem(L2PcInstance player, L2ItemInstance item)
	{
		_inventoryItems = new ArrayList<>();
		_itemId = item.getItemId();
		for (L2ItemInstance _item : player.getInventory().getItems())
		{
			if (_item.isEquipable())
			{
				_inventoryItems.add(_item);
			}
		}
		_attribute = Elementals.getItemElement(_itemId);
		if (_attribute == Elementals.NONE)
		{
			throw new IllegalArgumentException("Undefined Atribute item: " + item);
		}
		_level = Elementals.getMaxElementLevel(_itemId);

		// Armors have the opposite element
		if (item.isArmor())
		{
			_attribute = Elementals.getOppositeElement(_attribute);
		}

		_maxCount = item.getCount();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_itemId);
		writeQ(_maxCount); // Maximum items that can be attempted to use
		// Must be 0x01 for stone/crystal attribute type
		writeD(_attribute == Elementals.FIRE ? 1 : 0); // Fire
		writeD(_attribute == Elementals.WATER ? 1 : 0); // Water
		writeD(_attribute == Elementals.WIND ? 1 : 0); // Wind
		writeD(_attribute == Elementals.EARTH ? 1 : 0); // Earth
		writeD(_attribute == Elementals.HOLY ? 1 : 0); // Holy
		writeD(_attribute == Elementals.DARK ? 1 : 0); // Unholy
		writeD(_level); // Item max attribute level
		writeD(_inventoryItems.size()); //equipable items count
		for (L2ItemInstance item : _inventoryItems)
		{
			writeD(item.getObjectId());
		}
	}
}
