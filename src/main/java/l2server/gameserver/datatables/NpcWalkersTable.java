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
import l2server.gameserver.ai.L2NpcWalkerAI;
import l2server.gameserver.model.L2NpcWalkerNode;
import l2server.gameserver.model.L2Spawn;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.templates.chars.L2NpcTemplate;
import l2server.log.Log;
import l2server.util.loader.annotations.Load;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 *
 * @author Rayan RPG for L2Emu Project, JIV
 * @since 927
 */
public class NpcWalkersTable {
	private Map<Integer, List<L2NpcWalkerNode>> routes = new HashMap<>();

	public static NpcWalkersTable getInstance() {
		return SingletonHolder.instance;
	}

	private NpcWalkersTable() {
	}
	
	@Load(dependencies = NpcTable.class)
	public void load() {
		if (!Config.ALLOW_NPC_WALKERS) {
			return;
		}
		
		Log.info("Initializing Walkers Routes Table.");
		routes.clear();
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "WalkerRoutes.xml");
		if (file.exists()) {
			XmlDocument doc = new XmlDocument(file);
			for (XmlNode d : doc.getChildren()) {
				if (d.getName().equals("walker")) {
					List<L2NpcWalkerNode> route = new ArrayList<>();
					int npcId = d.getInt("npcId");
					for (XmlNode r : d.getChildren()) {
						if (r.getName().equals("route")) {
							int x = r.getInt("X");
							int y = r.getInt("Y");
							int z = r.getInt("Z");
							int delay = r.getInt("delay");
							String chat = r.getString("string");
							boolean running = r.getBool("run");
							route.add(new L2NpcWalkerNode(x, y, z, delay, chat, running));
						}
					}
					//routes.put(npcId, route);

					try {
						L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(npcId);
						L2Spawn walkerSpawn = new L2Spawn(tmpl);

						walkerSpawn.setX(route.get(0).getMoveX());
						walkerSpawn.setY(route.get(0).getMoveY());
						walkerSpawn.setZ(route.get(0).getMoveZ());

						SpawnTable.getInstance().addNewSpawn(walkerSpawn, false);

						walkerSpawn.startRespawn();
						walkerSpawn.doSpawn();

						L2Npc walker = walkerSpawn.getNpc();
						L2NpcWalkerAI walkerAI = new L2NpcWalkerAI(walker);

						walker.setAI(walkerAI);
						walkerAI.initializeRoute(route);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (Object list : routes.values()) {
			((ArrayList<?>) list).trimToSize();
		}

		Log.info("WalkerRoutesTable: Loaded " + routes.size() + " Npc Walker Routes.");
	}

	public List<L2NpcWalkerNode> getRouteForNpc(int id) {
		return routes.get(id);
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final NpcWalkersTable instance = new NpcWalkersTable();
	}
}
