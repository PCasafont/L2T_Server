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

import java.util.HashMap; import java.util.Map;
import l2server.Config;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.StaticObjectInstance;
import l2server.gameserver.templates.StatsSet;
import l2server.gameserver.templates.chars.CreatureTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;

public class StaticObjects {
	private static Logger log = LoggerFactory.getLogger(StaticObjects.class.getName());


	private Map<Integer, StaticObjectInstance> staticObjects = new HashMap<>();

	public static StaticObjects getInstance() {
		return SingletonHolder.instance;
	}

	private StaticObjects() {
	}
	
	@Load(dependencies = World.class)
	private void parseData() {
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "staticObjects.xml");
		XmlDocument doc = new XmlDocument(file);

		for (XmlNode d : doc.getChildren()) {
			if (d.getName().equalsIgnoreCase("object")) {
				int id = d.getInt("id");
				int x = d.getInt("x");
				int y = d.getInt("y");
				int z = d.getInt("z");
				int type = d.getInt("type");
				String texture = d.getString("texture");
				int map_x = d.getInt("mapX");
				int map_y = d.getInt("mapY");

				StatsSet npcDat = new StatsSet();
				npcDat.set("npcId", id);
				npcDat.set("level", 0);
				npcDat.set("jClass", "staticobject");

				npcDat.set("STR", 0);
				npcDat.set("CON", 0);
				npcDat.set("DEX", 0);
				npcDat.set("INT", 0);
				npcDat.set("WIT", 0);
				npcDat.set("MEN", 0);

				//npcDat.set("name", "");
				npcDat.set("collisionRadius", 10);
				npcDat.set("collisionHeight", 10);
				npcDat.set("sex", "male");
				npcDat.set("type", "");
				npcDat.set("atkRange", 0);
				npcDat.set("mpMax", 0);
				npcDat.set("cpMax", 0);
				npcDat.set("rewardExp", 0);
				npcDat.set("rewardSp", 0);
				npcDat.set("pAtk", 0);
				npcDat.set("mAtk", 0);
				npcDat.set("pAtkSpd", 0);
				npcDat.set("aggroRange", 0);
				npcDat.set("mAtkSpd", 0);
				npcDat.set("rhand", 0);
				npcDat.set("lhand", 0);
				npcDat.set("armor", 0);
				npcDat.set("walkSpd", 0);
				npcDat.set("runSpd", 0);
				npcDat.set("name", "");
				npcDat.set("hpMax", 1);
				npcDat.set("hpReg", 3.e-3f);
				npcDat.set("mpReg", 3.e-3f);
				npcDat.set("pDef", 1);
				npcDat.set("mDef", 1);

				CreatureTemplate template = new CreatureTemplate(npcDat);
				StaticObjectInstance obj = new StaticObjectInstance(IdFactory.getInstance().getNextId(), template, id);
				obj.setType(type);
				obj.setXYZ(x, y, z);
				obj.setMap(texture, map_x, map_y);
				obj.spawnMe();

				staticObjects.put(obj.getStaticObjectId(), obj);
			}
		}
		log.info("StaticObject: Loaded " + staticObjects.size() + " StaticObject Templates.");
	}

	public StaticObjectInstance getObject(int id) {
		return staticObjects.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final StaticObjects instance = new StaticObjects();
	}
}
