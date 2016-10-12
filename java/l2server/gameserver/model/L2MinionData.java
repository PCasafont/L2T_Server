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

import l2server.util.Rnd;

/**
 * This class defines the spawn data of a Minion type
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 * <p>
 * <B><U> Data</U> :</B><BR><BR>
 * <li>_minionId : The Identifier of the L2Minion to spawn </li>
 * <li>_minionAmount :  The number of this Minion Type to spawn </li><BR><BR>
 */
public class L2MinionData
{

	/**
	 * The Identifier of the L2Minion
	 */
	private int _minionId;

	/**
	 * The number of this Minion Type to spawn
	 */
	private int _minionAmount;
	private int _minionAmountMin;
	private int _minionAmountMax;
	private int _respawnTime;
	private int _maxRespawn;

	/**
	 * Set the Identifier of the Minion to spawn.<BR><BR>
	 */
	public void setMinionId(int id)
	{
		_minionId = id;
	}

	/**
	 * Return the Identifier of the Minion to spawn.<BR><BR>
	 */
	public int getMinionId()
	{
		return _minionId;
	}

	/**
	 * Set the minimum of minions to amount.<BR><BR>
	 *
	 * @param amountMin The minimum quantity of this Minion type to spawn
	 */
	public void setAmountMin(int amountMin)
	{
		_minionAmountMin = amountMin;
	}

	/**
	 * Set the maximum of minions to amount.<BR><BR>
	 *
	 * @param amountMax The maximum quantity of this Minion type to spawn
	 */
	public void setAmountMax(int amountMax)
	{
		_minionAmountMax = amountMax;
	}

	/**
	 * Set the amount of this Minion type to spawn.<BR><BR>
	 *
	 * @param amount The quantity of this Minion type to spawn
	 */
	public void setAmount(int amount)
	{
		_minionAmount = amount;
	}

	public int getAmountMin()
	{
		return _minionAmountMin;
	}

	public int getAmountMax()
	{
		return _minionAmountMax;
	}

	/**
	 * Return the amount of this Minion type to spawn.<BR><BR>
	 */
	public int getAmount()
	{
		if (_minionAmountMax > _minionAmountMin)
		{
			_minionAmount = Rnd.get(_minionAmountMin, _minionAmountMax);
			return _minionAmount;
		}
		else
		{
			return _minionAmountMin;
		}
	}

	public int getRespawnTime()
	{
		if (_respawnTime > 0 && _respawnTime < 15)
		{
			return 15;
		}

		return _respawnTime;
	}

	public void setRespawnTime(final int respawnTime)
	{
		_respawnTime = respawnTime;
	}

	public int getMaxRespawn()
	{
		//if (_maxRespawn > 5)
		//	return 5;

		return _maxRespawn;
	}

	public void setMaxRespawn(final int maxRespawn)
	{
		_maxRespawn = maxRespawn;
	}
}
