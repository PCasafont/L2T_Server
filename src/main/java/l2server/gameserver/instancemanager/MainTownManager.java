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

package l2server.gameserver.instancemanager;

import l2server.Config;
import l2server.gameserver.Announcements;
import l2server.gameserver.GeoData;
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.datatables.SpawnTable;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.zone.type.L2DummyZone;
import l2server.gameserver.model.zone.type.L2TownZone;
import l2server.log.Log;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Pere
 */
public class MainTownManager {
	public class MainTownInfo {
		private final int townId;
		private final String name;
		private final int startX;
		private final int startY;
		private final int startZ;
		private final int startRandom;

		public MainTownInfo(int townId, String name, int startX, int startY, int startZ, int startRandom) {
			this.townId = townId;
			this.name = name;
			this.startX = startX;
			this.startY = startY;
			this.startZ = startZ;
			this.startRandom = startRandom;
		}

		public int getTownId() {
			return townId;
		}

		public String getName() {
			return name;
		}

		public int getStartX() {
			return startX;
		}

		public int getStartY() {
			return startY;
		}

		public int getStartZ() {
			return startZ;
		}

		public int getStartRandom() {
			return startRandom;
		}

		public void spawnNpcs() {
			SpawnTable.getInstance().spawnSpecificTable(name + "_custom_spawns");
		}

		public void despawnNpcs() {
			SpawnTable.getInstance().despawnSpecificTable(name + "_custom_spawns");
		}

		public List<L2PcInstance> getPlayersInside() {
			L2TownZone zone = TownManager.getTown(townId);
			return zone.getPlayersInside();
		}

		public void teleportPlayers(List<L2PcInstance> players) {
			L2DummyZone zone = ZoneManager.getInstance().getZoneByName(name + "DummyZone", L2DummyZone.class);
			for (L2PcInstance player : players) {
				int[] coords = zone.getZone().getRandomPoint();
				player.teleToLocation(coords[0], coords[1], GeoData.getInstance().getHeight(coords[0], coords[1], coords[2]));
			}
		}

		public int[] getRandomCoords() {
			L2DummyZone zone = ZoneManager.getInstance().getZoneByName(name + "DummyZone", L2DummyZone.class);
			int[] coords = zone.getZone().getRandomPoint();
			coords[2] = GeoData.getInstance().getHeight(coords[0], coords[1], coords[2]);
			return coords;
		}
	}

	private static final int ROTATION_INTERVAL_DAYS = 2;

	private final List<MainTownInfo> mainTowns = new ArrayList<>();

	private int rotationIndex = 0;
	private MainTownInfo currentMainTown = null;

	private List<MainTownInfo> nextTowns = new ArrayList<>();
	private long nextTownTimer = 0L;

	public MainTownManager() {
		load();
	}

	private void load() {
		if (Config.isServer(Config.TENKAI_LEGACY)) {
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, "/data_" + Config.SERVER_NAME + "/mainTowns.xml");
		if (!file.exists()) {
			Log.warning("Dir " + file.getAbsolutePath() + " doesn't exist");
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode townNode : doc.getChildren()) {
			if (!townNode.getName().equalsIgnoreCase("town")) {
				continue;
			}

			int townId = townNode.getInt("id");
			String name = townNode.getString("name");
			int startX = townNode.getInt("startX");
			int startY = townNode.getInt("startY");
			int startZ = townNode.getInt("startZ");
			int startRandom = townNode.getInt("startRandom");
			mainTowns.add(new MainTownInfo(townId, name, startX, startY, startZ, startRandom));
		}

		rotationIndex = 0;
		if (GlobalVariablesManager.getInstance().isVariableStored("MTRotationIndex")) {
			rotationIndex = Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("MTRotationIndex"));
		} else {
			GlobalVariablesManager.getInstance().storeVariable("MTRotationIndex", String.valueOf(rotationIndex));
		}

		Random random = new Random(79286);
		for (int i = 0; i < rotationIndex; i++) {
			random.nextInt();
		}

		for (int i = 0; i < 1000; i++) {
			nextTowns.add(mainTowns.get(random.nextInt(mainTowns.size())));
		}

		currentMainTown = nextTowns.remove(0);
		currentMainTown.spawnNpcs();

		if (GlobalVariablesManager.getInstance().isVariableStored("MTLastRotation")) {
			nextTownTimer = Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("MTLastRotation")) +
					ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000;
		}

		if (nextTownTimer - System.currentTimeMillis() < 0) {
			if (nextTownTimer > 0) {
				while (nextTownTimer - System.currentTimeMillis() < 0) {
					changeMainTown();
					nextTownTimer += ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000;
				}
			} else {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 6);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				GlobalVariablesManager.getInstance().storeVariable("MTLastRotation", String.valueOf(cal.getTimeInMillis()));

				cal.roll(Calendar.DAY_OF_YEAR, ROTATION_INTERVAL_DAYS);
				nextTownTimer = cal.getTimeInMillis();
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			changeMainTown();
			nextTownTimer = System.currentTimeMillis() + ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000;
		}, nextTownTimer - System.currentTimeMillis(), ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000);
		//}, 3600000L * 5, 3600000L * 5);

		Log.info("MainTownManager: Loaded " + mainTowns.size() + " main towns.");
		Log.info("MainTownManager: The current main town is " + currentMainTown.getName() + ".");
	}

	private void changeMainTown() {
		MainTownInfo nextMainTown = nextTowns.remove(0);

		rotationIndex++;
		GlobalVariablesManager.getInstance().storeVariable("MTRotationIndex", String.valueOf(rotationIndex));
		GlobalVariablesManager.getInstance().storeVariable("MTLastRotation", String.valueOf(System.currentTimeMillis()));

		if (nextMainTown == currentMainTown) {
			return;
		}

		currentMainTown.despawnNpcs();
		nextMainTown.spawnNpcs();
		nextMainTown.teleportPlayers(currentMainTown.getPlayersInside());
		currentMainTown = nextMainTown;

		Announcements.getInstance().announceToAll("The main town has changed! Now it's " + currentMainTown.getName() + ".");
	}

	public MainTownInfo getCurrentMainTown() {
		//for (MainTownInfo inf : mainTowns)
		//	inf.spawnNpcs();
		return currentMainTown;
	}

	public String getNextTownsInfo() {
		String info = "";
		MainTownInfo prevTown = currentMainTown;
		Date prevDate = null;
		for (int i = 0; i < 10; i++) {
			MainTownInfo town = nextTowns.get(i);
			if (town != prevTown) {
				Date date = new Date(nextTownTimer + (ROTATION_INTERVAL_DAYS * i - 1) * 24 * 3600 * 1000);
				String from = "now";
				if (prevDate != null) {
					from = new SimpleDateFormat("E MMM d").format(prevDate);
				}
				String to = new SimpleDateFormat("E MMM d").format(date);

				info += "- From <font color=LEVEL>" + from + "</font> to <font color=LEVEL>" + to + "</font> " +
						"the main town will be <font color=LEVEL>" + prevTown.getName() + "</font>.<br1>";

				if (i >= 4) {
					break;
				}

				prevTown = town;
				prevDate = new Date(nextTownTimer + ROTATION_INTERVAL_DAYS * i * 24 * 3600 * 1000);
			}
		}
		return info;
	}

	public static MainTownManager getInstance() {
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final MainTownManager instance = new MainTownManager();
	}
}
