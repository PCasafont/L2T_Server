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
import l2server.gameserver.model.actor.instance.L2BoatInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.VehicleDeparture;
import l2server.gameserver.network.serverpackets.VehicleInfo;
import l2server.gameserver.network.serverpackets.VehicleStarted;

/**
 * @author DS
 */
public class L2BoatAI extends L2VehicleAI
{
	public L2BoatAI(L2BoatInstance.AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void moveTo(int x, int y, int z)
	{
		if (!this.actor.isMovementDisabled())
		{
			if (!this.clientMoving)
			{
				this.actor.broadcastPacket(new VehicleStarted(getActor(), 1));
			}

			this.clientMoving = true;
			this.accessor.moveTo(x, y, z);
			this.actor.broadcastPacket(new VehicleDeparture(getActor()));
		}
	}

	@Override
	protected void clientStopMoving(L2CharPosition pos)
	{
		if (this.actor.isMoving())
		{
			this.accessor.stopMove(pos);
		}

		if (this.clientMoving || pos != null)
		{
			this.clientMoving = false;
			this.actor.broadcastPacket(new VehicleStarted(getActor(), 0));
			this.actor.broadcastPacket(new VehicleInfo(getActor()));
		}
	}

	@Override
	public void describeStateToPlayer(L2PcInstance player)
	{
		if (this.clientMoving)
		{
			player.sendPacket(new VehicleDeparture(getActor()));
		}
	}

	@Override
	public L2BoatInstance getActor()
	{
		return (L2BoatInstance) this.actor;
	}
}
