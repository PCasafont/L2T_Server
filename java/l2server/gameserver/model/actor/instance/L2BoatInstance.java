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

import l2server.gameserver.ai.L2BoatAI;
import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.Location;
import l2server.gameserver.model.actor.L2Vehicle;
import l2server.gameserver.network.serverpackets.VehicleDeparture;
import l2server.gameserver.network.serverpackets.VehicleInfo;
import l2server.gameserver.network.serverpackets.VehicleStarted;
import l2server.gameserver.templates.chars.L2CharTemplate;

/**
 * @author Maktakien, reworked by DS
 */
public class L2BoatInstance extends L2Vehicle
{
	public L2BoatInstance(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2BoatInstance);
		setAI(new L2BoatAI(new AIAccessor()));
	}

	@Override
	public boolean isBoat()
	{
		return true;
	}

	@Override
	public boolean moveToNextRoutePoint()
	{
		final boolean result = super.moveToNextRoutePoint();
		if (result)
		{
			broadcastPacket(new VehicleDeparture(this));
		}

		return result;
	}

	@Override
	public void oustPlayer(L2PcInstance player)
	{
		super.oustPlayer(player);

		final Location loc = getOustLoc();
		if (player.isOnline())
		{
			player.teleToLocation(loc.getX(), loc.getY(), loc.getZ());
		}
		else
		{
			player.setXYZInvisible(loc.getX(), loc.getY(), loc.getZ()); // disconnects handling
		}
	}

	@Override
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		super.stopMove(pos, updateKnownObjects);

		broadcastPacket(new VehicleStarted(this, 0));
		broadcastPacket(new VehicleInfo(this));
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new VehicleInfo(this));
	}
}
