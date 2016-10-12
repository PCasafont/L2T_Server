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
 * The Class ConditionPlayerActiveSkillId.
 *
 * @author DrHouse
 */
public class ConditionPlayerActiveSkillId extends Condition
{
	private final int _skillId;
	private final int _skillLevel;

	/**
	 * Instantiates a new condition player active skill id.
	 *
	 * @param skillId the skill id
	 */
	public ConditionPlayerActiveSkillId(int skillId)
	{
		_skillId = skillId;
		_skillLevel = -1;
	}

	/**
	 * Instantiates a new condition player active skill id.
	 *
	 * @param skillId    the skill id
	 * @param skillLevel the skill level
	 */
	public ConditionPlayerActiveSkillId(int skillId, int skillLevel)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		for (L2Skill sk : env.player.getAllSkills())
		{
			if (sk != null)
			{
				if (sk.getId() == _skillId)
				{
					if (_skillLevel == -1 || _skillLevel <= sk.getLevel())
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}
