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
import lombok.Getter;

/**
 * @author BiggBoss
 *         Simple class for storing skill id/level
 */
public final class SkillHolder
{
	@Getter private final int skillId;
	@Getter private final int skillLvl;

	public SkillHolder(int skillId, int skillLvl)
	{
		this.skillId = skillId;
		this.skillLvl = skillLvl;
	}

	public SkillHolder(L2Skill skill)
	{
		skillId = skill.getId();
		skillLvl = skill.getLevelHash();
	}

	public final L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}
}
