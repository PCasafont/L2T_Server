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

package l2server.gameserver.templates.item;

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.network.serverpackets.L2ItemListPacket.ItemInstanceInfo;
import lombok.Getter;

/**
 * This class contains L2ItemInstance<BR>
 * Use to sort L2ItemInstance of :
 * <LI>L2Armor</LI>
 * <LI>L2EtcItem</LI>
 * <LI>L2Weapon</LI>
 *
 * @version $Revision: 1.7.2.2.2.5 $ $Date: 2005/04/06 18:25:18 $
 */
public class L2WarehouseItem implements ItemInstanceInfo
{
	@Getter private L2Item item;
	private int objectId;
	private long count;
	private int owner;
	private int locationSlot;
	private int enchant;
	private int grade;
	private boolean isSoulEnhanced;
	@Getter private int[] ensoulEffectIds;
	@Getter private int[] ensoulSpecialEffectIds;
	private boolean isAugmented;
	private long augmentationId;
	private int customType1;
	private int customType2;
	private int mana;

	private byte elemAtkType = -2;
	private int elemAtkPower = 0;
	private int[] elemDefAttr = {0, 0, 0, 0, 0, 0};
	private boolean elemEnchanted = false;
	private int time;

	@Getter private int appearance;

	public L2WarehouseItem(L2ItemInstance item)
	{
		this.item = item.getItem();
		objectId = item.getObjectId();
		count = item.getCount();
		owner = item.getOwnerId();
		locationSlot = item.getLocationSlot();
		enchant = item.getEnchantLevel();
		customType1 = item.getCustomType1();
		customType2 = item.getCustomType2();
		grade = item.getItem().getItemGrade();
		isSoulEnhanced = item.isSoulEnhanced();
		ensoulEffectIds = item.getEnsoulEffectIds();
		ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();
		if (item.isAugmented())
		{
			isAugmented = true;
			augmentationId = item.getAugmentation().getId();
		}
		else
		{
			isAugmented = false;
		}
		mana = item.getMana();
		time = item.getRemainingTime();

		if (elemAtkPower > 0)
		{
			elemEnchanted = true;
		}
		for (byte i = 0; i < 6; i++)
		{
			elemDefAttr[i] = item.getElementDefAttr(i);
			if (elemDefAttr[i] > 0)
			{
				elemEnchanted = true;
			}
		}

		appearance = item.getAppearance();
	}

	/**
	 * Returns the unique objectId
	 *
	 * @return int
	 */
	@Override
	public final int getObjectId()
	{
		return objectId;
	}

	/**
	 * Returns the owner
	 *
	 * @return int
	 */
	public final int getOwnerId()
	{
		return owner;
	}

	/**
	 * Returns the LocationSlot
	 *
	 * @return int
	 */
	@Override
	public final int getLocationSlot()
	{
		return locationSlot;
	}

	/**
	 * Returns the count
	 *
	 * @return int
	 */
	@Override
	public final long getCount()
	{
		return count;
	}

	/**
	 * Returns the first type
	 *
	 * @return int
	 */
	public final int getType1()
	{
		return item.getType1();
	}

	/**
	 * Returns the second type
	 *
	 * @return int
	 */
	public final int getType2()
	{
		return item.getType2();
	}

	/**
	 * Returns the second type
	 *
	 * @return int
	 */
	public final L2ItemType getItemType()
	{
		return item.getItemType();
	}

	/**
	 * Returns the ItemId
	 *
	 * @return int
	 */
	public final int getItemId()
	{
		return item.getItemId();
	}

	/**
	 * Returns the part of body used with this item
	 *
	 * @return int
	 */
	public final int getBodyPart()
	{
		return item.getBodyPart();
	}

	/**
	 * Returns the enchant level
	 *
	 * @return int
	 */
	@Override
	public final int getEnchantLevel()
	{
		return enchant;
	}

	/**
	 * Returns the item grade
	 *
	 * @return int
	 */
	public final int getItemGrade()
	{
		return grade;
	}

	/**
	 * Returns true if it is a weapon
	 *
	 * @return boolean
	 */
	public final boolean isWeapon()
	{
		return item instanceof L2Weapon;
	}

	/**
	 * Returns true if it is an armor
	 *
	 * @return boolean
	 */
	public final boolean isArmor()
	{
		return item instanceof L2Armor;
	}

	/**
	 * Returns true if it is an EtcItem
	 *
	 * @return boolean
	 */
	public final boolean isEtcItem()
	{
		return item instanceof L2EtcItem;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	public String getItemName()
	{
		return item.getName();
	}

	@Override
	public boolean isSoulEnhanced()
	{
		return isSoulEnhanced;
	}

	@Override
	public boolean isAugmented()
	{
		return isAugmented;
	}

	@Override
	public long getAugmentationBonus()
	{
		return augmentationId;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 * @deprecated beware to use getItemName() instead because getName() is final in L2Object and could not be overridden! Allover L2Object.getName() may return null!
	 */
	@Deprecated
	public String getName()
	{
		return item.getName();
	}

	public final int getCustomType1()
	{
		return customType1;
	}

	public final int getCustomType2()
	{
		return customType2;
	}

	@Override
	public final int getMana()
	{
		return mana;
	}

	@Override
	public byte getAttackElementType()
	{
		return elemAtkType;
	}

	@Override
	public int getAttackElementPower()
	{
		return elemAtkPower;
	}

	@Override
	public int getElementDefAttr(byte i)
	{
		return elemDefAttr[i];
	}

	@Override
	public boolean isElementEnchanted()
	{
		return elemEnchanted;
	}

	@Override
	public int getRemainingTime()
	{
		return time;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return item.toString();
	}

	@Override
	public boolean isEquipped()
	{
		return false;
	}
}
