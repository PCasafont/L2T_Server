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
public final class L2EtcItem extends L2Item
{
	// private final String[] skill;
	private String handler;
	private final int sharedReuseGroup;
	private L2EtcItemType type;
	private final boolean isBlessed;
	private L2ExtractableProduct[] extractableItems = null;

	/**
	 * Constructor for EtcItem.
	 *
	 * @param set : StatsSet designating the set of couples (key,value) for description of the Etc
	 * @see L2Item constructor
	 */
	public L2EtcItem(StatsSet set)
	{
		super(set);
		type = L2EtcItemType.valueOf(set.getString("etcitemType", "none").toUpperCase());

		// l2j custom - L2EtcItemType.SHOT
		switch (getDefaultAction())
		{
			case soulshot:
			case summon_soulshot:
			case summon_spiritshot:
			case spiritshot:
			{
				type = L2EtcItemType.SHOT;
				break;
			}
		}

		if (is_ex_immediate_effect())
		{
			type = L2EtcItemType.HERB;
		}

		type1 = L2Item.TYPE1_ITEM_QUESTITEM_ADENA;
		type2 = L2Item.TYPE2_OTHER; // default is other

		if (isQuestItem())
		{
			type2 = L2Item.TYPE2_QUEST;
		}
		else if (getItemId() == PcInventory.ADENA_ID || getItemId() == PcInventory.ANCIENT_ADENA_ID)
		{
			type2 = L2Item.TYPE2_MONEY;
		}

		handler = set.getString("handler", null); // ! null !
		sharedReuseGroup = set.getInteger("sharedReuseGroup", -1);
		isBlessed = set.getBool("blessed", false);
	}

	public void attach(L2ExtractableProduct product)
	{
		if (extractableItems == null)
		{
			extractableItems = new L2ExtractableProduct[]{product};
		}
		else
		{
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
	 * @return L2EtcItemType
	 */
	@Override
	public L2EtcItemType getItemType()
	{
		return type;
	}

	/**
	 * Returns if the item is consumable
	 *
	 * @return boolean
	 */
	@Override
	public final boolean isConsumable()
	{
		return getItemType() == L2EtcItemType.SHOT ||
				getItemType() == L2EtcItemType.POTION; // || (type == L2EtcItemType.SCROLL));
	}

	/**
	 * Returns the ID of the Etc item after applying the mask.
	 *
	 * @return int : ID of the EtcItem
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}

	/**
	 * Return handler name. null if no handler for item
	 *
	 * @return String
	 */
	public String getHandlerName()
	{
		return handler;
	}

	/**
	 * @return
	 */
	public int getSharedReuseGroup()
	{
		return sharedReuseGroup;
	}

	/**
	 * @return
	 */
	@Override
	public final boolean isBlessed()
	{
		return isBlessed;
	}

	/**
	 * @return the _extractable_items
	 */
	public L2ExtractableProduct[] getExtractableItems()
	{
		return extractableItems;
	}
}
