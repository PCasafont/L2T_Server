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

package l2server.gameserver.ai;

import l2server.gameserver.model.L2CharPosition;
import l2server.gameserver.model.actor.instance.L2AirShipInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ExMoveToLocationAirShip;
import l2server.gameserver.network.serverpackets.ExStopMoveAirShip;

/**
 * @author DS
 */
public class L2AirShipAI extends L2VehicleAI
{
	public L2AirShipAI(L2AirShipInstance creature)
	{
		super(creature);
	}

	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!actor.isMovementDisabled())
		{
			clientMoving = true;
			actor.moveToLocation(x, y, z, 0);
			actor.broadcastPacket(new ExMoveToLocationAirShip(getActor()));
		}
	}

	@Override
	protected void clientStopMoving(L2CharPosition pos)
	{
		if (actor.isMoving())
		{
			actor.stopMove(pos);
		}

		if (clientMoving || pos != null)
		{
			clientMoving = false;
			actor.broadcastPacket(new ExStopMoveAirShip(getActor()));
		}
	}

	@Override
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (clientMoving)
		{
			player.sendPacket(new ExMoveToLocationAirShip(getActor()));
		}
	}

	@Override
	public L2AirShipInstance getActor()
	{
		return (L2AirShipInstance) actor;
	}
}
