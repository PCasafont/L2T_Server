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
import l2server.gameserver.model.L2ItemInstance;

/**
 * @author DS
 */
public class ItemInfo
{
	private final int enchantLevel;
	private int[] ensoulEffectIds;
	private int[] ensoulSpecialEffectIds;
	private final long augmentId;
	private final byte elementId;
	private final int elementPower;
	private final int[] elementals = new int[6];

	public ItemInfo(L2ItemInstance item)
	{
		this.enchantLevel = item.getEnchantLevel();
		this.ensoulEffectIds = item.getEnsoulEffectIds();
		this.ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();
		this.augmentId = item.getAugmentation() != null ? item.getAugmentation().getId() : 0;
		this.elementId = item.getAttackElementType();
		this.elementPower = item.getAttackElementPower();
		this.elementals[0] = item.getElementDefAttr(Elementals.FIRE);
		this.elementals[1] = item.getElementDefAttr(Elementals.WATER);
		this.elementals[2] = item.getElementDefAttr(Elementals.WIND);
		this.elementals[3] = item.getElementDefAttr(Elementals.EARTH);
		this.elementals[4] = item.getElementDefAttr(Elementals.HOLY);
		this.elementals[5] = item.getElementDefAttr(Elementals.DARK);
	}

	public final int getEnchantLevel()
	{
		return this.enchantLevel;
	}

	public int[] getEnsoulEffectIds()
	{
		return this.ensoulEffectIds;
	}

	public int[] getEnsoulSpecialEffectIds()
	{
		return this.ensoulSpecialEffectIds;
	}

	public final long getAugmentId()
	{
		return this.augmentId;
	}

	public final byte getElementId()
	{
		return this.elementId;
	}

	public final int getElementPower()
	{
		return this.elementPower;
	}

	public final int[] getElementals()
	{
		return this.elementals;
	}
}
