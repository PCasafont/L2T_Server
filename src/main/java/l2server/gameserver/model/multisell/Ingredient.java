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
public class Ingredient implements Cloneable {
	private int itemId;
	private long itemCount;
	private float chance = 0;
	private boolean isTaxIngredient, maintainIngredient;

	private L2Item template = null;
	private ItemInfo itemInfo = null;

	public Ingredient(int itemId, long itemCount, boolean isTaxIngredient, boolean maintainIngredient) {
		this.itemId = itemId;
		this.itemCount = itemCount;
		this.isTaxIngredient = isTaxIngredient;
		this.maintainIngredient = maintainIngredient;
		if (itemId > 0) {
			template = ItemTable.getInstance().getTemplate(itemId);
		}
	}

	@Override
	public final Ingredient clone() {
		try {
			return (Ingredient) super.clone();
		} catch (CloneNotSupportedException e) {
			return null; // should not happens
		}
	}

	public final L2Item getTemplate() {
		return template;
	}

	public final void setItemInfo(L2ItemInstance item) {
		itemInfo = new ItemInfo(item);
	}

	public final void setItemInfo(ItemInfo info) {
		itemInfo = info;
	}

	public final ItemInfo getItemInfo() {
		return itemInfo;
	}

	public final int getEnchantLevel() {
		return itemInfo != null ? itemInfo.getEnchantLevel() : 0;
	}

	public final void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public final int getItemId() {
		return itemId;
	}

	public final void setItemCount(long itemCount) {
		this.itemCount = itemCount;
	}

	public final long getItemCount() {
		return itemCount;
	}

	public final void setChance(float chance) {
		this.chance = chance;
	}

	public final float getChance() {
		return chance;
	}

	public final void setIsTaxIngredient(boolean isTaxIngredient) {
		this.isTaxIngredient = isTaxIngredient;
	}

	public final boolean isTaxIngredient() {
		return isTaxIngredient;
	}

	public final void setMaintainIngredient(boolean maintainIngredient) {
		this.maintainIngredient = maintainIngredient;
	}

	public final boolean getMaintainIngredient() {
		return maintainIngredient;
	}

	public final boolean isStackable() {
		return template == null || template.isStackable();
	}

	public final boolean isArmorOrWeapon() {
		return template != null && (template instanceof L2Armor || template instanceof L2Weapon);
	}

	public final int getWeight() {
		return template == null ? 0 : template.getWeight();
	}
}
