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
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.item.HennaTemplate;
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HennaTable {
	private static Logger log = LoggerFactory.getLogger(HennaTable.class.getName());


	private Map<Integer, HennaTemplate> henna = new HashMap<>();

	public static HennaTable getInstance() {
		return SingletonHolder.instance;
	}

	private HennaTable() {
	}
	
	@Reload("henna")
	@Load(dependencies = {PlayerClassTable.class, SkillTable.class})
	public void load() {
		if (Config.IS_CLASSIC) {
			return;
		}
		
		henna.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "henna.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode hennaNode : doc.getChildren()) {
			if (hennaNode.getName().equalsIgnoreCase("henna")) {
				StatsSet hennaDat = new StatsSet();
				int id = hennaNode.getInt("symbolId");

				hennaDat.set("symbolId", id);
				hennaDat.set("dyeId", hennaNode.getInt("dyeId"));
				hennaDat.set("name", hennaNode.getString("name"));
				if (hennaNode.hasAttribute("price")) {
					hennaDat.set("price", hennaNode.getLong("price"));
				}
				if (hennaNode.hasAttribute("STR")) {
					hennaDat.set("STR", hennaNode.getInt("STR"));
				}
				if (hennaNode.hasAttribute("CON")) {
					hennaDat.set("CON", hennaNode.getInt("CON"));
				}
				if (hennaNode.hasAttribute("DEX")) {
					hennaDat.set("DEX", hennaNode.getInt("DEX"));
				}
				if (hennaNode.hasAttribute("INT")) {
					hennaDat.set("INT", hennaNode.getInt("INT"));
				}
				if (hennaNode.hasAttribute("WIT")) {
					hennaDat.set("WIT", hennaNode.getInt("WIT"));
				}
				if (hennaNode.hasAttribute("MEN")) {
					hennaDat.set("MEN", hennaNode.getInt("MEN"));
				}
				if (hennaNode.hasAttribute("LUC")) {
					hennaDat.set("LUC", hennaNode.getInt("LUC"));
				}
				if (hennaNode.hasAttribute("CHA")) {
					hennaDat.set("CHA", hennaNode.getInt("CHA"));
				}
				if (hennaNode.hasAttribute("elemId")) {
					hennaDat.set("elemId", hennaNode.getInt("elemId"));
				}
				if (hennaNode.hasAttribute("elemVal")) {
					hennaDat.set("elemVal", hennaNode.getInt("elemVal"));
				}

				if (hennaNode.hasAttribute("time")) {
					hennaDat.set("time", hennaNode.getLong("time"));
				}
				if (hennaNode.hasAttribute("fourthSlot")) {
					hennaDat.set("fourthSlot", hennaNode.getBool("fourthSlot"));
				}
				if (hennaNode.hasAttribute("skills")) {
					hennaDat.set("skills", hennaNode.getString("skills"));
				}

				HennaTemplate henna = new HennaTemplate(hennaDat);

				for (XmlNode allowedClassNode : hennaNode.getChildren()) {
					if (allowedClassNode.getName().equalsIgnoreCase("allowedClass")) {
						int classId = allowedClassNode.getInt("id");
						PlayerClassTable.getInstance().getClassById(classId).addAllowedDye(henna);
					}
				}

				this.henna.put(id, henna);
			}
		}
		log.info("HennaTable: Loaded " + henna.size() + " Templates.");
	}

	public HennaTemplate getTemplate(int id) {
		return henna.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final HennaTable instance = new HennaTable();
	}
}
