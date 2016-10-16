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
 * @author JIV
 */
public class L2ExtractableProduct
{
	private final int id;
	private final int min;
	private final int max;
	private final int chance;

	/**
	 * Create Extractable product
	 *
	 * @param id     crete item id
	 * @param min    item count max
	 * @param max    item count min
	 * @param chance chance for creating
	 */
	public L2ExtractableProduct(int id, int min, int max, double chance)
	{
		this.id = id;
		this.min = min;
		this.max = max;
		this.chance = (int) (chance * 1000);
	}

	public int getId()
	{
		return id;
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return max;
	}

	public int getChance()
	{
		return chance;
	}
}
