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

import l2server.Config;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pere
 */
public class ComboSkillTable
{
	public class Combo
	{
		public Combo(int i)
		{
			id = i;
		}

		public int id;
		public Map<Integer, Integer> skills = new HashMap<>();
	}

	private Map<Integer, Combo> _combos = new HashMap<>();

	private ComboSkillTable()
	{
		if (Config.IS_CLASSIC)
		{
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "comboSkills.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getFirstChild().getChildren())
		{
			if (n.getName().equalsIgnoreCase("combo"))
			{
				int id = n.getInt("id");
				Combo combo = new Combo(id);
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("skill"))
					{
						int skillId = d.getInt("id");
						int usedSkill = d.getInt("used");
						combo.skills.put(skillId, usedSkill);
					}
				}

				_combos.put(id, combo);
			}
		}

		Log.info("Combo Skill table: loaded " + _combos.size() + " combos.");
	}

	public Combo getCombo(int id)
	{
		return _combos.get(id);
	}

	public static ComboSkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ComboSkillTable _instance = new ComboSkillTable();
	}
}
