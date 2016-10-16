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
 * @author -Nemesiss-, Zoey76
 */
public class L2ExtractableProductItem
{
	private final int[] id;
	private final int[] ammount;
	private final double chance;

	public L2ExtractableProductItem(int[] id, int[] ammount, double chance)
	{
		this.id = id;
		this.ammount = ammount;
		this.chance = chance;
	}

	public int[] getId()
	{
		return id;
	}

	public int[] getAmmount()
	{
		return ammount;
	}

	public double getChance()
	{
		return chance;
	}
}
