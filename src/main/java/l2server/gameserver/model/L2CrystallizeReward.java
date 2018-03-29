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
 * @author Pere
 */
public class L2CrystallizeReward
{
	private final int itemId;
	private final int count;
	private final double chance;

	public L2CrystallizeReward(int itemId, int count, double chance)
	{
		this.itemId = itemId;
		this.count = count;
		this.chance = chance;
	}

	public int getItemId()
	{
		return itemId;
	}

	public int getCount()
	{
		return count;
	}

	public double getChance()
	{
		return chance;
	}
}
