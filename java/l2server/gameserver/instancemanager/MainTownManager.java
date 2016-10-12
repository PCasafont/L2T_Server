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
public class MainTownManager
{
	public class MainTownInfo
	{
		private final int _townId;
		private final String _name;
		private final int _startX;
		private final int _startY;
		private final int _startZ;
		private final int _startRandom;

		public MainTownInfo(int townId, String name, int startX, int startY, int startZ, int startRandom)
		{
			_townId = townId;
			_name = name;
			_startX = startX;
			_startY = startY;
			_startZ = startZ;
			_startRandom = startRandom;
		}

		public int getTownId()
		{
			return _townId;
		}

		public String getName()
		{
			return _name;
		}

		public int getStartX()
		{
			return _startX;
		}

		public int getStartY()
		{
			return _startY;
		}

		public int getStartZ()
		{
			return _startZ;
		}

		public int getStartRandom()
		{
			return _startRandom;
		}

		public void spawnNpcs()
		{
			SpawnTable.getInstance().spawnSpecificTable(_name + "_custom_spawns");
		}

		public void despawnNpcs()
		{
			SpawnTable.getInstance().despawnSpecificTable(_name + "_custom_spawns");
		}

		public List<L2PcInstance> getPlayersInside()
		{
			L2TownZone zone = TownManager.getTown(_townId);
			return zone.getPlayersInside();
		}

		public void teleportPlayers(List<L2PcInstance> players)
		{
			L2DummyZone zone = ZoneManager.getInstance().getZoneByName(_name + "DummyZone", L2DummyZone.class);
			for (L2PcInstance player : players)
			{
				int[] coords = zone.getZone().getRandomPoint();
				player.teleToLocation(coords[0], coords[1],
						GeoData.getInstance().getHeight(coords[0], coords[1], coords[2]));
			}
		}

		public int[] getRandomCoords()
		{
			L2DummyZone zone = ZoneManager.getInstance().getZoneByName(_name + "DummyZone", L2DummyZone.class);
			int[] coords = zone.getZone().getRandomPoint();
			coords[2] = GeoData.getInstance().getHeight(coords[0], coords[1], coords[2]);
			return coords;
		}
	}

	private static final int ROTATION_INTERVAL_DAYS = 2;

	private final List<MainTownInfo> _mainTowns = new ArrayList<>();

	private int _rotationIndex = 0;
	private MainTownInfo _currentMainTown = null;

	private List<MainTownInfo> _nextTowns = new ArrayList<>();
	private long _nextTownTimer = 0L;

	public MainTownManager()
	{
		load();
	}

	private void load()
	{
		if (!Config.isServer(Config.TENKAI_ESTHUS))
		{
			return;
		}

		File file = new File(Config.DATAPACK_ROOT, "/data_" + Config.SERVER_NAME + "/mainTowns.xml");
		if (!file.exists())
		{
			Log.warning("Dir " + file.getAbsolutePath() + " doesn't exist");
			return;
		}

		XmlDocument doc = new XmlDocument(file);
		for (XmlNode townNode : doc.getFirstChild().getChildren())
		{
			if (!townNode.getName().equalsIgnoreCase("town"))
			{
				continue;
			}

			int townId = townNode.getInt("id");
			String name = townNode.getString("name");
			int startX = townNode.getInt("startX");
			int startY = townNode.getInt("startY");
			int startZ = townNode.getInt("startZ");
			int startRandom = townNode.getInt("startRandom");
			_mainTowns.add(new MainTownInfo(townId, name, startX, startY, startZ, startRandom));
		}

		_rotationIndex = 0;
		if (GlobalVariablesManager.getInstance().isVariableStored("MTRotationIndex"))
		{
			_rotationIndex =
					Integer.parseInt(GlobalVariablesManager.getInstance().getStoredVariable("MTRotationIndex"));
		}
		else
		{
			GlobalVariablesManager.getInstance().storeVariable("MTRotationIndex", String.valueOf(_rotationIndex));
		}

		Random random = new Random(79286);
		for (int i = 0; i < _rotationIndex; i++)
		{
			random.nextInt();
		}

		for (int i = 0; i < 1000; i++)
		{
			_nextTowns.add(_mainTowns.get(random.nextInt(_mainTowns.size())));
		}

		_currentMainTown = _nextTowns.remove(0);
		_currentMainTown.spawnNpcs();

		if (GlobalVariablesManager.getInstance().isVariableStored("MTLastRotation"))
		{
			_nextTownTimer = Long.parseLong(GlobalVariablesManager.getInstance().getStoredVariable("MTLastRotation")) +
					ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000;
		}

		if (_nextTownTimer - System.currentTimeMillis() < 0)
		{
			if (_nextTownTimer > 0)
			{
				while (_nextTownTimer - System.currentTimeMillis() < 0)
				{
					changeMainTown();
					_nextTownTimer += ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000;
				}
			}
			else
			{
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY, 6);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				GlobalVariablesManager.getInstance()
						.storeVariable("MTLastRotation", String.valueOf(cal.getTimeInMillis()));

				cal.roll(Calendar.DAY_OF_YEAR, ROTATION_INTERVAL_DAYS);
				_nextTownTimer = cal.getTimeInMillis();
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			changeMainTown();
			_nextTownTimer = System.currentTimeMillis() + ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000;
		}, _nextTownTimer - System.currentTimeMillis(), ROTATION_INTERVAL_DAYS * 24 * 3600 * 1000);
		//}, 3600000L * 5, 3600000L * 5);

		Log.info("MainTownManager: Loaded " + _mainTowns.size() + " main towns.");
		Log.info("MainTownManager: The current main town is " + _currentMainTown.getName() + ".");
	}

	private void changeMainTown()
	{
		MainTownInfo nextMainTown = _nextTowns.remove(0);

		_rotationIndex++;
		GlobalVariablesManager.getInstance().storeVariable("MTRotationIndex", String.valueOf(_rotationIndex));
		GlobalVariablesManager.getInstance()
				.storeVariable("MTLastRotation", String.valueOf(System.currentTimeMillis()));

		if (nextMainTown == _currentMainTown)
		{
			return;
		}

		_currentMainTown.despawnNpcs();
		nextMainTown.spawnNpcs();
		nextMainTown.teleportPlayers(_currentMainTown.getPlayersInside());
		_currentMainTown = nextMainTown;

		Announcements.getInstance()
				.announceToAll("The main town has changed! Now it's " + _currentMainTown.getName() + ".");
	}

	public MainTownInfo getCurrentMainTown()
	{
		//for (MainTownInfo inf : _mainTowns)
		//	inf.spawnNpcs();
		return _currentMainTown;
	}

	public String getNextTownsInfo()
	{
		String info = "";
		MainTownInfo prevTown = _currentMainTown;
		Date prevDate = null;
		for (int i = 0; i < 10; i++)
		{
			MainTownInfo town = _nextTowns.get(i);
			if (town != prevTown)
			{
				Date date = new Date(_nextTownTimer + (ROTATION_INTERVAL_DAYS * i - 1) * 24 * 3600 * 1000);
				String from = "now";
				if (prevDate != null)
				{
					from = new SimpleDateFormat("E MMM d").format(prevDate);
				}
				String to = new SimpleDateFormat("E MMM d").format(date);

				info += "- From <font color=LEVEL>" + from + "</font> to <font color=LEVEL>" + to + "</font> " +
						"the main town will be <font color=LEVEL>" + prevTown.getName() + "</font>.<br1>";

				if (i >= 4)
				{
					break;
				}

				prevTown = town;
				prevDate = new Date(_nextTownTimer + ROTATION_INTERVAL_DAYS * i * 24 * 3600 * 1000);
			}
		}
		return info;
	}

	public static MainTownManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MainTownManager _instance = new MainTownManager();
	}
}
