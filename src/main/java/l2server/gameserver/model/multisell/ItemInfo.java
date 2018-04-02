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

import l2server.gameserver.model.Elementals;
import l2server.gameserver.model.Item;

/**
 * @author DS
 */
public class ItemInfo {
	private final int enchantLevel;
	private int[] ensoulEffectIds;
	private int[] ensoulSpecialEffectIds;
	private final long augmentId;
	private final byte elementId;
	private final int elementPower;
	private final int[] elementals = new int[6];

	public ItemInfo(Item item) {
		enchantLevel = item.getEnchantLevel();
		ensoulEffectIds = item.getEnsoulEffectIds();
		ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();
		augmentId = item.getAugmentation() != null ? item.getAugmentation().getId() : 0;
		elementId = item.getAttackElementType();
		elementPower = item.getAttackElementPower();
		elementals[0] = item.getElementDefAttr(Elementals.FIRE);
		elementals[1] = item.getElementDefAttr(Elementals.WATER);
		elementals[2] = item.getElementDefAttr(Elementals.WIND);
		elementals[3] = item.getElementDefAttr(Elementals.EARTH);
		elementals[4] = item.getElementDefAttr(Elementals.HOLY);
		elementals[5] = item.getElementDefAttr(Elementals.DARK);
	}

	public final int getEnchantLevel() {
		return enchantLevel;
	}

	public int[] getEnsoulEffectIds() {
		return ensoulEffectIds;
	}

	public int[] getEnsoulSpecialEffectIds() {
		return ensoulSpecialEffectIds;
	}

	public final long getAugmentId() {
		return augmentId;
	}

	public final byte getElementId() {
		return elementId;
	}

	public final int getElementPower() {
		return elementPower;
	}

	public final int[] getElementals() {
		return elementals;
	}
}
