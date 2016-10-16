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

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class L2RandomMinionData
{
	private List<Integer> randomMinionIds = new ArrayList<>();
	@Getter private List<Integer> lastSpawnedMinionIds = new ArrayList<>();
	private int minionAmount;

	public L2RandomMinionData()
	{
	}

	/**
	 * Copy constructor
	 *
	 * @param rhs The minion data to copy from
	 */
	public L2RandomMinionData(L2RandomMinionData rhs)
	{
		randomMinionIds = new ArrayList<>(rhs.randomMinionIds);
		lastSpawnedMinionIds = new ArrayList<>(rhs.lastSpawnedMinionIds);
		minionAmount = rhs.minionAmount;
	}

	public void addMinionId(int id)
	{
		randomMinionIds.add(id);
	}

	public void addLastSpawnedMinionId(int id)
	{
		lastSpawnedMinionIds.add(id);
	}

	public List<Integer> getMinionIds()
	{
		return randomMinionIds;
	}

	public void setAmount(int amount)
	{
		minionAmount = amount;
	}

	public int getAmount()
	{
		return minionAmount;
	}
}
