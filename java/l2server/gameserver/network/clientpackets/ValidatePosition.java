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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.TaskPriority;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.GetOnVehicle;
import l2server.gameserver.network.serverpackets.ValidateLocation;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends L2GameClientPacket
{

	/**
	 * urgent messages, execute immediately
	 */
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	private int x;
	private int y;
	private int z;
	private int heading;
	private int data; // vehicle id

	@Override
	protected void readImpl()
	{
		this.x = readD();
		this.y = readD();
		this.z = readD();
		this.heading = readD();
		this.data = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isTeleporting() || activeChar.inObserverMode())
		{
			return;
		}

		final int realX = activeChar.getX();
		final int realY = activeChar.getY();
		int realZ = activeChar.getZ();

		if (Config.DEVELOPER)
		{
			Log.fine("client pos: " + this.x + " " + this.y + " " + this.z + " head " + this.heading);
			Log.fine("server pos: " + realX + " " + realY + " " + realZ + " head " + activeChar.getHeading());
		}

		if (this.x == 0 && this.y == 0)
		{
			if (realX != 0) // in this case this seems like a client error
			{
				return;
			}
		}

		int dx, dy, dz;
		double diffSq;

		if (activeChar.isInBoat())
		{
			if (Config.COORD_SYNCHRONIZE == 2)
			{
				dx = this.x - activeChar.getInVehiclePosition().getX();
				dy = this.y - activeChar.getInVehiclePosition().getY();
				dz = this.z - activeChar.getInVehiclePosition().getZ();
				diffSq = dx * dx + dy * dy;
				if (diffSq > 250000)
				{
					sendPacket(new GetOnVehicle(activeChar.getObjectId(), this.data, activeChar.getInVehiclePosition()));
				}
			}
			return;
		}
		if (activeChar.isInAirShip())
		{
			/*if (Config.COORD_SYNCHRONIZE == 2)
            {
				dx = this.x - activeChar.getInVehiclePosition().getX();
				dy = this.y - activeChar.getInVehiclePosition().getY();
				dz = this.z - activeChar.getInVehiclePosition().getZ();
				diffSq = (dx*dx + dy*dy);
				if (diffSq > 250000)
					sendPacket(new GetOnVehicle(activeChar.getObjectId(), this.data, activeChar.getInBoatPosition()));
			}*/
			return;
		}

		if (activeChar.isFalling(this.z))
		{
			return; // disable validations during fall to avoid "jumping"
		}

		dx = this.x - realX;
		dy = this.y - realY;
		dz = this.z - realZ;
		diffSq = dx * dx + dy * dy;

		/*L2Party party = activeChar.getParty();
		if (party != null && activeChar.getLastPartyPositionDistance(this.x, this.y, this.z) > 150)
		{
			activeChar.setLastPartyPosition(this.x, this.y, this.z);
			party.broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));
		}*/

		if (activeChar.isFlying() || activeChar.isInsideZone(L2Character.ZONE_WATER))
		{
			activeChar.setXYZ(realX, realY, this.z);
			if (diffSq > 90000) // validate packet, may also cause z bounce if close to land
			{
				activeChar.sendPacket(new ValidateLocation(activeChar));
			}
		}
		else if (diffSq < 360000) // if too large, messes observation
		{
			if (Config.COORD_SYNCHRONIZE == -1) // Only Z coordinate synched to server,
			// mainly used when no geodata but can be used also with geodata
			{
				activeChar.setXYZ(realX, realY, this.z);
				return;
			}
			if (Config.COORD_SYNCHRONIZE == 1) // Trusting also client x,y coordinates (should not be used with geodata)
			{
				if (!activeChar.isMoving() ||
						!activeChar.validateMovementHeading(this.heading)) // Heading changed on client = possible obstacle
				{
					// character is not moving, take coordinates from client
					if (diffSq < 2500) // 50*50 - attack won't work fluently if even small differences are corrected
					{
						activeChar.setXYZ(realX, realY, this.z);
					}
					else
					{
						activeChar.setXYZ(this.x, this.y, this.z);
					}
				}
				else
				{
					activeChar.setXYZ(realX, realY, this.z);
				}

				activeChar.setHeading(this.heading);
				return;
			}
			// Sync 2 (or other),
			// intended for geodata. Sends a validation packet to client
			// when too far from server calculated true coordinate.
			// Due to geodata/zone errors, some Z axis checks are made. (maybe a temporary solution)
			// Important: this code part must work together with L2Character.updatePosition
			if (Config.GEODATA > 0 && (diffSq > 40000 || Math.abs(dz) > 100))
			{
				//if ((this.z - activeChar.getClientZ()) < 200 && Math.abs(activeChar.getLastServerPosition().getZ()-realZ) > 70)

				if (Math.abs(dz) > 100 && Math.abs(dz) < 1500 && Math.abs(this.z - activeChar.getClientZ()) < 800)
				{
					activeChar.setXYZ(realX, realY, this.z);
					realZ = this.z;
				}
				else
				{
					if (Config.DEVELOPER)
					{
						Log.info(activeChar.getName() + ": Synchronizing position Server --> Client");
					}

					activeChar.sendPacket(new ValidateLocation(activeChar));
				}
			}
		}

		activeChar.setClientX(this.x);
		activeChar.setClientY(this.y);
		activeChar.setClientZ(this.z);
		activeChar.setClientHeading(this.heading); // No real need to validate heading.
		activeChar.setLastServerPosition(realX, realY, realZ);
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
