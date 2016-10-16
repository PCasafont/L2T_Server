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

/**
 * This class ...
 *
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public final class TradeItem
{
	private int objectId;
	private int itemId;
	private long price;
	private long storePrice;
	private long count;
	private int enchantLevel;

	public TradeItem()
	{
	}

	public void setObjectId(int id)
	{
		objectId = id;
	}

	public int getObjectId()
	{
		return objectId;
	}

	public void setItemId(int id)
	{
		itemId = id;
	}

	public int getItemId()
	{
		return itemId;
	}

	public void setOwnersPrice(long price)
	{
		this.price = price;
	}

	public long getOwnersPrice()
	{
		return price;
	}

	public void setstorePrice(long price)
	{
		storePrice = price;
	}

	public long getStorePrice()
	{
		return storePrice;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

	public long getCount()
	{
		return count;
	}

	public void setEnchantLevel(int enchant)
	{
		enchantLevel = enchant;
	}

	public int getEnchantLevel()
	{
		return enchantLevel;
	}
}
