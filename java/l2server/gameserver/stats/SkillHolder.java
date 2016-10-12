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

package l2server.gameserver.stats;

import l2server.gameserver.datatables.SkillTable;
import l2server.gameserver.model.L2Skill;

/**
 * @author BiggBoss
 *         Simple class for storing skill id/level
 */
public final class SkillHolder
{
	private final int _skillId;
	private final int _skillLvl;

	public SkillHolder(int skillId, int skillLvl)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
	}

	public SkillHolder(L2Skill skill)
	{
		_skillId = skill.getId();
		_skillLvl = skill.getLevelHash();
	}

	public final int getSkillId()
	{
		return _skillId;
	}

	public final int getSkillLvl()
	{
		return _skillLvl;
	}

	public final L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
	}
}
