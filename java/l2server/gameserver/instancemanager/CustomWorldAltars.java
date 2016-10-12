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
import l2server.gameserver.ThreadPoolManager;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Attackable;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.zone.type.L2SiegeZone;
import l2server.gameserver.network.serverpackets.Earthquake;
import l2server.gameserver.network.serverpackets.ExShowScreenMessage;
import l2server.gameserver.network.serverpackets.MagicSkillUse;
import l2server.gameserver.util.NpcUtil;
import l2server.log.Log;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author LasTravel
 */

public class CustomWorldAltars
{
	private static List<Integer> _bosssIds = new ArrayList<>();
	private static List<AltarsSpawns> _spawnInfo = new ArrayList<>();
	private static List<WorldAltarsInfo> _altarsList = new ArrayList<>();
	private static int _baseRespawnMinutes;
	private static int _randomRespawnMinutes;
	private static int _cursedChance;

	private class AltarsSpawns
	{
		private String _spawnName;
		private int _zoneId;
		private Location _spawnLoc;
		private boolean _isUnderUse;

		private AltarsSpawns(String spawnName, int zoneId, Location spawnLoc)
		{
			_spawnName = spawnName;
			_zoneId = zoneId;
			_spawnLoc = spawnLoc;
			_isUnderUse = false;
		}

		private String getSpawnName()
		{
			return _spawnName;
		}

		private int getZoneId()
		{
			return _zoneId;
		}

		private Location getSpawn()
		{
			return _spawnLoc;
		}

		private boolean isZoneActive()
		{
			L2SiegeZone zone = ZoneManager.getInstance().getZoneById(getZoneId(), L2SiegeZone.class);
			if (zone != null)
			{
				return zone.isActive();
			}
			return false;
		}

		private void setInUse(boolean zoneInUse, boolean shouldBePvp)
		{
			_isUnderUse = zoneInUse;

			L2SiegeZone zone = ZoneManager.getInstance().getZoneById(getZoneId(), L2SiegeZone.class);
			if (zone != null)
			{
				zone.setIsActive(shouldBePvp);
				zone.updateZoneStatusForCharactersInside();
			}
		}

		private boolean getIsInUse()
		{
			return _isUnderUse;
		}
	}

	private class WorldAltarsInfo
	{
		private int _altarImageId;
		private String _altarName;
		private int _altarId;
		private L2Npc _altarNpc;
		private L2Npc _bossNpc;
		private AltarsSpawns _currentSpawn;
		@SuppressWarnings("unused")
		private ScheduledFuture<?> _spawnTask;
		private long _nextRespawn;
		private boolean _isCursed;

		private WorldAltarsInfo(int altarImageId, String altarName, int altarId)
		{
			_altarImageId = altarImageId;
			_altarName = altarName;
			_altarId = altarId;
			respawnAltar();
		}

		private L2Npc getBossNpc()
		{
			return _bossNpc;
		}

		private int getAltarImageId()
		{
			return _altarImageId;
		}

		private String getAltarName()
		{
			return _altarName;
		}

		private int getAltarId()
		{
			return _altarId;
		}

		private AltarsSpawns getCurrentSpawn()
		{
			return _currentSpawn;
		}

		private boolean isCursed()
		{
			return _isCursed;
		}

		private String getNextRespawn()
		{
			if (_nextRespawn > System.currentTimeMillis())
			{
				Long remainingTime = (_nextRespawn - System.currentTimeMillis()) / 1000;
				int minutes = (int) (remainingTime % 3600 / 60);
				if (minutes >= _baseRespawnMinutes)
				{
					return "It will take long to come!";
				}
				else if (minutes < _baseRespawnMinutes && minutes >= _baseRespawnMinutes / 2)
				{
					return "It will appear in quite a few time!";
				}
				else if (minutes > 1 && minutes < _baseRespawnMinutes / 2)
				{
					return "It will be there soon!";
				}
				else if (minutes < 1)
				{
					return "It's almost there!";
				}
			}

			return "Currently Spawned!";
		}

		private boolean getIsBossUnderAttack()
		{
			return _bossNpc.isInCombat();
		}

		private void respawnAltar()
		{
			_isCursed = Rnd.get(100) <= _cursedChance;

			if (_altarNpc != null)
			{
				_altarNpc.deleteMe();
			}

			if (_bossNpc != null)
			{
				_bossNpc = null;
			}

			if (_currentSpawn != null)
			{
				_currentSpawn.setInUse(false, false);
			}

			_currentSpawn = null;

			int randomSpawnTime = Rnd.get(1, _randomRespawnMinutes);
			_spawnTask = ThreadPoolManager.getInstance()
					.scheduleGeneral(new AltarRespawn(), (_baseRespawnMinutes + randomSpawnTime) * 60000);
			_nextRespawn = System.currentTimeMillis() + (_baseRespawnMinutes + randomSpawnTime) * 60000;
		}

		private void spawnBoss()
		{
			int rndBoss = _bosssIds.get(Rnd.get(_bosssIds.size()));
			_bossNpc = NpcUtil.addSpawn(rndBoss, _currentSpawn.getSpawn().getX() + Rnd.get(300),
					_currentSpawn.getSpawn().getY() + Rnd.get(300), _currentSpawn.getSpawn().getZ() + 50,
					_currentSpawn.getSpawn().getHeading(), false, 0, true, 0);
			if (_isCursed)
			{
				((L2Attackable) _bossNpc).setChampion(true);
			}

			_altarNpc.broadcastPacket(new Earthquake(_altarNpc.getX(), _altarNpc.getY(), _altarNpc.getZ(), 8, 10));
			_altarNpc.broadcastPacket(new MagicSkillUse(_altarNpc, _altarNpc, 14497, 1, 1, 1, 0));
			_altarNpc.broadcastPacket(new ExShowScreenMessage(
					_altarNpc.getTemplate().Name + "'s seal has been broken and " + _bossNpc.getTemplate().Name +
							" has been liberated!", 5000));
		}

		private class AltarRespawn implements Runnable
		{
			@Override
			public void run()
			{
				synchronized (_spawnInfo)
				{
					List<AltarsSpawns> _notUsed = new ArrayList<>();
					int pvpZones = 0;
					for (AltarsSpawns spawn : _spawnInfo)
					{
						if (!spawn.getIsInUse())
						{
							_notUsed.add(spawn);
						}
						else
						{
							if (spawn.isZoneActive())
							{
								pvpZones++;
							}
						}
					}

					AltarsSpawns randomSpawn = _notUsed.get(Rnd.get(_notUsed.size()));
					if (randomSpawn != null)
					{
						_currentSpawn = randomSpawn;
						_currentSpawn.setInUse(true, pvpZones < 2);
						_altarNpc = NpcUtil.addSpawn(_altarId, _currentSpawn.getSpawn().getX(),
								_currentSpawn.getSpawn().getY(), _currentSpawn.getSpawn().getZ(),
								_currentSpawn.getSpawn().getHeading(), false, 0, true, 0);

						Announcements.getInstance().announceToAll(
								"World Altars: The altar " + _altarNpc.getName() + " has respawned in " +
										_currentSpawn.getSpawnName() + "!");
					}
				}
			}
		}
	}

	public void notifyBossKilled(L2Npc npc)
	{
		synchronized (_altarsList)
		{
			for (WorldAltarsInfo altar : _altarsList)
			{
				if (altar.getBossNpc() == null)
				{
					continue;
				}

				if (altar.getBossNpc() == npc)
				{
					altar.respawnAltar();
					break;
				}
			}
		}
	}

	public boolean notifyTrySpawnBosss(L2Npc npc)
	{
		synchronized (_altarsList)
		{
			for (WorldAltarsInfo altar : _altarsList)
			{
				if (altar.getAltarId() == npc.getNpcId())
				{
					//Be sure there are no boss spawned
					if (altar.getBossNpc() == null)
					{
						altar.spawnBoss();
						return true;
					}
				}
			}
			return false;
		}
	}

	public String getAltarsInfo(boolean isGM)
	{
		StringBuilder sb = new StringBuilder();
		for (WorldAltarsInfo i : _altarsList)
		{
			sb.append("<table width=710 border=0 bgcolor=" + (i.isCursed() ? "FF6F79" : "999999") +
					"><tr><td align=center FIXWIDTH=710>" + i.getAltarName() + "</td></tr></table>");
			sb.append("<table width=710 height=150 border=0><tr><td><table><tr><td><img src=\"Crest.pledge_crest_" +
					Config.SERVER_ID + "_" + i.getAltarImageId() + "\" width=256 height=128></td></tr></table></td>");

			sb.append("<td FIXWIDTH=450><table width=450 border=0>");
			if (i.getCurrentSpawn() != null)
			{
				sb.append("<tr><td>Spawn Location:</td><td><a action=\"" +
						(isGM ? "bypass -h admin_move_to " : "bypass _bbscustom;action;showRadar; ") + "" +
						i.getCurrentSpawn().getSpawn().getX() + " " + i.getCurrentSpawn().getSpawn().getY() + " " +
						i.getCurrentSpawn().getSpawn().getZ() + "\"> " + i.getCurrentSpawn().getSpawnName() +
						"</a></td></tr>");
				sb.append("<tr><td>Is Cursed:</td><td>" + (i.isCursed() ? "Yes" : "No") + "</td></tr>");
				sb.append("<tr><td>Is pvp area:</td><td>" + (i.getCurrentSpawn().isZoneActive() ? "Yes" : "No") +
						"</td></tr>");
			}
			else
			{
				sb.append("<tr><td>Spawn Location:</td><td>Not Spawned Yet</td></tr>");
				sb.append("<tr><td>Is Cursed:</td><td>" + (i.isCursed() ? "Yes" : "No") + "</td></tr>");
			}

			if (i.getBossNpc() != null && !i.getBossNpc().isDead())
			{
				sb.append("<tr><td>Spawned Boss:</td><td> " + i.getBossNpc().getTemplate().Name + "</td></tr>");
				if (i.getIsBossUnderAttack())
				{
					sb.append("<tr><td>Status</td><td>Under Attack!</td></tr>");
				}
				else
				{
					sb.append("<tr><td>Status</td><td>Awaiting!</td></tr>");
				}
			}
			else
			{
				sb.append("<tr><td>Spawned Boss:</td><td>None</td></tr>");
			}

			sb.append("<tr><td FIXWIDTH=150>Altar Respawn:</td><td FIXWIDTH=300> " + _baseRespawnMinutes +
					" minute(s) + " + _randomRespawnMinutes + " random.</td></tr>");
			sb.append("<tr><td>Next Respawn in:</td><td> " + i.getNextRespawn() + "</td></tr>");
			sb.append("</table></td></tr></table>");
		}
		return sb.toString();
	}

	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "scripts/ai/WorldAltars/WorldAltars.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode n : doc.getChildren())
		{
			if (n.getName().equalsIgnoreCase("list"))
			{
				for (XmlNode d : n.getChildren())
				{
					if (d.getName().equalsIgnoreCase("respawnInfo"))
					{
						_baseRespawnMinutes = d.getInt("baseRespawnMinutes");
						_randomRespawnMinutes = d.getInt("randomRespawnMinutes");
						_cursedChance = d.getInt("cursedChance");
					}
					else if (d.getName().equalsIgnoreCase("possibleSpawns"))
					{
						for (XmlNode a : d.getChildren())
						{
							if (a.getName().equalsIgnoreCase("spawn"))
							{
								String name = a.getString("name");
								int zoneId = a.getInt("zoneId");
								int x = a.getInt("x");
								int y = a.getInt("y");
								int z = a.getInt("z");
								int heading = a.getInt("heading");
								_spawnInfo.add(new AltarsSpawns(name, zoneId, new Location(x, y, z, heading)));
							}
						}
					}
					else if (d.getName().equalsIgnoreCase("altars"))
					{
						for (XmlNode a : d.getChildren())
						{
							if (a.getName().equalsIgnoreCase("altar"))
							{
								String name = a.getString("name");
								int imageId = a.getInt("imageId");
								int altarId = a.getInt("altarId");
								_altarsList.add(new WorldAltarsInfo(imageId, name, altarId));
							}
						}
					}
					else if (d.getName().equalsIgnoreCase("bosses"))
					{
						for (XmlNode a : d.getChildren())
						{
							if (a.getName().equalsIgnoreCase("boss"))
							{
								int bossId = a.getInt("id");
								_bosssIds.add(bossId);
							}
						}
					}
				}
			}
		}

		Log.info("WorldAltars: Loaded: " + _spawnInfo.size() + " altar spawns, " + _altarsList.size() + " altars and " +
				_bosssIds.size() + " bosses!");
	}

	private CustomWorldAltars()
	{
		if (Config.ENABLE_WORLD_ALTARS)
		{
			load();
		}
	}

	public static CustomWorldAltars getInstance()
	{
		return SingletonHolder._instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CustomWorldAltars _instance = new CustomWorldAltars();
	}
}
