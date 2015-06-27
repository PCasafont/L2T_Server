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

import l2server.log.Log;

/**
 * Format: (c) dddd
 * d: dx
 * d: dy
 * d: dz
 * d: AirShip id ??
 * @author  -Wooden-
 * 
 */
public class ExGetOnAirShip extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _shipId;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_shipId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		Log.info("[T1:ExGetOnAirShip] x: "+_x);
		Log.info("[T1:ExGetOnAirShip] y: "+_y);
		Log.info("[T1:ExGetOnAirShip] z: "+_z);
		Log.info("[T1:ExGetOnAirShip] ship ID: "+_shipId);
	}
	
	@Override
	public String getType()
	{
		return "[C] 0xD0:0x36 ExGetOnAirShip";
	}
	
}
