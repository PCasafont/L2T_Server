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

package l2server.gameserver.model;

import gnu.trove.TIntObjectHashMap;
import l2server.gameserver.datatables.SkillTable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class hold information about basic pet stats which are same on each level
 *
 * @author JIV
 */
public class L2PetData
{
	private TIntObjectHashMap<L2PetLevelData> levelStats = new TIntObjectHashMap<>();
	private List<L2PetSkillLearn> skills = new ArrayList<>();

	@Getter private int load = 20000;
	@Setter private int _hungry_limit = 1;
	private int minlvl = Byte.MAX_VALUE;
	@Getter private int[] food = {};

	public void addNewStat(L2PetLevelData data, int level)
	{
		if (minlvl > level)
		{
			minlvl = level;
		}
		levelStats.put(level, data);
	}

	public L2PetLevelData getPetLevelData(int petLevel)
	{
		return levelStats.get(petLevel);
	}

	public int getHungry_limit()
	{
		return _hungry_limit;
	}

	public int getMinLevel()
	{
		return minlvl;
	}

	public void set_load(int load)
	{
		this.load = load;
	}

	public void set_food(int[] food)
	{
		this.food = food;
	}

	//SKILS

	public void addNewSkill(int id, int lvl, int petLvl)
	{
		skills.add(new L2PetSkillLearn(id, lvl, petLvl));
	}

	public int getAvailableLevel(int skillId, int petLvl)
	{
		int lvl = 0;
		for (L2PetSkillLearn temp : skills)
		{
			if (temp.getId() != skillId)
			{
				continue;
			}
			if (temp.getLevel() == 0)
			{
				if (petLvl < 70)
				{
					lvl = petLvl / 10;
					if (lvl <= 0)
					{
						lvl = 1;
					}
				}
				else
				{
					lvl = 7 + (petLvl - 70) / 5;
				}

				// formula usable for skill that have 10 or more skill levels
				int maxLvl = SkillTable.getInstance().getMaxLevel(temp.getId());
				if (lvl > maxLvl)
				{
					lvl = maxLvl;
				}
				break;
			}
			else if (temp.getMinLevel() <= petLvl)
			{
				if (temp.getLevel() > lvl)
				{
					lvl = temp.getLevel();
				}
			}
		}
		return lvl;
	}

	public List<L2PetSkillLearn> getAvailableSkills()
	{
		return skills;
	}

	public static final class L2PetSkillLearn
	{
		@Getter private final int id;
		@Getter private final int level;
		@Getter private final int minLevel;

		public L2PetSkillLearn(int id, int lvl, int minLvl)
		{
			this.id = id;
			level = lvl;
			minLevel = minLvl;
		}
	}
}
