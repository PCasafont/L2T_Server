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
 *
 */
public class ItemRequest
{
	int objectId;
	int itemId;
	long count;
	long price;

	public ItemRequest(int objectId, long count, long price)
	{
		this.objectId = objectId;
		this.count = count;
		this.price = price;
	}

	public ItemRequest(int objectId, int itemId, long count, long price)
	{
		this.objectId = objectId;
		this.itemId = itemId;
		this.count = count;
		this.price = price;
	}

	public int getObjectId()
	{
		return this.objectId;
	}

	public int getItemId()
	{
		return this.itemId;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

	public long getCount()
	{
		return this.count;
	}

	public long getPrice()
	{
		return this.price;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.objectId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		ItemRequest other = (ItemRequest) obj;
		return this.objectId == other.objectId;
	}
}
