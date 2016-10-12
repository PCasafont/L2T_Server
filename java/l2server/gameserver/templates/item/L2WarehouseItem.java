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
	private L2Item _item;
	private int _object;
	private long _count;
	private int _owner;
	private int _locationSlot;
	private int _enchant;
	private int _grade;
	private boolean _isSoulEnhanced;
	private int[] _ensoulEffectIds;
	private int[] _ensoulSpecialEffectIds;
	private boolean _isAugmented;
	private long _augmentationId;
	private int _customType1;
	private int _customType2;
	private int _mana;

	private byte _elemAtkType = -2;
	private int _elemAtkPower = 0;
	private int[] _elemDefAttr = {0, 0, 0, 0, 0, 0};
	private boolean _elemEnchanted = false;
	private int _time;

	private int _appearance;

	public L2WarehouseItem(L2ItemInstance item)
	{
		_item = item.getItem();
		_object = item.getObjectId();
		_count = item.getCount();
		_owner = item.getOwnerId();
		_locationSlot = item.getLocationSlot();
		_enchant = item.getEnchantLevel();
		_customType1 = item.getCustomType1();
		_customType2 = item.getCustomType2();
		_grade = item.getItem().getItemGrade();
		_isSoulEnhanced = item.isSoulEnhanced();
		_ensoulEffectIds = item.getEnsoulEffectIds();
		_ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();
		if (item.isAugmented())
		{
			_isAugmented = true;
			_augmentationId = item.getAugmentation().getId();
		}
		else
		{
			_isAugmented = false;
		}
		_mana = item.getMana();
		_time = item.getRemainingTime();

		if (_elemAtkPower > 0)
		{
			_elemEnchanted = true;
		}
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
			if (_elemDefAttr[i] > 0)
			{
				_elemEnchanted = true;
			}
		}

		_appearance = item.getAppearance();
	}

	/**
	 * Returns the item.
	 *
	 * @return L2Item
	 */
	@Override
	public L2Item getItem()
	{
		return _item;
	}

	/**
	 * Returns the unique objectId
	 *
	 * @return int
	 */
	@Override
	public final int getObjectId()
	{
		return _object;
	}

	/**
	 * Returns the owner
	 *
	 * @return int
	 */
	public final int getOwnerId()
	{
		return _owner;
	}

	/**
	 * Returns the LocationSlot
	 *
	 * @return int
	 */
	@Override
	public final int getLocationSlot()
	{
		return _locationSlot;
	}

	/**
	 * Returns the count
	 *
	 * @return int
	 */
	@Override
	public final long getCount()
	{
		return _count;
	}

	/**
	 * Returns the first type
	 *
	 * @return int
	 */
	public final int getType1()
	{
		return _item.getType1();
	}

	/**
	 * Returns the second type
	 *
	 * @return int
	 */
	public final int getType2()
	{
		return _item.getType2();
	}

	/**
	 * Returns the second type
	 *
	 * @return int
	 */
	public final L2ItemType getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * Returns the ItemId
	 *
	 * @return int
	 */
	public final int getItemId()
	{
		return _item.getItemId();
	}

	/**
	 * Returns the part of body used with this item
	 *
	 * @return int
	 */
	public final int getBodyPart()
	{
		return _item.getBodyPart();
	}

	/**
	 * Returns the enchant level
	 *
	 * @return int
	 */
	@Override
	public final int getEnchantLevel()
	{
		return _enchant;
	}

	/**
	 * Returns the item grade
	 *
	 * @return int
	 */
	public final int getItemGrade()
	{
		return _grade;
	}

	/**
	 * Returns true if it is a weapon
	 *
	 * @return boolean
	 */
	public final boolean isWeapon()
	{
		return _item instanceof L2Weapon;
	}

	/**
	 * Returns true if it is an armor
	 *
	 * @return boolean
	 */
	public final boolean isArmor()
	{
		return _item instanceof L2Armor;
	}

	/**
	 * Returns true if it is an EtcItem
	 *
	 * @return boolean
	 */
	public final boolean isEtcItem()
	{
		return _item instanceof L2EtcItem;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	public String getItemName()
	{
		return _item.getName();
	}

	@Override
	public boolean isSoulEnhanced()
	{
		return _isSoulEnhanced;
	}

	@Override
	public int[] getEnsoulEffectIds()
	{
		return _ensoulEffectIds;
	}

	@Override
	public int[] getEnsoulSpecialEffectIds()
	{
		return _ensoulSpecialEffectIds;
	}

	@Override
	public boolean isAugmented()
	{
		return _isAugmented;
	}

	@Override
	public long getAugmentationBonus()
	{
		return _augmentationId;
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
		return _item.getName();
	}

	public final int getCustomType1()
	{
		return _customType1;
	}

	public final int getCustomType2()
	{
		return _customType2;
	}

	@Override
	public final int getMana()
	{
		return _mana;
	}

	@Override
	public byte getAttackElementType()
	{
		return _elemAtkType;
	}

	@Override
	public int getAttackElementPower()
	{
		return _elemAtkPower;
	}

	@Override
	public int getElementDefAttr(byte i)
	{
		return _elemDefAttr[i];
	}

	@Override
	public boolean isElementEnchanted()
	{
		return _elemEnchanted;
	}

	@Override
	public int getRemainingTime()
	{
		return _time;
	}

	/**
	 * Returns the name of the item
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _item.toString();
	}

	@Override
	public boolean isEquipped()
	{
		return false;
	}

	@Override
	public int getAppearance()
	{
		return _appearance;
	}
}
