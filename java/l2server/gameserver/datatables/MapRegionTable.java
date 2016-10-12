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
import l2server.gameserver.events.MonsterInvasion;
import l2server.gameserver.instancemanager.*;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.L2Npc;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.entity.ClanHall;
import l2server.gameserver.model.entity.Fort;
import l2server.gameserver.model.entity.Instance;
import l2server.gameserver.model.zone.type.L2ArenaZone;
import l2server.gameserver.model.zone.type.L2ClanHallZone;
import l2server.log.Log;

import java.io.*;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * This class ...
 */
public class MapRegionTable
{

	private final int[][] _regions = new int[16][18];

	public enum TeleportWhereType
	{
		Castle, ClanHall, SiegeFlag, Town, Fortress
	}

	public static MapRegionTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private MapRegionTable()
	{
		LineNumberReader lnr = null;
		try
		{
			File data = new File(Config.DATAPACK_ROOT, Config.DATA_FOLDER + "mapregion.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(data)));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}

				if (line.indexOf("#") > 0)
				{
					line = line.substring(0, line.indexOf("#"));
				}

				StringTokenizer st = new StringTokenizer(line, ",");
				int region = Integer.parseInt(st.nextToken().trim());
				for (int j = 0; j < 16; j++)
				{
					_regions[j][region] = Integer.parseInt(st.nextToken().trim());
					//Log.fine(j+","+region+" -> "+rset.getInt(j+2));
				}
			}
		}
		catch (FileNotFoundException e)
		{
			Log.warning("mapregion.csv is missing in data folder");
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Error while loading MapRegion table " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public final int getMapRegion(int posX, int posY)
	{
		try
		{
			return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// Position sent is outside MapRegionTable area.
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "MapRegionTable: Player outside map regions at X,Y=" + posX + "," + posY, e);
			}
			return 0;
		}
	}

	public final int getMapRegionX(int posX)
	{
		return (posX >> 15) + 9;// + centerTileX;
	}

	public final int getMapRegionY(int posY)
	{
		return (posY >> 15) + 10;// + centerTileX;
	}

	public int getAreaCastle(L2Character activeChar)
	{
		int area = getClosestTownNumber(activeChar);
		int castle;
		switch (area)
		{
			case 0:
				castle = 1;
				break;//Talking Island Village
			case 1:
				castle = 4;
				break; //Elven Village
			case 2:
				castle = 4;
				break; //Dark Elven Village
			case 3:
				castle = 9;
				break; //Orc Village
			case 4:
				castle = 9;
				break; //Dwarven Village
			case 5:
				castle = 1;
				break; //Town of Gludio
			case 6:
				castle = 1;
				break; //Gludin Village
			case 7:
				castle = 2;
				break; //Town of Dion
			case 8:
				castle = 3;
				break; //Town of Giran
			case 9:
				castle = 4;
				break; //Town of Oren
			case 10:
				castle = 5;
				break; //Town of Aden
			case 11:
				castle = 5;
				break; //Hunters Village
			case 12:
				castle = 3;
				break; //Giran Harbor
			case 13:
				castle = 6;
				break; //Heine
			case 14:
				castle = 8;
				break; //Rune Township
			case 15:
				castle = 7;
				break; //Town of Goddard
			case 16:
				castle = 9;
				break; //Town of Shuttgart
			case 17:
				castle = 2;
				break; //Floran Village
			case 18:
				castle = 8;
				break; //Primeval Isle Wharf
			case 19:
				castle = 5;
				break; //Kamael Village
			case 20:
				castle = 6;
				break; //South of Wastelands Camp
			case 21:
				castle = 8;
				break; //Fantasy Island
			default:
				castle = 5;
				break; //Town of Aden
		}
		return castle;
	}

	public int getClosestTownNumber(L2Character activeChar)
	{
		return getMapRegion(activeChar.getX(), activeChar.getY());
	}

	/**
	 * Get town name by character position
	 *
	 * @param activeChar
	 * @return String
	 */
	public String getClosestTownName(L2Character activeChar)
	{
		return getTownName(getMapRegion(activeChar.getX(), activeChar.getY()));
	}

	/**
	 * Get town name by town id
	 *
	 * @param townId
	 * @return String
	 */
	public String getTownName(int townId)
	{
		String nearestTown = null;
		switch (townId)
		{
			case 0:
				nearestTown = "Talking Island Village";
				break;
			case 1:
				nearestTown = "Elven Village";
				break;
			case 2:
				nearestTown = "Dark Elven Village";
				break;
			case 3:
				nearestTown = "Orc Village";
				break;
			case 4:
				nearestTown = "Dwarven Village";
				break;
			case 5:
				nearestTown = "Town of Gludio";
				break;
			case 6:
				nearestTown = "Gludin Village";
				break;
			case 7:
				nearestTown = "Town of Dion";
				break;
			case 8:
				nearestTown = "Town of Giran";
				break;
			case 9:
				nearestTown = "Town of Oren";
				break;
			case 10:
				nearestTown = "Town of Aden";
				break;
			case 11:
				nearestTown = "Hunters Village";
				break;
			case 12:
				nearestTown = "Giran Harbor";
				break;
			case 13:
				nearestTown = "Heine";
				break;
			case 14:
				nearestTown = "Rune Township";
				break;
			case 15:
				nearestTown = "Town of Goddard";
				break;
			case 16:
				nearestTown = "Town of Schuttgart";
				break;
			case 18:
				nearestTown = "Primeval Isle";
				break;
			case 19:
				nearestTown = "Kamael Village";
				break;
			case 20:
				nearestTown = "South of Wastelands Camp";
				break;
			case 21:
				nearestTown = "Fantasy Island";
				break;
			case 22:
				nearestTown = "Neutral Zone";
				break;
			case 23:
				nearestTown = "Coliseum";
				break;
			case 24:
				nearestTown = "GM Consultation service";
				break;
			case 25:
				nearestTown = "Dimensional Gap";
				break;
			case 26:
				nearestTown = "Cemetary of the Empire";
				break;
			case 27:
				nearestTown = "Inside the Steel Citadel";
				break;
			case 28:
				nearestTown = "Steel Citadel Resistance";
				break;
			case 29:
				nearestTown = "Inside Kamaloka";
				break;
			case 30:
				nearestTown = "Inside Nia Kamaloka";
				break;
			case 31:
				nearestTown = "Inside Rim Kamaloka";
				break;
			case 32:
				nearestTown = "Keucereus clan association";
				break;
			case 33:
				nearestTown = "Inside the Seed of Infinity";
				break;
			case 34:
				nearestTown = "Outside the Seed of Infinity";
				break;
			case 35:
				nearestTown = "Aerial Cleft";
				break;
			case 36:
				nearestTown = "Ancient City of Arcan";
				break;
			case 37:
				nearestTown = "Gainak";
				break;
			case 38:
				nearestTown = "Faeron Village";
				break;
			default:
				nearestTown = "Town of Aden";
				break;
		}

		return nearestTown;
	}

	public String getClosestTownSimpleName(L2Character activeChar)
	{
		int nearestTownId = getMapRegion(activeChar.getX(), activeChar.getY());
		return getTownSimpleName(nearestTownId);
	}

	public String getTownSimpleName(int townId)
	{
		String nearestTown;
		switch (townId)
		{
			case 0:
				nearestTown = "Talking";
				break;
			case 1:
				nearestTown = "Elven";
				break;
			case 2:
				nearestTown = "Dark Elven";
				break;
			case 3:
				nearestTown = "Orc";
				break;
			case 4:
				nearestTown = "Dwarven";
				break;
			case 5:
				nearestTown = "Gludio";
				break;
			case 6:
				nearestTown = "Gludin";
				break;
			case 7:
				nearestTown = "Dion";
				break;
			case 8:
				nearestTown = "Giran";
				break;
			case 9:
				nearestTown = "Oren";
				break;
			case 10:
				nearestTown = "Aden";
				break;
			case 11:
				nearestTown = "Hunters";
				break;
			case 12:
				nearestTown = "Giran Harbor";
				break;
			case 13:
				nearestTown = "Heine";
				break;
			case 14:
				nearestTown = "Rune";
				break;
			case 15:
				nearestTown = "Goddard";
				break;
			case 16:
				nearestTown = "Schuttgart";
				break;
			case 17:
				nearestTown = "Floran";
				break;
			case 18:
				nearestTown = "Primeval";
				break;
			case 19:
				nearestTown = "Kamael";
				break;
			case 20:
				nearestTown = "Wastelands";
				break;
			case 21:
				nearestTown = "Fantasy";
				break;
			case 22:
				nearestTown = "Neutral";
				break;
			case 23:
				nearestTown = "Coliseum";
				break;
			case 24:
				nearestTown = "Jail";
				break;
			case 25:
				nearestTown = "Dimensional Gap";
				break;
			case 26:
				nearestTown = "Cemetary";
				break;
			case 27:
				nearestTown = "Steel Citadel";
				break;
			case 28:
				nearestTown = "Steel Citadel R";
				break;
			case 29:
				nearestTown = "Kamaloka";
				break;
			case 30:
				nearestTown = "Nia Kamaloka";
				break;
			case 31:
				nearestTown = "Rim Kamaloka";
				break;
			case 32:
				nearestTown = "Keucereus";
				break;
			case 33:
				nearestTown = "I. Seed of Infinity";
				break;
			case 34:
				nearestTown = "O. Seed of Infinity";
				break;
			case 35:
				nearestTown = "Aerial Cleft";
				break;
			case 36:
				nearestTown = "Arcan";
				break;
			case 37:
				nearestTown = "Gainak";
				break;
			default:
				nearestTown = "Aden";
				break;
		}

		return nearestTown;
	}

	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
	{
		int[] coord;

		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeChar;

			// If in Monster Derby Track
			if (player.isInsideZone(L2Character.ZONE_MONSTERTRACK))
			{
				return new Location(12661, 181687, -3560);
			}

			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;

			if (player.getClan() != null && !player.isFlyingMounted() &&
					!player.isFlying()) // flying players in gracia cant use teleports to aden continent
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.ClanHall)
				{

					clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
					if (clanhall != null)
					{
						L2ClanHallZone zone = clanhall.getZone();
						if (zone != null && !player.isFlyingMounted())
						{
							return zone.getSpawnLoc();
						}
					}
				}

				// If teleport to castle
				if (teleportWhere == TeleportWhereType.Castle)
				{
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
					// Otherwise check if player is on castle or fortress ground
					// and player's clan is defender
					if (castle == null)
					{
						castle = CastleManager.getInstance().getCastle(player);
						if (!(castle != null && castle.getSiege().getIsInProgress() &&
								castle.getSiege().getDefenderClan(player.getClan()) != null))
						{
							castle = null;
						}
					}

					if (castle != null && castle.getCastleId() > 0)
					{
						return castle.getCastleZone().getSpawnLoc();
					}
				}

				// If teleport to fortress
				if (teleportWhere == TeleportWhereType.Fortress)
				{
					fort = FortManager.getInstance().getFortByOwner(player.getClan());
					// Otherwise check if player is on castle or fortress ground
					// and player's clan is defender
					if (fort == null)
					{
						fort = FortManager.getInstance().getFort(player);
						if (!(fort != null && fort.getSiege().getIsInProgress() &&
								fort.getOwnerClan() == player.getClan()))
						{
							fort = null;
						}
					}

					if (fort != null && fort.getFortId() > 0)
					{
						return fort.getFortZone().getSpawnLoc();
					}
				}

				// If teleport to SiegeHQ
				if (teleportWhere == TeleportWhereType.SiegeFlag)
				{
					castle = CastleManager.getInstance().getCastle(player);
					fort = FortManager.getInstance().getFort(player);

					if (castle != null)
					{
						if (castle.getSiege().getIsInProgress())
						{
							// Check if player's clan is attacker
							List<L2Npc> flags = castle.getSiege().getFlag(player.getClan());
							if (flags != null && !flags.isEmpty())
							{
								// Spawn to flag - Need more work to get player to the nearest flag
								L2Npc flag = flags.get(0);
								return new Location(flag.getX(), flag.getY(), flag.getZ());
							}
						}
					}
					else if (fort != null)
					{
						if (fort.getSiege().getIsInProgress())
						{
							// Check if player's clan is attacker
							List<L2Npc> flags = fort.getSiege().getFlag(player.getClan());
							if (flags != null && !flags.isEmpty())
							{
								// Spawn to flag - Need more work to get player to the nearest flag
								L2Npc flag = flags.get(0);
								return new Location(flag.getX(), flag.getY(), flag.getZ());
							}
						}
					}
				}
			}

			//Karma player land out of city
			if (player.getReputation() < 0)
			{
				try
				{
					return TownManager.getClosestTown(activeChar).getChaoticSpawnLoc();
				}
				catch (Exception e)
				{
					if (player.isFlyingMounted()) // prevent flying players to teleport outside of gracia
					{
						return new Location(-186330, 242944, 2544);
					}
					else
					{
						return new Location(17817, 170079, -3530);
					}
				}
			}

			// Checking if in arena
			L2ArenaZone arena = ZoneManager.getInstance().getArena(player);
			if (arena != null)
			{
				return arena.getSpawnLoc();
			}

			// Checking if in an instance
			if (player.getInstanceId() > 0)
			{
				Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
				if (inst != null)
				{
					coord = inst.getSpawnLoc();
					if (coord[0] != 0 && coord[1] != 0 && coord[2] != 0)
					{
						return new Location(coord[0], coord[1], coord[2]);
					}
				}
			}
			if (MonsterInvasion.getInstance().getAttackedTown() != -1 &&
					TownManager.getClosestTown(activeChar).getTownId() ==
							MonsterInvasion.getInstance().getAttackedTown())
			{
				return TownManager.getClosestTown(activeChar).getChaoticSpawnLoc();
			}
		}

		// Get the nearest town
		try
		{
			return TownManager.getClosestTown(activeChar).getSpawnLoc();
		}
		catch (NullPointerException e)
		{
			// port to the Talking Island if no closest town found
			return new Location(-84176, 243382, -3126);
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MapRegionTable _instance = new MapRegionTable();
	}
}
