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
/**
 * This class ...
 *
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public final class L2PledgeSkillLearn
{
	// these two build the primary key
	@Getter private final int id;
	@Getter private final int level;

	@Getter private final int repCost;
	private final int baseLvl;

	public L2PledgeSkillLearn(int id, int lvl, int baseLvl, int cost)
	{
		this.id = id;
		level = lvl;
		this.baseLvl = baseLvl;
		repCost = cost;
	}

	/**
	 * @return Returns the minLevel.
	 */
	public int getBaseLevel()
	{
		return baseLvl;
	}

}
