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
	private List<Integer> _randomMinionIds = new ArrayList<>();
	private List<Integer> _lastSpawnedMinionIds = new ArrayList<>();
	private int _minionAmount;

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
		_randomMinionIds = new ArrayList<>(rhs._randomMinionIds);
		_lastSpawnedMinionIds = new ArrayList<>(rhs._lastSpawnedMinionIds);
		_minionAmount = rhs._minionAmount;
	}

	public void addMinionId(int id)
	{
		_randomMinionIds.add(id);
	}

	public void addLastSpawnedMinionId(int id)
	{
		_lastSpawnedMinionIds.add(id);
	}

	public List<Integer> getMinionIds()
	{
		return _randomMinionIds;
	}

	public List<Integer> getLastSpawnedMinionIds()
	{
		return _lastSpawnedMinionIds;
	}

	public void setAmount(int amount)
	{
		_minionAmount = amount;
	}

	public int getAmount()
	{
		return _minionAmount;
	}
}
