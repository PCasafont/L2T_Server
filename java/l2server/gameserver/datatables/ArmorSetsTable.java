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

/*
  @author godson
 */

package l2server.gameserver.datatables;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import l2server.Config;
import l2server.gameserver.Reloadable;
import l2server.gameserver.ReloadableManager;
import l2server.gameserver.model.L2ArmorSet;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author Pere
 */
public class ArmorSetsTable implements Reloadable
{

	private TIntObjectHashMap<L2ArmorSet> _armorSets;

	public static ArmorSetsTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private ArmorSetsTable()
	{
		_armorSets = new TIntObjectHashMap<>();
		reload();

		ReloadableManager.getInstance().register("armorsets", this);
	}

	@Override
	public boolean reload()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "armorSets.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("armorSet"))
					{
						int id = d.getInt("id");
						int parts = d.getInt("parts");

						TIntIntHashMap skills = new TIntIntHashMap();
						int enchant6Skill = 0, shieldSkill = 0;

						for (XmlNode skillNode : d.getChildren())
						{
							if (skillNode.getName().equalsIgnoreCase("skill"))
							{
								int skillId = skillNode.getInt("id");
								int levels = skillNode.getInt("levels");
								skills.put(skillId, levels);
							}
							else if (skillNode.getName().equalsIgnoreCase("enchant6Skill"))
							{
								enchant6Skill = skillNode.getInt("id");
							}
							else if (skillNode.getName().equalsIgnoreCase("shieldSkill"))
							{
								shieldSkill = skillNode.getInt("id");
							}
						}

						_armorSets.put(id, new L2ArmorSet(id, parts, skills, enchant6Skill, shieldSkill));
					}
				}
				Log.info("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
			}
		}

		return true;
	}

	@Override
	public String getReloadMessage(boolean success)
	{
		return "Armor Sets reloaded";
	}

	public boolean setExists(int chestId)
	{
		return _armorSets.containsKey(chestId);
	}

	public L2ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ArmorSetsTable _instance = new ArmorSetsTable();
	}
}
