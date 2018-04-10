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
import l2server.util.loader.annotations.Load;
import l2server.util.loader.annotations.Reload;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LasTravel
 * @author Pere
 */

public class BeautyTable {
	private static Logger log = LoggerFactory.getLogger(BeautyTable.class.getName());

	public class BeautyTemplate {
		private int id;
		private Map<Integer, BeautyInfo> hairStyles = new HashMap<>();
		private Map<Integer, BeautyInfo> faceStyles = new HashMap<>();
		private Map<Integer, BeautyInfo> hairColors = new HashMap<>();

		public BeautyTemplate(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public Map<Integer, BeautyInfo> getHairStyles() {
			return hairStyles;
		}

		public Map<Integer, BeautyInfo> getFaceStyles() {
			return faceStyles;
		}

		public Map<Integer, BeautyInfo> getHairColors() {
			return hairColors;
		}
	}

	public class BeautyInfo {
		private int id;
		private int parentId;
		private int unk;
		private int adenaCost;
		private int ticketCost;

		private BeautyInfo(int id, int parentId, int unk, int adena, int tickets) {
			this.id = id;
			this.parentId = parentId;
			this.unk = unk;
			adenaCost = adena;
			ticketCost = tickets;
		}

		public int getId() {
			return id;
		}

		public int getParentId() {
			return parentId;
		}

		public int getUnk() {
			return unk;
		}

		public int getAdenaPrice() {
			return adenaCost;
		}

		public int getTicketPrice() {
			return ticketCost;
		}
	}

	private Map<Integer, BeautyTemplate> beautyTable = new HashMap<>();

	private BeautyTable() {
	}

	@Reload("beauty")
	@Load
	public void reload() {
		if (Config.IS_CLASSIC) {
			return;
		}
		
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "beautyShop.xml");

		XmlDocument doc = new XmlDocument(file);
		beautyTable.clear();
		BeautyTemplate template = new BeautyTemplate(0);
		for (XmlNode d : doc.getChildren()) {
			boolean isHairStyle = d.getName().equalsIgnoreCase("hairStyle");
			boolean isFaceStyle = d.getName().equalsIgnoreCase("faceStyle");
			boolean isHairColor = d.getName().equalsIgnoreCase("hairColor");
			if (isHairStyle || isFaceStyle || isHairColor) {
				int id = d.getInt("id");
				int parentId = d.getInt("parentId", 0);
				int unk = d.getInt("unk");
				int adenaCost = d.getInt("adenaCost");
				int ticketCost = d.getInt("ticketCost");

				BeautyInfo info = new BeautyInfo(id, parentId, unk, adenaCost, ticketCost);

				if (isHairStyle) {
					template.getHairStyles().put(id, info);
				} else if (isFaceStyle) {
					template.getFaceStyles().put(id, info);
				} else {
					template.getHairColors().put(id, info);
				}
			}
		}

		beautyTable.put(0, template);

		log.info("BeautyTable: Loaded " + template.getHairStyles().size() + " hair styles, " + template.getFaceStyles().size() + " face styles and " +
				template.getHairColors().size() + " hair colors!");
	}

	public BeautyTemplate getTemplate(int id) {
		return beautyTable.get(id);
	}

	public static BeautyTable getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final BeautyTable instance = new BeautyTable();
	}
}
