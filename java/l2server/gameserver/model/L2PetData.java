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

import java.util.ArrayList;
import java.util.List;

/**
 * Class hold information about basic pet stats which are same on each level
 *
 * @author JIV
 */
public class L2PetData
{
	private TIntObjectHashMap<L2PetLevelData> _levelStats = new TIntObjectHashMap<>();
	private List<L2PetSkillLearn> _skills = new ArrayList<>();

	private int _load = 20000;
	private int _hungry_limit = 1;
	private int _minlvl = Byte.MAX_VALUE;
	private int[] _food = {};

	public void addNewStat(L2PetLevelData data, int level)
	{
		if (_minlvl > level)
		{
			_minlvl = level;
		}
		_levelStats.put(level, data);
	}

	public L2PetLevelData getPetLevelData(int petLevel)
	{
		return _levelStats.get(petLevel);
	}

	public int getLoad()
	{
		return _load;
	}

	public int getHungry_limit()
	{
		return _hungry_limit;
	}

	public int getMinLevel()
	{
		return _minlvl;
	}

	public int[] getFood()
	{
		return _food;
	}

	public void set_load(int _load)
	{
		this._load = _load;
	}

	public void set_hungry_limit(int _hungry_limit)
	{
		this._hungry_limit = _hungry_limit;
	}

	public void set_food(int[] _food)
	{
		this._food = _food;
	}

	//SKILS

	public void addNewSkill(int id, int lvl, int petLvl)
	{
		_skills.add(new L2PetSkillLearn(id, lvl, petLvl));
	}

	public int getAvailableLevel(int skillId, int petLvl)
	{
		int lvl = 0;
		for (L2PetSkillLearn temp : _skills)
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
		return _skills;
	}

	public static final class L2PetSkillLearn
	{
		private final int _id;
		private final int _level;
		private final int _minLevel;

		public L2PetSkillLearn(int id, int lvl, int minLvl)
		{
			_id = id;
			_level = lvl;
			_minLevel = minLvl;
		}

		public int getId()
		{
			return _id;
		}

		public int getLevel()
		{
			return _level;
		}

		public int getMinLevel()
		{
			return _minLevel;
		}
	}
}
