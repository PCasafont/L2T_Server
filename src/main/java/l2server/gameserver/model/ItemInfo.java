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
public class ItemInfo implements ItemInstanceInfo {
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
	public ItemInfo(L2ItemInstance item) {
		if (item == null) {
			return;
		}

		// Get the Identifier of the L2ItemInstance
		objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		this.item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		enchant = item.getEnchantLevel();

		// Get the ensoul effect of the L2ItemInstance
		isSoulEnhanced = item.isSoulEnhanced();
		ensoulEffectIds = item.getEnsoulEffectIds();
		ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();

		// Get the augmentation boni
		if (item.isAugmented()) {
			augmentation = item.getAugmentation().getId();
		} else {
			augmentation = 0;
		}

		// Get the quantity of the L2ItemInstance
		count = item.getCount();

		// Get custom item types (used loto, race tickets)
		type1 = item.getCustomType1();
		type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		equipped = item.isEquipped();

		// Get the action to do clientside
		switch (item.getLastChange()) {
			case L2ItemInstance.ADDED: {
				change = 1;
				break;
			}
			case L2ItemInstance.MODIFIED: {
				change = 2;
				break;
			}
			case L2ItemInstance.REMOVED: {
				change = 3;
				break;
			}
		}

		// Get shadow item mana
		mana = item.getMana();
		time = item.getRemainingTime();
		location = item.getLocationSlot();

		elemAtkType = item.getAttackElementType();
		elemAtkPower = item.getAttackElementPower();
		if (elemAtkPower > 0) {
			elemEnchanted = true;
		}
		for (byte i = 0; i < 6; i++) {
			elemDefAttr[i] = item.getElementDefAttr(i);
			if (elemDefAttr[i] > 0) {
				elemEnchanted = true;
			}
		}

		appearance = item.getAppearance();
	}

	public ItemInfo(L2ItemInstance item, int change) {
		if (item == null) {
			return;
		}

		// Get the Identifier of the L2ItemInstance
		objectId = item.getObjectId();

		// Get the L2Item of the L2ItemInstance
		this.item = item.getItem();

		// Get the enchant level of the L2ItemInstance
		enchant = item.getEnchantLevel();

		// Get the ensoul effect of the L2ItemInstance
		isSoulEnhanced = item.isSoulEnhanced();
		ensoulEffectIds = item.getEnsoulEffectIds();
		ensoulSpecialEffectIds = item.getEnsoulSpecialEffectIds();

		// Get the augmentation boni
		if (item.isAugmented()) {
			augmentation = item.getAugmentation().getId();
		} else {
			augmentation = 0;
		}

		// Get the quantity of the L2ItemInstance
		count = item.getCount();

		// Get custom item types (used loto, race tickets)
		type1 = item.getCustomType1();
		type2 = item.getCustomType2();

		// Verify if the L2ItemInstance is equipped
		equipped = item.isEquipped();

		// Get the action to do clientside
		this.change = change;

		// Get shadow item mana
		mana = item.getMana();
		time = item.getRemainingTime();

		location = item.getLocationSlot();

		elemAtkType = item.getAttackElementType();
		elemAtkPower = item.getAttackElementPower();
		if (elemAtkPower > 0) {
			elemEnchanted = true;
		}
		for (byte i = 0; i < 6; i++) {
			elemDefAttr[i] = item.getElementDefAttr(i);
			if (elemDefAttr[i] > 0) {
				elemEnchanted = true;
			}
		}

		appearance = item.getAppearance();
	}

	@Override
	public int getObjectId() {
		return objectId;
	}

	@Override
	public L2Item getItem() {
		return item;
	}

	@Override
	public int getEnchantLevel() {
		return enchant;
	}

	@Override
	public boolean isSoulEnhanced() {
		return isSoulEnhanced;
	}

	@Override
	public int[] getEnsoulEffectIds() {
		return ensoulEffectIds;
	}

	@Override
	public int[] getEnsoulSpecialEffectIds() {
		return ensoulSpecialEffectIds;
	}

	@Override
	public boolean isAugmented() {
		return augmentation != 0;
	}

	@Override
	public long getAugmentationBonus() {
		return augmentation;
	}

	@Override
	public long getCount() {
		return count;
	}

	public long getPrice() {
		return price;
	}

	public int getCustomType1() {
		return type1;
	}

	public int getCustomType2() {
		return type2;
	}

	@Override
	public boolean isEquipped() {
		return equipped;
	}

	public int getChange() {
		return change;
	}

	@Override
	public int getMana() {
		return mana;
	}

	@Override
	public int getRemainingTime() {
		return time;
	}

	@Override
	public int getLocationSlot() {
		return location;
	}

	@Override
	public boolean isElementEnchanted() {
		return elemEnchanted;
	}

	@Override
	public byte getAttackElementType() {
		return elemAtkType;
	}

	@Override
	public int getAttackElementPower() {
		return elemAtkPower;
	}

	@Override
	public int getElementDefAttr(byte i) {
		return elemDefAttr[i];
	}

	@Override
	public int getAppearance() {
		return appearance;
	}
}
