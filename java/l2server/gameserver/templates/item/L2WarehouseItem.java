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
	private L2Item item;
	private int object;
	private long count;
	private int owner;
	private int locationSlot;
	private int enchant;
	private int grade;
	private boolean isSoulEnhanced;
	private int[] ensoulEffectIds;
	private int[] ensoulSpecialEffectIds;
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

	private int appearance;

	public L2WarehouseItem(L2ItemInstance item)
	{
		this.item = item.getItem();
		this.object = item.getObjectId();
		this.count = item.getCount();
		this.owner = item.getOwnerId();
		this.locationSlot = item.getLocationSlot();
		this.enchant = item.getEnchantLevel();
		this.customType1 = item.getCustomType1();
		this.customType2 = item.getCustomType2();
		this.grade = item.getItem().getItemGrade();
		this.isSoulEnhanced = item.isSoulEnhanced();
		this.ensoulEffectIds = item.getEnsoulEffectIds();
		this.ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();
		if (item.isAugmented())
		{
			this.isAugmented = true;
			this.augmentationId = item.getAugmentation().getId();
		}
		else
		{
			this.isAugmented = false;
		}
		this.mana = item.getMana();
		this.time = item.getRemainingTime();

		if (this.elemAtkPower > 0)
		{
			this.elemEnchanted = true;
		}
		for (byte i = 0; i < 6; i++)
		{
			this.elemDefAttr[i] = item.getElementDefAttr(i);
			if (this.elemDefAttr[i] > 0)
			{
				this.elemEnchanted = true;
			}
		}

		this.appearance = item.getAppearance();
	}

	/**
	 * Returns the item.
	 *
	 * @return L2Item
	 */
	@Override
	public L2Item getItem()
	{
		return this.item;
	}

	/**
	 * Returns the unique objectId
	 *
	 * @return int
	 */
	@Override
	public final int getObjectId()
	{
		return this.object;
	}

	/**
	 * Returns the owner
	 *
	 * @return int
	 */
	public final int getOwnerId()
	{
		return this.owner;
	}

	/**
	 * Returns the LocationSlot
	 *
	 * @return int
	 */
	@Override
	public final int getLocationSlot()
	{
		return this.locationSlot;
	}

	/**
	 * Returns the count
	 *
	 * @return int
	 */
	@Override
	public final long getCount()
	{
		return this.count;
	}

	/**
	 * Returns the first type
	 *
	 * @return int
	 */
	public final int getType1()
	{
		return this.item.getType1();
	}

	/**
	 * Returns the second type
	 *
	 * @return int
	 */
	public final int getType2()
	{
		return this.item.getType2();
	}

	/**
	 * Returns the second type
	 *
	 * @return int
	 */
	public final L2ItemType getItemType()
	{
		return this.item.getItemType();
	}

	/**
	 * Returns the ItemId
	 *
	 * @return int
	 */
	public final int getItemId()
	{
		return this.item.getItemId();
	}

	/**
	 * Returns the part of body used with this item
	 *
	 * @return int
	 */
	public final int getBodyPart()
	{
		return this.item.getBodyPart();
	}

	/**
	 * Returns the enchant level
	 *
	 * @return int
	 */
	@Override
	public final int getEnchantLevel()
	{
		return this.enchant;
	}

	/**
	 * Returns the item grade
	 *
	 * @return int
	 */
	public final int getItemGrade()
	{
		return this.grade;
	}

	/**
	 * Returns true if it is a weapon
	 *
	 * @return boolean
	 */
	public final boolean isWeapon()
	{
		return this.item instanceof L2Weapon;
	}

	/**
	 * Returns true if it is an armor
	 *
	 * @return boolean
	 */
	public final boolean isArmor()
	{
		return this.item instanceof L2Armor;
	}

	/**
	 * Returns true if it is an EtcItem
	 *
	 * @return boolean
	 */
	public final boolean isEtcItem()
	{
		return this.item instanceof L2EtcItem;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	public String getItemName()
	{
		return this.item.getName();
	}

	@Override
	public boolean isSoulEnhanced()
	{
		return this.isSoulEnhanced;
	}

	@Override
	public int[] getEnsoulEffectIds()
	{
		return this.ensoulEffectIds;
	}

	@Override
	public int[] getEnsoulSpecialEffectIds()
	{
		return this.ensoulSpecialEffectIds;
	}

	@Override
	public boolean isAugmented()
	{
		return this.isAugmented;
	}

	@Override
	public long getAugmentationBonus()
	{
		return this.augmentationId;
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
		return this.item.getName();
	}

	public final int getCustomType1()
	{
		return this.customType1;
	}

	public final int getCustomType2()
	{
		return this.customType2;
	}

	@Override
	public final int getMana()
	{
		return this.mana;
	}

	@Override
	public byte getAttackElementType()
	{
		return this.elemAtkType;
	}

	@Override
	public int getAttackElementPower()
	{
		return this.elemAtkPower;
	}

	@Override
	public int getElementDefAttr(byte i)
	{
		return this.elemDefAttr[i];
	}

	@Override
	public boolean isElementEnchanted()
	{
		return this.elemEnchanted;
	}

	@Override
	public int getRemainingTime()
	{
		return this.time;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return this.item.toString();
	}

	@Override
	public boolean isEquipped()
	{
		return false;
	}

	@Override
	public int getAppearance()
	{
		return this.appearance;
	}
}
