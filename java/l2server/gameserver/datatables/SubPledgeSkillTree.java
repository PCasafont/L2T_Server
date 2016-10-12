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

import gnu.trove.TLongObjectHashMap;
import l2server.Config;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Skill;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author JIV
 */
public class SubPledgeSkillTree
{
	private TLongObjectHashMap<SubUnitSkill> _skilltree = new TLongObjectHashMap<>();

	public SubPledgeSkillTree()
	{
		load();
	}

	public static SubPledgeSkillTree getInstance()
	{
		return SingletonHolder._instance;
	}

	public static class SubUnitSkill
	{
		private L2Skill skill;
		private int clanLvl;
		private int reputation;
		private int itemId;
		private int count;

		public SubUnitSkill(L2Skill skill, int clanLvl, int reputation, int itemId, int count)
		{
			super();
			this.skill = skill;
			this.clanLvl = clanLvl;
			this.reputation = reputation;
			this.itemId = itemId;
			this.count = count;
		}

		public L2Skill getSkill()
		{
			return skill;
		}

		public int getClanLvl()
		{
			return clanLvl;
		}

		public int getReputation()
		{
			return reputation;
		}

		public int getItemId()
		{
			return itemId;
		}

		public int getCount()
		{
			return count;
		}
	}

	public void reload()
	{
		load();
	}

	private void load()
	{
		_skilltree.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "skilltrees/subpledgeskilltree.xml");
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
							int itemId;
							int count;

							if (!d.hasAttribute("id"))
							{
								Log.severe("[SubPledgeSkillTree] Missing id, skipping");
								continue;
							}
							skillId = d.getInt("id");

							if (!d.hasAttribute("level"))
							{
								Log.severe("[SubPledgeSkillTree] Missing level, skipping");
								continue;
							}
							skillLvl = d.getInt("level");

							if (!d.hasAttribute("reputation"))
							{
								Log.severe("[SubPledgeSkillTree] Missing reputation, skipping");
								continue;
							}
							reputation = d.getInt("level");

							if (!d.hasAttribute("clanLevel"))
							{
								Log.severe("[SubPledgeSkillTree] Missing clan_level, skipping");
								continue;
							}
							clanLvl = d.getInt("clanLevel");

							if (!d.hasAttribute("itemId"))
							{
								Log.severe("[SubPledgeSkillTree] Missing itemId, skipping");
								continue;
							}
							itemId = d.getInt("itemId");

							if (!d.hasAttribute("count"))
							{
								Log.severe("[SubPledgeSkillTree] Missing count, skipping");
								continue;
							}
							count = d.getInt("count");

							L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
							if (skill == null)
							{
								Log.severe("[SubPledgeSkillTree] Skill " + skillId + " not exist, skipping");
								continue;
							}

							_skilltree.put(SkillTable.getSkillHashCode(skill),
									new SubUnitSkill(skill, clanLvl, reputation, itemId, count));
						}
					}
				}
			}
		}
		Log.info(getClass().getSimpleName() + ": Loaded " + _skilltree.size() + " SubUnit Skills");
	}

	public SubUnitSkill getSkill(long skillhash)
	{
		return _skilltree.get(skillhash);
	}

	public SubUnitSkill[] getAvailableSkills(L2Clan clan)
	{
		ArrayList<SubUnitSkill> list = new ArrayList<>();
		for (Object obj : _skilltree.getValues())
		{
			SubUnitSkill skill = (SubUnitSkill) obj;
			if (skill.getClanLvl() <= clan.getLevel())
			{
				list.add(skill);
			}
		}

		Iterator<SubUnitSkill> it = list.iterator();
		while (it.hasNext())
		{
			SubUnitSkill sus = it.next();
			if (!clan.isLearnableSubSkill(sus.getSkill()))
			{
				it.remove();
			}
		}

		return list.toArray(new SubUnitSkill[list.size()]);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SubPledgeSkillTree _instance = new SubPledgeSkillTree();
	}
}
