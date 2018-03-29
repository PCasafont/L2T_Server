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
	private static List<Integer> bosssIds = new ArrayList<>();
	private static List<AltarsSpawns> spawnInfo = new ArrayList<>();
	private static List<WorldAltarsInfo> altarsList = new ArrayList<>();
	private static int baseRespawnMinutes;
	private static int randomRespawnMinutes;
	private static int cursedChance;

	private class AltarsSpawns
	{
		private String spawnName;
		private int zoneId;
		private Location spawnLoc;
		private boolean isUnderUse;

		private AltarsSpawns(String spawnName, int zoneId, Location spawnLoc)
		{
			this.spawnName = spawnName;
			this.zoneId = zoneId;
			this.spawnLoc = spawnLoc;
			isUnderUse = false;
		}

		private String getSpawnName()
		{
			return spawnName;
		}

		private int getZoneId()
		{
			return zoneId;
		}

		private Location getSpawn()
		{
			return spawnLoc;
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
			isUnderUse = zoneInUse;

			L2SiegeZone zone = ZoneManager.getInstance().getZoneById(getZoneId(), L2SiegeZone.class);
			if (zone != null)
			{
				zone.setIsActive(shouldBePvp);
				zone.updateZoneStatusForCharactersInside();
			}
		}

		private boolean getIsInUse()
		{
			return isUnderUse;
		}
	}

	private class WorldAltarsInfo
	{
		private int altarImageId;
		private String altarName;
		private int altarId;
		private L2Npc altarNpc;
		private L2Npc bossNpc;
		private AltarsSpawns currentSpawn;
		@SuppressWarnings("unused")
		private ScheduledFuture<?> spawnTask;
		private long nextRespawn;
		private boolean isCursed;

		private WorldAltarsInfo(int altarImageId, String altarName, int altarId)
		{
			this.altarImageId = altarImageId;
			this.altarName = altarName;
			this.altarId = altarId;
			respawnAltar();
		}

		private L2Npc getBossNpc()
		{
			return bossNpc;
		}

		private int getAltarImageId()
		{
			return altarImageId;
		}

		private String getAltarName()
		{
			return altarName;
		}

		private int getAltarId()
		{
			return altarId;
		}

		private AltarsSpawns getCurrentSpawn()
		{
			return currentSpawn;
		}

		private boolean isCursed()
		{
			return isCursed;
		}

		private String getNextRespawn()
		{
			if (nextRespawn > System.currentTimeMillis())
			{
				Long remainingTime = (nextRespawn - System.currentTimeMillis()) / 1000;
				int minutes = (int) (remainingTime % 3600 / 60);
				if (minutes >= baseRespawnMinutes)
				{
					return "It will take long to come!";
				}
				else if (minutes < baseRespawnMinutes && minutes >= baseRespawnMinutes / 2)
				{
					return "It will appear in quite a few time!";
				}
				else if (minutes > 1 && minutes < baseRespawnMinutes / 2)
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
			return bossNpc.isInCombat();
		}

		private void respawnAltar()
		{
			isCursed = Rnd.get(100) <= cursedChance;

			if (altarNpc != null)
			{
				altarNpc.deleteMe();
			}

			if (bossNpc != null)
			{
				bossNpc = null;
			}

			if (currentSpawn != null)
			{
				currentSpawn.setInUse(false, false);
			}

			currentSpawn = null;

			int randomSpawnTime = Rnd.get(1, randomRespawnMinutes);
			spawnTask = ThreadPoolManager.getInstance()
					.scheduleGeneral(new AltarRespawn(), (baseRespawnMinutes + randomSpawnTime) * 60000);
			nextRespawn = System.currentTimeMillis() + (baseRespawnMinutes + randomSpawnTime) * 60000;
		}

		private void spawnBoss()
		{
			int rndBoss = bosssIds.get(Rnd.get(bosssIds.size()));
			bossNpc = NpcUtil.addSpawn(rndBoss, currentSpawn.getSpawn().getX() + Rnd.get(300),
					currentSpawn.getSpawn().getY() + Rnd.get(300), currentSpawn.getSpawn().getZ() + 50,
					currentSpawn.getSpawn().getHeading(), false, 0, true, 0);
			if (isCursed)
			{
				((L2Attackable) bossNpc).setChampion(true);
			}

			altarNpc.broadcastPacket(new Earthquake(altarNpc.getX(), altarNpc.getY(), altarNpc.getZ(), 8, 10));
			altarNpc.broadcastPacket(new MagicSkillUse(altarNpc, altarNpc, 14497, 1, 1, 1, 0));
			altarNpc.broadcastPacket(new ExShowScreenMessage(
					altarNpc.getTemplate().Name + "'s seal has been broken and " + bossNpc.getTemplate().Name +
							" has been liberated!", 5000));
		}

		private class AltarRespawn implements Runnable
		{
			@Override
			public void run()
			{
				synchronized (spawnInfo)
				{
					List<AltarsSpawns> notUsed = new ArrayList<>();
					int pvpZones = 0;
					for (AltarsSpawns spawn : spawnInfo)
					{
						if (!spawn.getIsInUse())
						{
							notUsed.add(spawn);
						}
						else
						{
							if (spawn.isZoneActive())
							{
								pvpZones++;
							}
						}
					}

					AltarsSpawns randomSpawn = notUsed.get(Rnd.get(notUsed.size()));
					if (randomSpawn != null)
					{
						currentSpawn = randomSpawn;
						currentSpawn.setInUse(true, pvpZones < 2);
						altarNpc = NpcUtil.addSpawn(altarId, currentSpawn.getSpawn().getX(),
								currentSpawn.getSpawn().getY(), currentSpawn.getSpawn().getZ(),
								currentSpawn.getSpawn().getHeading(), false, 0, true, 0);

						Announcements.getInstance().announceToAll(
								"World Altars: The altar " + altarNpc.getName() + " has respawned in " +
										currentSpawn.getSpawnName() + "!");
					}
				}
			}
		}
	}

	public void notifyBossKilled(L2Npc npc)
	{
		synchronized (altarsList)
		{
			for (WorldAltarsInfo altar : altarsList)
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
		synchronized (altarsList)
		{
			for (WorldAltarsInfo altar : altarsList)
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
		for (WorldAltarsInfo i : altarsList)
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

			sb.append("<tr><td FIXWIDTH=150>Altar Respawn:</td><td FIXWIDTH=300> " + baseRespawnMinutes +
					" minute(s) + " + randomRespawnMinutes + " random.</td></tr>");
			sb.append("<tr><td>Next Respawn in:</td><td> " + i.getNextRespawn() + "</td></tr>");
			sb.append("</table></td></tr></table>");
		}
		return sb.toString();
	}

	private void load()
	{
		File file = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "scripts/ai/WorldAltars/WorldAltars.xml");
		XmlDocument doc = new XmlDocument(file);
		for (XmlNode d : doc.getChildren())
        {
            if (d.getName().equalsIgnoreCase("respawnInfo"))
            {
                baseRespawnMinutes = d.getInt("baseRespawnMinutes");
                randomRespawnMinutes = d.getInt("randomRespawnMinutes");
                cursedChance = d.getInt("cursedChance");
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
                        spawnInfo.add(new AltarsSpawns(name, zoneId, new Location(x, y, z, heading)));
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
                        altarsList.add(new WorldAltarsInfo(imageId, name, altarId));
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
                        bosssIds.add(bossId);
                    }
                }
            }
        }

		Log.info("WorldAltars: Loaded: " + spawnInfo.size() + " altar spawns, " + altarsList.size() + " altars and " +
				bosssIds.size() + " bosses!");
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
		return SingletonHolder.instance;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CustomWorldAltars instance = new CustomWorldAltars();
	}
}
