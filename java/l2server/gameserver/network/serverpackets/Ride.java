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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.model.actor.instance.L2PcInstance;

public final class Ride extends L2GameServerPacket
{
	public static final int ACTION_MOUNT = 1;
	public static final int ACTION_DISMOUNT = 0;
	private final int id;
	private final int bRide;
	private final int rideType;
	private final int rideClassID;
	private final int x, y, z;

	public Ride(L2PcInstance cha, boolean mount, int rideClassId)
	{
		id = cha.getObjectId();
		bRide = mount ? 1 : 0;
		rideClassID = rideClassId + 1000000; // npcID

		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();

		switch (rideClassId)
		{
			case 0: // dismount
				rideType = 0;
				break;
			case 12526: // Wind
			case 12527: // Star
			case 12528: // Twilight
			case 16038: // red strider of wind
			case 16039: // red strider of star
			case 16040: // red strider of dusk
			case 16068: // Guardian Strider
				rideType = 1;
				break;
			case 12621: // Wyvern
				rideType = 2;
				break;
			case 16037: // Great Snow Wolf
			case 16041: // Fenrir Wolf
			case 16042: // White Fenrir Wolf
				rideType = 3;
				break;
			case 32: // Jet Bike
			case 13130: // Light Purple Maned Horse
			case 13146: // Tawny-Maned Lion
			case 13147: // Steam Sledge
			case 13314: // Archer Horse
			case 13316: // Cobalt Horse
			case 13317: // Enchanter Horse
			case 13318: // Healer Horse
			case 13311: // Knight Horse
			case 13315: // Phantom Horse
			case 13312: // Warrior Horse
			case 13330: // ClockWork Cucuru
			case 13313: // Rusty Steel Horse
			case 162: //BlackBear?
			case 159: //TamePrincessAnt?
			case 161: //HalloweenWitchsBroomstick?
			case 13340: //Kukurin
			case 13390: //Lyn draco
			case 13391: //Air Bike
				rideType = 4;
				break;
			default:
				throw new IllegalArgumentException("Unsupported mount NpcId: " + rideClassId);
		}
	}

	@Override
	public void runImpl()
	{

	}

	public int getMountType()
	{
		return rideType;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(id);
		writeD(bRide);
		writeD(rideType);
		writeD(rideClassID);
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
