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

package l2server.gameserver.model.actor.stat;

import l2server.gameserver.model.actor.instance.L2ControllableAirShipInstance;

public class ControllableAirShipStat extends VehicleStat
{
	public ControllableAirShipStat(L2ControllableAirShipInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2ControllableAirShipInstance getActiveChar()
	{
		return (L2ControllableAirShipInstance) super.getActiveChar();
	}

	@Override
	public float getMoveSpeed()
	{
		if (getActiveChar().isInDock() || getActiveChar().getFuel() > 0)
		{
			return super.getMoveSpeed();
		}
		else
		{
			return super.getMoveSpeed() * 0.05f;
		}
	}
}
