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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Skill;

/**
 * Format: (ch) dd[dd][ddd]
 *
 * @author -Wooden-
 */
public class PledgeSkillList extends L2GameServerPacket
{
	private L2Skill[] _skills;
	private SubPledgeSkill[] _subSkills;

	public static class SubPledgeSkill
	{
		public SubPledgeSkill(int subType, int skillId, int skillLvl)
		{
			super();
			this.subType = subType;
			this.skillId = skillId;
			this.skillLvl = skillLvl;
		}

		int subType;
		int skillId;
		int skillLvl;
	}

	public PledgeSkillList(L2Clan clan)
	{
		_skills = clan.getAllSkills();
		_subSkills = clan.getAllSubSkills();
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_skills.length);
		writeD(_subSkills.length); // squad skill length
		for (L2Skill sk : _skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevelHash());
		}
		for (SubPledgeSkill sk : _subSkills)
		{
			writeD(sk.subType); // clan Sub-unit types
			writeD(sk.skillId);
			writeD(sk.skillLvl);
		}
	}
}
