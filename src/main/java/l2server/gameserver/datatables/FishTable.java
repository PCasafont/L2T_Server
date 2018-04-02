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
import l2server.gameserver.model.FishData;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author -Nemesiss-
 */
public class FishTable {
	private static Logger log = LoggerFactory.getLogger(FishTable.class.getName());



	private static List<FishData> fishsNormal = new ArrayList<>();
	private static List<FishData> fishsEasy = new ArrayList<>();
	private static List<FishData> fishsHard = new ArrayList<>();

	public static FishTable getInstance() {
		return SingletonHolder.instance;
	}
	
	private FishTable() {
	}
	
	@Load
	public void load() {
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "fishes.xml");
		XmlDocument doc = new XmlDocument(file);
		
		int count = 0;
		for (XmlNode n : doc.getChildren()) {
			if (n.getName().equalsIgnoreCase("fish")) {
				int id = n.getInt("id");
				int lvl = n.getInt("level");
				String name = n.getString("name");
				int hp = n.getInt("hp");
				int hpReg = n.getInt("hpReg");
				int type = n.getInt("type");
				int group = n.getInt("group");
				int guts = n.getInt("guts");
				int gutsCheckTime = n.getInt("gutsCheckTime");
				int waitTime = n.getInt("waitTime");
				int combatTime = n.getInt("combatTime");
				FishData fish = new FishData(id, lvl, name, hp, hpReg, type, group, guts, gutsCheckTime, waitTime, combatTime);
				switch (fish.getGroup()) {
					case 0:
						fishsEasy.add(fish);
						break;
					case 1:
						fishsNormal.add(fish);
						break;
					case 2:
						fishsHard.add(fish);
				}

				count++;
			}
		}
		
		log.info("FishTable: Loaded " + count + " Fishes.");
	}
	
	/**
	 * @return List of Fish that can be fished
	 */
	public List<FishData> getfish(int lvl, int type, int group) {
		List<FishData> result = new ArrayList<>();
		List<FishData> Fishs = null;
		switch (group) {
			case 0:
				Fishs = fishsEasy;
				break;
			case 1:
				Fishs = fishsNormal;
				break;
			case 2:
				Fishs = fishsHard;
		}
		if (Fishs == null) {
			// the fish list is empty
			log.warn("Fish are not defined !");
			return null;
		}
		for (FishData f : Fishs) {
			if (f.getLevel() != lvl) {
				continue;
			}
			if (f.getType() != type) {
				continue;
			}

			result.add(f);
		}
		if (result.isEmpty()) {
			log.warn("Cant Find Any Fish!? - Lvl: " + lvl + " Type: " + type);
		}
		return result;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final FishTable instance = new FishTable();
	}
}
