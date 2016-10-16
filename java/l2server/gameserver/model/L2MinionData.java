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
import lombok.Getter;

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
	@Getter private int minionId;

	/**
	 * The number of this Minion Type to spawn
	 */
	private int minionAmount;
	private int minionAmountMin;
	private int minionAmountMax;
	private int respawnTime;
	private int maxRespawn;

	/**
	 * Set the Identifier of the Minion to spawn.<BR><BR>
	 */
	public void setMinionId(int id)
	{
		minionId = id;
	}

	/**
	 * Set the minimum of minions to amount.<BR><BR>
	 *
	 * @param amountMin The minimum quantity of this Minion type to spawn
	 */
	public void setAmountMin(int amountMin)
	{
		minionAmountMin = amountMin;
	}

	/**
	 * Set the maximum of minions to amount.<BR><BR>
	 *
	 * @param amountMax The maximum quantity of this Minion type to spawn
	 */
	public void setAmountMax(int amountMax)
	{
		minionAmountMax = amountMax;
	}

	/**
	 * Set the amount of this Minion type to spawn.<BR><BR>
	 *
	 * @param amount The quantity of this Minion type to spawn
	 */
	public void setAmount(int amount)
	{
		minionAmount = amount;
	}

	public int getAmountMin()
	{
		return minionAmountMin;
	}

	public int getAmountMax()
	{
		return minionAmountMax;
	}

	/**
	 * Return the amount of this Minion type to spawn.<BR><BR>
	 */
	public int getAmount()
	{
		if (minionAmountMax > minionAmountMin)
		{
			minionAmount = Rnd.get(minionAmountMin, minionAmountMax);
			return minionAmount;
		}
		else
		{
			return minionAmountMin;
		}
	}

	public int getRespawnTime()
	{
		if (respawnTime > 0 && respawnTime < 15)
		{
			return 15;
		}

		return respawnTime;
	}

	public void setRespawnTime(final int respawnTime)
	{
		this.respawnTime = respawnTime;
	}

	public int getMaxRespawn()
	{
		//if (this.maxRespawn > 5)
		//	return 5;

		return maxRespawn;
	}

	public void setMaxRespawn(final int maxRespawn)
	{
		this.maxRespawn = maxRespawn;
	}
}
