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
package l2tserver.gameserver.network.serverpackets;

import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.util.Point3D;

public class ExMoveToLocationInAirShip extends L2GameServerPacket
{
	private int _charObjId;
	private int _airShipId;
	private Point3D _destination;
	private int _heading;
	
	/**
	 * @param actor
	 * @param destination
	 * @param origin
	 */
	public ExMoveToLocationInAirShip(L2PcInstance player)
	{
		_charObjId = player.getObjectId();
		_airShipId = player.getAirShip().getObjectId();
		_destination = player.getInVehiclePosition();
		_heading = player.getHeading();
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6e);
		writeD(_charObjId);
		writeD(_airShipId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_heading);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] 6D MoveToLocationInAirShip";
	}
}