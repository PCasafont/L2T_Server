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

package l2server.gameserver.model.actor.instance;

import l2server.gameserver.ai.L2AirShipAI;
import l2server.gameserver.instancemanager.AirShipManager;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Vehicle;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.templates.chars.L2CharTemplate;
import l2server.util.Point3D;

/**
 * Flying airships. Very similar to Maktakien boats (see L2BoatInstance) but these do fly :P
 *
 * @author DrHouse, reworked by DS
 */
public class L2AirShipInstance extends L2Vehicle
{
	public L2AirShipInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2AirShipInstance);
		setAI(new L2AirShipAI(new AIAccessor()));
	}

	@Override
	public boolean isAirShip()
	{
		return true;
	}

	public boolean isOwner(L2PcInstance player)
	{
		return false;
	}

	public int getOwnerId()
	{
		return 0;
	}

	public boolean isCaptain(L2PcInstance player)
	{
		return false;
	}

	public int getCaptainId()
	{
		return 0;
	}

	public int getHelmObjectId()
	{
		return 0;
	}

	public int getHelmItemId()
	{
		return 0;
	}

	public boolean setCaptain(L2PcInstance player)
	{
		return false;
	}

	public int getFuel()
	{
		return 0;
	}

	public void setFuel(int f)
	{

	}

	public int getMaxFuel()
	{
		return 0;
	}

	public void setMaxFuel(int mf)
	{

	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		final boolean result = super.moveToNextRoutePoint();
		if (result)
		{
			broadcastPacket(new ExMoveToLocationAirShip(this));
		}

		return result;
	}

	@Override
	public boolean addPassenger(L2PcInstance player)
	{
		if (!super.addPassenger(player))
		{
			return false;
		}

		player.setVehicle(this);
		player.setInVehiclePosition(new Point3D(0, 0, 0));
		player.broadcastPacket(new ExGetOnAirShip(player, this));
		player.getKnownList().removeAllKnownObjects();
		player.setXYZ(getX(), getY(), getZ());
		player.revalidateZone(true);
		return true;
	}

	@Override
	public void oustPlayer(L2PcInstance player)
	{
		super.oustPlayer(player);
		final Location loc = getOustLoc();
		if (player.isOnline())
		{
			player.broadcastPacket(new ExGetOffAirShip(player, this, loc.getX(), loc.getY(), loc.getZ()));
			player.getKnownList().removeAllKnownObjects();
			player.setXYZ(loc.getX(), loc.getY(), loc.getZ());
			player.revalidateZone(true);
		}
		else
		{
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
		}
	}

	@Override
	public void deleteMe()
	{
		super.deleteMe();
		AirShipManager.getInstance().removeAirShip(this);
	}

	@Override
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		super.stopMove(pos, updateKnownObjects);

		broadcastPacket(new ExStopMoveAirShip(this));
	}

	@Override
	public void updateAbnormalEffect()
	{
		broadcastPacket(new ExAirShipInfo(this));
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExAirShipInfo(this));
	}
}
