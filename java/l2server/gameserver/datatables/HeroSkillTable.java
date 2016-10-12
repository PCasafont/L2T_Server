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

package l2server.gameserver.datatables;

import l2server.gameserver.model.L2Skill;

/**
 * @author BiTi
 */
public class HeroSkillTable
{
	private static final L2Skill[] _heroSkills = new L2Skill[5];
	private static final int[] _heroSkillsId = {395, 396, 1374, 1375, 1376};

	private HeroSkillTable()
	{
		for (int i = 0; i < _heroSkillsId.length; i++)
		{
			_heroSkills[i] = SkillTable.getInstance().getInfo(_heroSkillsId[i], 1);
		}
	}

	public static HeroSkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public static L2Skill[] getHeroSkills()
	{
		return _heroSkills;
	}

	public static boolean isHeroSkill(int skillid)
	{
		/*
         * Do not perform checks directly on L2Skill array,
		 * it will cause errors due to SkillTable not initialized
		 */
		for (int id : _heroSkillsId)
		{
			if (id == skillid)
			{
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final HeroSkillTable _instance = new HeroSkillTable();
	}
}
