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

public class RadarControl extends L2GameServerPacket
{
	private int _showRadar;
	private int _type;
	private int _x;
	private int _y;
	private int _z;

	/**
	 * 0xEB RadarControl		 ddddd
	 */

	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar; // showRader?? 0 = showradar; 1 = delete radar;
		_type = type; // radar type??
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_showRadar);
		writeD(_type); //maybe type
		writeD(_x); //x
		writeD(_y); //y
		writeD(_z); //z
	}
}
