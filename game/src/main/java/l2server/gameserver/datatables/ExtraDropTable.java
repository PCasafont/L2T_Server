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
import l2server.gameserver.model.L2DropCategory;
import l2server.gameserver.model.L2DropData;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class ...
 *
 * @version $Revision$ $Date$
 */
public class ExtraDropTable {
	private static Logger log = LoggerFactory.getLogger(ExtraDropTable.class.getName());

	private Map<Integer, ArrayList<L2DropCategory>> extraGroups = new HashMap<>();

	public static ExtraDropTable getInstance() {
		return SingletonHolder.instance;
	}

	private ExtraDropTable() {
	}
	
	@Load(dependencies = ItemTable.class)
	private void restoreData() {
		if (Config.IS_CLASSIC) {
			return;
		}
		
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "extraDropGroups.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("extraDrop")) {
				int id = d.getInt("id");
				ArrayList<L2DropCategory> extraGroup = new ArrayList<>();
				for (XmlNode propertyNode : d.getChildren()) {
					if (propertyNode.getName().equalsIgnoreCase("dropCategory")) {
						float chance = propertyNode.getFloat("chance");
						L2DropCategory dc = new L2DropCategory(chance);

						for (XmlNode dropCategoryNode : propertyNode.getChildren()) {
							if (dropCategoryNode.getName().equalsIgnoreCase("itemDrop")) {
								int itemId = dropCategoryNode.getInt("itemId");
								int min = dropCategoryNode.getInt("min");
								int max = dropCategoryNode.getInt("max");
								float chance2 = dropCategoryNode.getFloat("chance");
								L2DropData dd = new L2DropData(itemId, min, max, chance2);

								if (ItemTable.getInstance().getTemplate(dd.getItemId()) == null) {
									log.warn(
											"Drop data for undefined item template! Extra drop category id: " + id + " itemId: " + dd.getItemId());
									continue;
								}

								dc.addDropData(dd);
							}
						}
						extraGroup.add(dc);
					}
				}
				extraGroups.put(id, extraGroup);
			}
		}
	}

	public ArrayList<L2DropCategory> getExtraDroplist(int groupId) {
		return extraGroups.get(groupId);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ExtraDropTable instance = new ExtraDropTable();
	}
}
