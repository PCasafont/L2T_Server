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

import java.util.ArrayList;
import java.util.List;

public class L2RandomMinionData
{
	private List<Integer> randomMinionIds = new ArrayList<>();
	private List<Integer> lastSpawnedMinionIds = new ArrayList<>();
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
		this.randomMinionIds = new ArrayList<>(rhs.randomMinionIds);
		this.lastSpawnedMinionIds = new ArrayList<>(rhs.lastSpawnedMinionIds);
		this.minionAmount = rhs.minionAmount;
	}

	public void addMinionId(int id)
	{
		this.randomMinionIds.add(id);
	}

	public void addLastSpawnedMinionId(int id)
	{
		this.lastSpawnedMinionIds.add(id);
	}

	public List<Integer> getMinionIds()
	{
		return this.randomMinionIds;
	}

	public List<Integer> getLastSpawnedMinionIds()
	{
		return this.lastSpawnedMinionIds;
	}

	public void setAmount(int amount)
	{
		this.minionAmount = amount;
	}

	public int getAmount()
	{
		return this.minionAmount;
	}
}
