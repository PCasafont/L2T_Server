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

import l2server.gameserver.model.L2ExtractableProduct;
import l2server.gameserver.model.itemcontainer.PcInventory;
import l2server.gameserver.templates.StatsSet;

/**
 * This class is dedicated to the management of EtcItem.
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:30:10 $
 */
public final class EtcItemTemplate extends ItemTemplate {
	// private final String[] skill;
	private String handler;
	private final int sharedReuseGroup;
	private EtcItemType type;
	private final boolean isBlessed;
	private L2ExtractableProduct[] extractableItems = null;

	/**
	 * Constructor for EtcItem.
	 *
	 * @param set : StatsSet designating the set of couples (key,value) for description of the Etc
	 * @see ItemTemplate constructor
	 */
	public EtcItemTemplate(StatsSet set) {
		super(set);
		type = EtcItemType.valueOf(set.getString("etcitemType", "none").toUpperCase());

		// l2j custom - EtcItemType.SHOT
		switch (getDefaultAction()) {
			case soulshot:
			case summon_soulshot:
			case summon_spiritshot:
			case spiritshot: {
				type = EtcItemType.SHOT;
				break;
			}
		}

		if (is_ex_immediate_effect()) {
			type = EtcItemType.HERB;
		}

		type1 = ItemTemplate.TYPE1_ITEM_QUESTITEM_ADENA;
		type2 = ItemTemplate.TYPE2_OTHER; // default is other

		if (isQuestItem()) {
			type2 = ItemTemplate.TYPE2_QUEST;
		} else if (getItemId() == PcInventory.ADENA_ID || getItemId() == PcInventory.ANCIENT_ADENA_ID) {
			type2 = ItemTemplate.TYPE2_MONEY;
		}

		handler = set.getString("handler", null); // ! null !
		sharedReuseGroup = set.getInteger("sharedReuseGroup", -1);
		isBlessed = set.getBool("blessed", false);
	}

	public void attach(L2ExtractableProduct product) {
		if (extractableItems == null) {
			extractableItems = new L2ExtractableProduct[]{product};
		} else {
			int len = extractableItems.length;
			L2ExtractableProduct[] tmp = new L2ExtractableProduct[len + 1];
			System.arraycopy(extractableItems, 0, tmp, 0, len);
			tmp[len] = product;
			extractableItems = tmp;
		}
	}

	/**
	 * Returns the type of Etc Item
	 *
	 * @return EtcItemType
	 */
	@Override
	public EtcItemType getItemType() {
		return type;
	}

	/**
	 * Returns if the item is consumable
	 *
	 * @return boolean
	 */
	@Override
	public final boolean isConsumable() {
		return getItemType() == EtcItemType.SHOT || getItemType() == EtcItemType.POTION; // || (type == EtcItemType.SCROLL));
	}

	/**
	 * Returns the ID of the Etc item after applying the mask.
	 *
	 * @return int : ID of the EtcItem
	 */
	@Override
	public int getItemMask() {
		return getItemType().mask();
	}

	/**
	 * Return handler name. null if no handler for item
	 *
	 * @return String
	 */
	public String getHandlerName() {
		return handler;
	}

	public int getSharedReuseGroup() {
		return sharedReuseGroup;
	}

	@Override
	public final boolean isBlessed() {
		return isBlessed;
	}

	/**
	 * @return the _extractable_items
	 */
	public L2ExtractableProduct[] getExtractableItems() {
		return extractableItems;
	}
}
