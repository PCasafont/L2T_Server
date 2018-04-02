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

import l2server.gameserver.model.WorldObject;
import l2server.gameserver.model.Skill;
import l2server.gameserver.model.actor.Creature;
import l2server.gameserver.templates.StatsSet;

/**
 * @author JIV
 */
public class SkillSweeper extends Skill {
	private boolean absorbHp;
	private int absorbAbs;

	/**
	 * @param set
	 */
	public SkillSweeper(StatsSet set) {
		super(set);
		absorbHp = set.getBool("absorbHp", true);
		absorbAbs = set.getInteger("absorbAbs", -1);
	}

	@Override
	public void useSkill(Creature caster, WorldObject[] targets) {
		// not used
	}

	public boolean isAbsorbHp() {
		return absorbHp;
	}

	public int getAbsorbAbs() {
		return absorbAbs;
	}
}
