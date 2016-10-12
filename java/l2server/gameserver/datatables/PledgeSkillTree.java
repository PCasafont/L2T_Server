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
import l2server.gameserver.model.L2PledgeSkillLearn;
import l2server.gameserver.model.L2Skill;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JIV
 */
public class PledgeSkillTree
{
	private Map<Long, L2PledgeSkillLearn> _skillTree = new HashMap<>();

	public PledgeSkillTree()
	{
		load();
	}

	public static PledgeSkillTree getInstance()
	{
		return SingletonHolder._instance;
	}

	public void reload()
	{
		load();
	}

	private void load()
	{
		_skillTree.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skilltrees/pledgeSkillTree.xml");
		if (file.exists())
		{
			XmlDocument doc = new XmlDocument(file);
			for (XmlNode n : doc.getChildren())
			{
				if ("skill_tree".equalsIgnoreCase(n.getName()))
				{
					for (XmlNode d : n.getChildren())
					{
						if (d.getName().equalsIgnoreCase("skill"))
						{
							int skillId;
							int skillLvl;
							int clanLvl;
							int reputation;

							if (!d.hasAttribute("id"))
							{
								Log.severe("[PledgeSkillTree] Missing id, skipping");
								continue;
							}
							skillId = d.getInt("id");

							if (!d.hasAttribute("level"))
							{
								Log.severe("[PledgeSkillTree] Missing level, skipping");
								continue;
							}
							skillLvl = d.getInt("level");

							if (!d.hasAttribute("reputation"))
							{
								Log.severe("[PledgeSkillTree] Missing reputation, skipping");
								continue;
							}
							reputation = d.getInt("reputation");

							if (!d.hasAttribute("clanLevel"))
							{
								Log.severe("[PledgeSkillTree] Missing clan_level, skipping");
								continue;
							}
							clanLvl = d.getInt("clanLevel");

							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							if (skill == null)
							{
								Log.severe("[PledgeSkillTree] Skill " + skillId + " not exist, skipping");
								continue;
							}

							_skillTree.put(SkillTable.getSkillHashCode(skill),
									new L2PledgeSkillLearn(skillId, skillLvl, clanLvl, reputation));
						}
					}
				}
			}
		}
		Log.info(getClass().getSimpleName() + ": Loaded " + _skillTree.size() + " Pledge Skills");
	}

	public L2PledgeSkillLearn[] getAvailableSkills(L2PcInstance cha)
	{
		List<L2PledgeSkillLearn> result = new ArrayList<>();
		Map<Long, L2PledgeSkillLearn> skills = _skillTree;

		if (skills == null)
		{
			// the _skillTree for this class is undefined, so we give an empty list

			Log.warning("No clan skills defined!");
			return new L2PledgeSkillLearn[]{};
		}

		L2Skill[] oldSkills = cha.getClan().getAllSkills();

		for (L2PledgeSkillLearn temp : skills.values())
		{
			if (temp.getBaseLevel() <= cha.getClan().getLevel())
			{
				boolean knownSkill = false;

				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;

						if (oldSkills[j].getLevelHash() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}

				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}

		return result.toArray(new L2PledgeSkillLearn[result.size()]);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final PledgeSkillTree _instance = new PledgeSkillTree();
	}
}
