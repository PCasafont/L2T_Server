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
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2PledgeSkillLearn
{
	// these two build the primary key
	private final int _id;
	private final int _level;

	private final int _repCost;
	private final int _baseLvl;

	public L2PledgeSkillLearn(int id, int lvl, int baseLvl, int cost)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_repCost = cost;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return _baseLvl;
	}

	/**
	 * @return Returns the spCost.
	 */
	public int getRepCost()
	{
		return _repCost;
	}
}
