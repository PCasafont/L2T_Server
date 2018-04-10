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
 */

public class ScenePlayerDataTable {
	private static Logger log = LoggerFactory.getLogger(ScenePlayerDataTable.class.getName());

	private Map<Integer, Integer> sceneDataTable;

	@Reload("scenes")
	@Load
	public boolean load() {
		sceneDataTable = new HashMap<>();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "scenePlayerData.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("scene")) {
				int id = d.getInt("id");

				int time = d.getInt("time");

				sceneDataTable.put(id, time);
			}
		}

		log.info("ScenePlayerTable: Loaded: " + sceneDataTable.size() + " scenes!");

		return false;
	}

	public static ScenePlayerDataTable getInstance() {
		return SingletonHolder.instance;
	}

	public Map<Integer, Integer> getSceneTable() {
		return sceneDataTable;
	}

	public int getVideoDuration(int vidId) {
		return sceneDataTable.get(vidId);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final ScenePlayerDataTable instance = new ScenePlayerDataTable();
	}
}
