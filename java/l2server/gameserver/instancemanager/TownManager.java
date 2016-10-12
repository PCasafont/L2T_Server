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

import l2server.gameserver.datatables.MapRegionTable;
import l2server.gameserver.events.Curfew;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.model.zone.L2ZoneType;
import l2server.gameserver.model.zone.type.L2TownZone;

public class TownManager
{
	public static L2TownZone getClosestTown(L2Object activeObject)
	{
		if (Curfew.getInstance().getOnlyPeaceTown() != -1 && activeObject instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeObject;
			Castle castle = CastleManager.getInstance().findNearestCastle(player);
			if (!(castle != null && castle.getSiege().getIsInProgress() &&
					(castle.getSiege().checkIsDefender(player.getClan()) ||
							castle.getSiege().checkIsAttacker(player.getClan()))))
			{
				return getTown(Curfew.getInstance().getOnlyPeaceTown());
			}
		}

		if (MainTownManager.getInstance().getCurrentMainTown() != null && activeObject instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) activeObject;
			Castle castle = CastleManager.getInstance().findNearestCastle(player);
			if (!(castle != null && castle.getSiege().getIsInProgress() &&
					(castle.getSiege().checkIsDefender(player.getClan()) ||
							castle.getSiege().checkIsAttacker(player.getClan()))))
			{
				return getTown(MainTownManager.getInstance().getCurrentMainTown().getTownId());
			}
		}

		int mapRegion = MapRegionTable.getInstance()
				.getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY());
		switch (mapRegion)
		{
			case 0:
				return getTown(2); // TI
			case 1:
				return getTown(3); // Elven
			case 2:
				return getTown(1); // DE
			case 3:
				return getTown(4); // Orc
			case 4:
				return getTown(6); // Dwarven
			case 5:
				return getTown(7); // Gludio
			case 6:
				return getTown(5); // Gludin
			case 7:
				return getTown(8); // Dion
			case 8:
				return getTown(9); // Giran
			case 9:
				return getTown(10); // Oren
			case 10:
				return getTown(12); // Aden
			case 11:
				return getTown(11); // HV
			case 12:
				return getTown(9); // Giran Harbour
			case 13:
				return getTown(15); // Heine
			case 14:
				return getTown(14); // Rune
			case 15:
				return getTown(13); // Goddard
			case 16:
				return getTown(17); // Schuttgart
			case 17:
				return getTown(16); // Floran
			case 18:
				return getTown(19); //Primeval Isle
			case 19:
				return getTown(20); //Kamael Village
			case 20:
				return getTown(21); //South of Wastelands Camp
			case 21:
				return getTown(22); //Fantasy Island
			case 22:
				return getTown(23); //Neutral Zone
			case 23:
				return getTown(24);//Coliseum
			case 24:
				return getTown(25);//GM Consultation service
			case 25:
				return getTown(26);//Dimensional Gap
			case 26:
				return getTown(27);//Cemetery of the Empire
			case 27:
				return getTown(28);//inside the Steel Citadel
			case 28:
				return getTown(29);//Steel Citadel Resistance
			case 29:
				return getTown(30);//Inside Kamaloka
			case 30:
				return getTown(31);//Inside Nia Kamaloka
			case 31:
				return getTown(32);//Inside Rim Kamaloka
			case 32:
				return getTown(33);//near the Keucereus clan association location
			case 33:
				return getTown(34);//inside the Seed of Infinity
			case 34:
				return getTown(35);//outside the Seed of Infinity
			case 35:
				return getTown(36);//inside Aerial Cleft
			case 36:
				return getTown(37);//magmeld
			case 37:
				return getTown(38);//gainak
			case 38:
				return getTown(39);//faeron
		}

		return getTown(16); // Default to floran
	}

	public static int getClosestLocation(L2Object activeObject)
	{
		switch (MapRegionTable.getInstance()
				.getMapRegion(activeObject.getPosition().getX(), activeObject.getPosition().getY()))
		{
			case 0:
				return 1; // TI
			case 1:
				return 4; // Elven
			case 2:
				return 3; // DE
			case 3:
				return 9; // Orc
			case 4:
				return 9; // Dwarven
			case 5:
				return 2; // Gludio
			case 6:
				return 2; // Gludin
			case 7:
				return 5; // Dion
			case 8:
				return 6; // Giran
			case 9:
				return 10; // Oren
			case 10:
				return 13; // Aden
			case 11:
				return 11; // Hunters
			case 12:
				return 6; // Giran Harbour
			case 13:
				return 12; // Heine
			case 14:
				return 14; // Rune
			case 15:
				return 15; // Goddard
			case 16:
				return 9; // Schuttgart
				/*
                case 17:
				return getTown(16); // Floran
				case 18:
				return getTown(19); //Primeval Isle
				case 19:
				return getTown(20); //Kamael Village
				case 20:
				return getTown(21); //South of Wastelands Camp
				case 21:
				return getTown(22); //Fantasy Island
				case 22:
				return 7; //Neutral Zone
				case 23:
				return getTown(24);//Coliseum
				case 24:
				return getTown(25);//GM Consultation service
				case 25:
				return getTown(26);//Dimensional Gap
				case 26:
				return getTown(27);//Cemetery of the Empire
				case 27:
				return getTown(28);//inside the Steel Citadel
				case 28:
				return getTown(29);//Steel Citadel Resistance
				case 29:
				return getTown(30);//Inside Kamaloka
				case 30:
				return getTown(31);//Inside Nia Kamaloka
				case 31:
				return getTown(32);//Inside Rim Kamaloka
				case 32:
				return getTown(33);//near the Keucereus clan association location
				case 33:
				return getTown(34);//inside the Seed of Infinity
				case 34:
				return getTown(35);//outside the Seed of Infinity
				case 35:
				return getTown(36);//inside Aerial Cleft
				 */
		}
		return 0;
	}

	public static boolean townHasCastleInSiege(int townId)
	{
		//int[] castleidarray = {0,0,0,0,0,0,0,1,2,3,4,0,5,0,0,6,0};
		int[] castleidarray = {
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				1,
				2,
				3,
				4,
				0,
				5,
				7,
				8,
				6,
				0,
				9,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0
		};
		int castleIndex = castleidarray[townId];

		if (castleIndex > 0)
		{
			Castle castle = CastleManager.getInstance().getCastles()
					.get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().getIsInProgress();
			}
		}
		return false;
	}

	public static boolean townHasCastleInSiege(int x, int y)
	{
		int curtown = MapRegionTable.getInstance().getMapRegion(x, y);
		//int[] castleidarray = {0,0,0,0,0,1,0,2,3,4,5,0,0,6,0,0,0,0};
		int[] castleidarray = {
				0,
				0,
				0,
				0,
				0,
				1,
				0,
				2,
				3,
				4,
				5,
				0,
				0,
				6,
				8,
				7,
				9,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0
		};
		//find an instance of the castle for this town.
		int castleIndex = castleidarray[curtown];
		if (castleIndex > 0)
		{
			Castle castle = CastleManager.getInstance().getCastles()
					.get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
			{
				return castle.getSiege().getIsInProgress();
			}
		}
		return false;
	}

	public static L2TownZone getTown(int townId)
	{
		for (L2TownZone temp : ZoneManager.getInstance().getAllZones(L2TownZone.class))
		{
			if (temp.getTownId() == townId)
			{
				return temp;
			}
		}
		return null;
	}

	/**
	 * Returns the town at that position (if any)
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static L2TownZone getTown(int x, int y, int z)
	{
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(x, y, z))
		{
			if (temp instanceof L2TownZone)
			{
				return (L2TownZone) temp;
			}
		}
		return null;
	}
}
