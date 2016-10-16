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

package l2server.gameserver.model;

import l2server.gameserver.network.serverpackets.L2ItemListPacket.ItemInstanceInfo;
import l2server.gameserver.templates.item.L2Item;

/**
 * Get all information from L2ItemInstance to generate ItemInfo.<BR><BR>
 */
public class ItemInfo implements ItemInstanceInfo
{
	/**
	 * Identifier of the L2ItemInstance
	 */
	private int objectId;

	/**
	 * The L2Item template of the L2ItemInstance
	 */
	private L2Item item;

	/**
	 * The level of enchant on the L2ItemInstance
	 */
	private int enchant;

	/**
	 * The item's ensoul effect data
	 */
	private boolean isSoulEnhanced;
	private int[] ensoulEffectIds;
	private int[] ensoulSpecialEffectIds;

	/**
	 * The augmentation of the item
	 */
	private long augmentation;

	/**
	 * The quantity of L2ItemInstance
	 */
	private long count;

	/**
	 * The price of the L2ItemInstance
	 */
	private long price;

	/**
	 * The custom L2ItemInstance types (used loto, race tickets)
	 */
	private int type1;
	private int type2;

	/**
	 * If True the L2ItemInstance is equipped
	 */
	private boolean equipped;

	/**
	 * The action to do clientside (1=ADD, 2=MODIFY, 3=REMOVE)
	 */
	private int change;

	/**
	 * The mana of this item
	 */
	private int mana;
	private int time;

	private int location;

	boolean elemEnchanted = false;
	private byte elemAtkType = -2;
	private int elemAtkPower = 0;
	private int[] elemDefAttr = {0, 0, 0, 0, 0, 0};

	private int appearance;

	/**
	 * Get all information from L2ItemInstance to generate ItemInfo.<BR><BR>
	 */
	public ItemInfo(L2ItemInstance item)
	{
		if (item == null)
		{
			return;
		}

		// Get the Identifier of the L2ItemInstance
		this.objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		this.item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		this.enchant = item.getEnchantLevel();

		// Get the ensoul effect of the L2ItemInstance
		this.isSoulEnhanced = item.isSoulEnhanced();
		this.ensoulEffectIds = item.getEnsoulEffectIds();
		this.ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();

		// Get the augmentation boni
		if (item.isAugmented())
		{
			this.augmentation = item.getAugmentation().getId();
		}
		else
		{
			this.augmentation = 0;
		}

		// Get the quantity of the L2ItemInstance
		this.count = item.getCount();

		// Get custom item types (used loto, race tickets)
		this.type1 = item.getCustomType1();
		this.type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		this.equipped = item.isEquipped();

		// Get the action to do clientside
		switch (item.getLastChange())
		{
			case L2ItemInstance.ADDED:
			{
				this.change = 1;
				break;
			}
			case L2ItemInstance.MODIFIED:
			{
				this.change = 2;
				break;
			}
			case L2ItemInstance.REMOVED:
			{
				this.change = 3;
				break;
			}
		}

		// Get shadow item mana
		this.mana = item.getMana();
		this.time = item.getRemainingTime();
		this.location = item.getLocationSlot();

		this.elemAtkType = item.getAttackElementType();
		this.elemAtkPower = item.getAttackElementPower();
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

	public ItemInfo(L2ItemInstance item, int change)
	{
		if (item == null)
		{
			return;
		}

		// Get the Identifier of the L2ItemInstance
		this.objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		this.item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		this.enchant = item.getEnchantLevel();

		// Get the ensoul effect of the L2ItemInstance
		this.isSoulEnhanced = item.isSoulEnhanced();
		this.ensoulEffectIds = item.getEnsoulEffectIds();
		this.ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();

		// Get the augmentation boni
		if (item.isAugmented())
		{
			this.augmentation = item.getAugmentation().getId();
		}
		else
		{
			this.augmentation = 0;
		}

		// Get the quantity of the L2ItemInstance
		this.count = item.getCount();

		// Get custom item types (used loto, race tickets)
		this.type1 = item.getCustomType1();
		this.type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		this.equipped = item.isEquipped();

		// Get the action to do clientside
		this.change = change;

		// Get shadow item mana
		this.mana = item.getMana();
		this.time = item.getRemainingTime();

		this.location = item.getLocationSlot();

		this.elemAtkType = item.getAttackElementType();
		this.elemAtkPower = item.getAttackElementPower();
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

	@Override
	public int getObjectId()
	{
		return this.objectId;
	}

	@Override
	public L2Item getItem()
	{
		return this.item;
	}

	@Override
	public int getEnchantLevel()
	{
		return this.enchant;
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
		return this.augmentation != 0;
	}

	@Override
	public long getAugmentationBonus()
	{
		return this.augmentation;
	}

	@Override
	public long getCount()
	{
		return this.count;
	}

	public long getPrice()
	{
		return this.price;
	}

	public int getCustomType1()
	{
		return this.type1;
	}

	public int getCustomType2()
	{
		return this.type2;
	}

	@Override
	public boolean isEquipped()
	{
		return this.equipped;
	}

	public int getChange()
	{
		return this.change;
	}

	@Override
	public int getMana()
	{
		return this.mana;
	}

	@Override
	public int getRemainingTime()
	{
		return this.time;
	}

	@Override
	public int getLocationSlot()
	{
		return this.location;
	}

	@Override
	public boolean isElementEnchanted()
	{
		return this.elemEnchanted;
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
	public int getAppearance()
	{
		return this.appearance;
	}
}
