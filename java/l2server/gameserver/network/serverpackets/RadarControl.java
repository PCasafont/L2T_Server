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
	private int showRadar;
	private int type;
	private int x;
	private int y;
	private int z;

	/**
	 * 0xEB RadarControl		 ddddd
	 */

	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		this.showRadar = showRadar; // showRader?? 0 = showradar; 1 = delete radar;
		this.type = type; // radar type??
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(showRadar);
		writeD(type); //maybe type
		writeD(x); //x
		writeD(y); //y
		writeD(z); //z
	}
}
