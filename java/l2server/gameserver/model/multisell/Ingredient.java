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

package l2server.gameserver.model.multisell;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.templates.item.L2Armor;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.templates.item.L2Weapon;

/**
 * @author DS
 */
public class Ingredient implements Cloneable
{
	private int _itemId;
	private long _itemCount;
	private float _chance = 0;
	private boolean _isTaxIngredient, _maintainIngredient;

	private L2Item _template = null;
	private ItemInfo _itemInfo = null;

	public Ingredient(int itemId, long itemCount, boolean isTaxIngredient, boolean maintainIngredient)
	{
		_itemId = itemId;
		_itemCount = itemCount;
		_isTaxIngredient = isTaxIngredient;
		_maintainIngredient = maintainIngredient;
		if (_itemId > 0)
		{
			_template = ItemTable.getInstance().getTemplate(_itemId);
		}
	}

	@Override
	public final Ingredient clone()
	{
		try
		{
			return (Ingredient) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null; // should not happens
		}
	}

	public final L2Item getTemplate()
	{
		return _template;
	}

	public final void setItemInfo(L2ItemInstance item)
	{
		_itemInfo = new ItemInfo(item);
	}

	public final void setItemInfo(ItemInfo info)
	{
		_itemInfo = info;
	}

	public final ItemInfo getItemInfo()
	{
		return _itemInfo;
	}

	public final int getEnchantLevel()
	{
		return _itemInfo != null ? _itemInfo.getEnchantLevel() : 0;
	}

	public final void setItemId(int itemId)
	{
		_itemId = itemId;
	}

	public final int getItemId()
	{
		return _itemId;
	}

	public final void setItemCount(long itemCount)
	{
		_itemCount = itemCount;
	}

	public final long getItemCount()
	{
		return _itemCount;
	}

	public final void setChance(float chance)
	{
		_chance = chance;
	}

	public final float getChance()
	{
		return _chance;
	}

	public final void setIsTaxIngredient(boolean isTaxIngredient)
	{
		_isTaxIngredient = isTaxIngredient;
	}

	public final boolean isTaxIngredient()
	{
		return _isTaxIngredient;
	}

	public final void setMaintainIngredient(boolean maintainIngredient)
	{
		_maintainIngredient = maintainIngredient;
	}

	public final boolean getMaintainIngredient()
	{
		return _maintainIngredient;
	}

	public final boolean isStackable()
	{
		return _template == null || _template.isStackable();
	}

	public final boolean isArmorOrWeapon()
	{
		return _template != null && (_template instanceof L2Armor || _template instanceof L2Weapon);
	}

	public final int getWeight()
	{
		return _template == null ? 0 : _template.getWeight();
	}
}
