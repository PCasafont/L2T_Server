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

package l2server.gameserver.templates;

import lombok.Getter;
/**
 * This class represents a Newbie Helper Buff
 * <p>
 * Author: Ayor
 */

public class L2HelperBuff
{
	/**
	 * Min level that the player must achieve to obtain this buff from Newbie Helper
	 */
	@Getter private int lowerLevel;

	/**
	 * Max level that the player mustn't exceed if it want to obtain this buff from Newbie Helper
	 */
	@Getter private int upperLevel;

	/**
	 * Identifier of the skill (buff) that the Newbie Helper must cast
	 */
	@Getter private int skillID;

	/**
	 * Level of the skill (buff) that the Newbie Helper must cast
	 */
	@Getter private int skillLevel;

	/**
	 * If True only Magus class will obtain this Buff <BR>
	 * If False only Fighter class will obtain this Buff
	 */
	private boolean isMagicClass;

	@Getter private boolean forSummon = false;

	/**
	 * Constructor of L2HelperBuff.<BR><BR>
	 */
	public L2HelperBuff(StatsSet set)
	{
		lowerLevel = set.getInteger("lowerLevel");
		upperLevel = set.getInteger("upperLevel");
		skillID = set.getInteger("skillID");
		skillLevel = set.getInteger("skillLevel");
		if ("true".equals(set.getString("forSummon")))
		{
			forSummon = true;
		}

		isMagicClass = !"false".equals(set.getString("isMagicClass"));
	}

	/**
	 * Returns if this Buff can be cast on a fighter or a mystic
	 *
	 * @return boolean : False if it's a fighter class Buff
	 */
	public boolean isMagicClassBuff()
	{
		return isMagicClass;
	}

}
