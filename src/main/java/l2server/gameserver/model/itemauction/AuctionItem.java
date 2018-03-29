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

package l2server.gameserver.model.itemauction;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.model.L2Augmentation;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.L2Item;

/**
 * @author Forsaiken
 */
public final class AuctionItem
{
	private final int auctionItemId;
	private final int auctionLength;
	private final long auctionInitBid;

	private final int itemId;
	private final long itemCount;
	private final StatsSet itemExtra;

	public AuctionItem(final int auctionItemId, final int auctionLength, final long auctionInitBid, final int itemId, final long itemCount, final StatsSet itemExtra)
	{
		this.auctionItemId = auctionItemId;
		this.auctionLength = auctionLength;
		this.auctionInitBid = auctionInitBid;

		this.itemId = itemId;
		this.itemCount = itemCount;
		this.itemExtra = itemExtra;
	}

	public final boolean checkItemExists()
	{
		final L2Item item = ItemTable.getInstance().getTemplate(itemId);
		return item != null;
	}

	public final int getAuctionItemId()
	{
		return auctionItemId;
	}

	public final int getAuctionLength()
	{
		return auctionLength;
	}

	public final long getAuctionInitBid()
	{
		return auctionInitBid;
	}

	public final int getItemId()
	{
		return itemId;
	}

	public final long getItemCount()
	{
		return itemCount;
	}

	public final L2ItemInstance createNewItemInstance()
	{
		final L2ItemInstance item = ItemTable.getInstance().createItem("ItemAuction", itemId, itemCount, null, null);

		final int enchantLevel = itemExtra.getInteger("enchant_level", 0);
		item.setEnchantLevel(enchantLevel);

		final int augmentationId = itemExtra.getInteger("augmentation_id", 0);
		if (augmentationId != 0)
		{
			@SuppressWarnings("unused") final int augmentationSkillId =
					itemExtra.getInteger("augmentation_skill_id", 0);
			@SuppressWarnings("unused") final int augmentationSkillLevel =
					itemExtra.getInteger("augmentation_skill_lvl", 0);
			item.setAugmentation(new L2Augmentation(augmentationId));
		}

		return item;
	}
}
