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

package l2server.gameserver.stats.skills;

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.templates.StatsSet;

/**
 * @author JIV
 */
public class L2SkillSweeper extends L2Skill
{
	private boolean _absorbHp;
	private int _absorbAbs;

	/**
	 * @param set
	 */
	public L2SkillSweeper(StatsSet set)
	{
		super(set);
		_absorbHp = set.getBool("absorbHp", true);
		_absorbAbs = set.getInteger("absorbAbs", -1);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		// not used
	}

	public boolean isAbsorbHp()
	{
		return _absorbHp;
	}

	public int getAbsorbAbs()
	{
		return _absorbAbs;
	}
}
