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

import l2tserver.gameserver.model.VehiclePathPoint;

public class ExAirShipTeleportList extends L2GameServerPacket
{
	private static final String _S__FE_9A_EXAIRSHIPTELEPORTLIST = "[S] FE:9A ExAirShipTeleportList";
	
	private int _dockId;
	private VehiclePathPoint[][] _teleports;
	private int[] _fuelConsumption;
	
	public ExAirShipTeleportList(int dockId, VehiclePathPoint[][] teleports, int[] fuelConsumption)
	{
		_dockId = dockId;
		_teleports = teleports;
		_fuelConsumption = fuelConsumption;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x9b);
		
		writeD(_dockId);
		if (_teleports != null)
		{
			writeD(_teleports.length);
			
			VehiclePathPoint[] path;
			VehiclePathPoint dst;
			for (int i = 0; i < _teleports.length; i++)
			{
				writeD(i - 1);
				writeD(_fuelConsumption[i]);
				path = _teleports[i];
				dst = path[path.length - 1];
				writeD(dst.x);
				writeD(dst.y);
				writeD(dst.z);
			}
		}
		else
			writeD(0);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_9A_EXAIRSHIPTELEPORTLIST;
	}
}