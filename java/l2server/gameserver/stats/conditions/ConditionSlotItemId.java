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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemcontainer.Inventory;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionSlotItemId.
 *
 * @author mkizub
 */
public final class ConditionSlotItemId extends ConditionInventory
{

	private final int _itemId;
	private final int _enchantLevel;

	/**
	 * Instantiates a new condition slot item id.
	 *
	 * @param slot         the slot
	 * @param itemId       the item id
	 * @param enchantLevel the enchant level
	 */
	public ConditionSlotItemId(int slot, int itemId, int enchantLevel)
	{
		super(slot);
		_itemId = itemId;
		_enchantLevel = enchantLevel;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.ConditionInventory#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}
		Inventory inv = ((L2PcInstance) env.player).getInventory();
		L2ItemInstance item = inv.getPaperdollItem(_slot);
		if (item == null)
		{
			return _itemId == 0;
		}
		return item.getItemId() == _itemId && item.getEnchantLevel() >= _enchantLevel;
	}
}
