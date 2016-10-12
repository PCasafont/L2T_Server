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

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillList extends L2GameServerPacket
{
	public enum EnchantSkillType
	{
		NORMAL, SAFE, UNTRAIN, CHANGE_ROUTE,
	}

	private final EnchantSkillType _type;
	private final List<Skill> _skills;

	static class Skill
	{
		public int id;
		public int nextLevel;

		Skill(int pId, int pNextLevel)
		{
			id = pId;
			nextLevel = pNextLevel;
		}
	}

	public void addSkill(int id, int level)
	{
		_skills.add(new Skill(id, level));
	}

	public ExEnchantSkillList(EnchantSkillType type)
	{
		_type = type;
		_skills = new ArrayList<>();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_type.ordinal());
		writeD(_skills.size());
		for (Skill sk : _skills)
		{
			writeD(sk.id);
			writeD(sk.nextLevel);
		}
	}
}
