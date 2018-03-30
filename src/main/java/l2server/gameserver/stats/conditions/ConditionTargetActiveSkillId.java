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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.L2Skill;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionTargetActiveSkillId.
 */
public class ConditionTargetActiveSkillId extends Condition {

	private final int skillId;
	private final int skillLevel;

	/**
	 * Instantiates a new condition target active skill id.
	 *
	 * @param skillId the skill id
	 */
	public ConditionTargetActiveSkillId(int skillId) {
		this.skillId = skillId;
		skillLevel = -1;
	}

	/**
	 * Instantiates a new condition target active skill id.
	 *
	 * @param skillId    the skill id
	 * @param skillLevel the skill level
	 */
	public ConditionTargetActiveSkillId(int skillId, int skillLevel) {
		this.skillId = skillId;
		this.skillLevel = skillLevel;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		for (L2Skill sk : env.target.getAllSkills()) {
			if (sk != null) {
				if (sk.getId() == skillId) {
					if (skillLevel == -1 || skillLevel <= sk.getLevel()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
