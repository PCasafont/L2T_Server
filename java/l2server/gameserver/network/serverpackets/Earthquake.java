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

/**
 * format   dddddd
 */
public class Earthquake extends L2GameServerPacket
{
	private int x;
	private int y;
	private int z;
	private int intensity;
	private int duration;

	/**
	 * @param
	 */
	public Earthquake(int x, int y, int z, int intensity, int duration)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.intensity = intensity;
		this.duration = duration;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(intensity);
		writeD(duration);
		writeD(0x00); // Unknown
	}
}
