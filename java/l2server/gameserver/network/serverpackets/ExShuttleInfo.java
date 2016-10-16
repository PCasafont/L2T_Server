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

import l2server.gameserver.model.actor.instance.L2ShuttleInstance;
import l2server.gameserver.model.actor.instance.L2ShuttleInstance.ShuttleStop;

/**
 * @author Pere
 */
public class ExShuttleInfo extends L2GameServerPacket
{
	private final L2ShuttleInstance shuttle;

	public ExShuttleInfo(L2ShuttleInstance shuttle)
	{
		this.shuttle = shuttle;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(shuttle.getObjectId());
		writeD(shuttle.getX());
		writeD(shuttle.getY());
		writeD(shuttle.getZ());
		writeD(shuttle.getHeading());
		writeD(shuttle.getId());
		writeD(shuttle.getStops().size());
		for (ShuttleStop door : shuttle.getStops())
		{
			writeD(door.getId());
			writeD(0x00);
			writeD(0x00);
			writeD(shuttle.getId() == 3 ? -115 : -50);//Related with shuttle doors
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(door.getId() == 0 ? 200 : -200);
			writeD(45);
			writeD(door.isDoorOpen() ? 0x01 : 0x00);
			writeD(door.hasDoorChanged() ? 0x01 : 0x00);
		}
	}
}
