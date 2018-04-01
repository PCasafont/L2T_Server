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
import java.util.HashMap; import java.util.Map;
import l2server.Config;
import l2server.gameserver.model.L2ArmorSet;
import l2server.log.Log;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

/**
 * @author Pere
 */
public class ArmorSetsTable {

	private Map<Integer, L2ArmorSet> armorSets = new HashMap<>();

	public static ArmorSetsTable getInstance() {
		return SingletonHolder.instance;
	}

	private ArmorSetsTable() {
	}

	@Reload("armorsets")
	@Load
	public void load() {
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "armorSets.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("armorSet")) {
				int id = d.getInt("id");
				int parts = d.getInt("parts");

				TIntIntHashMap skills = new TIntIntHashMap();
				int enchant6Skill = 0, shieldSkill = 0;

				for (XmlNode skillNode : d.getChildren()) {
					if (skillNode.getName().equalsIgnoreCase("skill")) {
						int skillId = skillNode.getInt("id");
						int levels = skillNode.getInt("levels");
						skills.put(skillId, levels);
					} else if (skillNode.getName().equalsIgnoreCase("enchant6Skill")) {
						enchant6Skill = skillNode.getInt("id");
					} else if (skillNode.getName().equalsIgnoreCase("shieldSkill")) {
						shieldSkill = skillNode.getInt("id");
					}
				}

				armorSets.put(id, new L2ArmorSet(id, parts, skills, enchant6Skill, shieldSkill));
			}
		}
		Log.info("ArmorSetsTable: Loaded " + armorSets.size() + " armor sets.");
	}

	public boolean setExists(int chestId) {
		return armorSets.containsKey(chestId);
	}

	public L2ArmorSet getSet(int chestId) {
		return armorSets.get(chestId);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ArmorSetsTable instance = new ArmorSetsTable();
	}
}
