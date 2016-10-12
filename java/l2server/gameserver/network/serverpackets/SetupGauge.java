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
 * sample
 * 0000: 85 00 00 00 00 f0 1a 00 00
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE_DUAL = 0;
	public static final int BLUE = 1;
	public static final int BLUE_MINI = 2;
	public static final int GREEN_MINI = 3;
	public static final int REC_MINI = 4;

	private int _color;
	private int _time;
	private int _time2;
	private int _charObjId;

	public SetupGauge(int dat1, int time)
	{
		_color = dat1;
		_time = time;
		_time2 = time;
	}

	public SetupGauge(int color, int currentTime, int maxTime)
	{
		_color = color;
		_time = currentTime;
		_time2 = maxTime;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId);
		writeD(_color);
		writeD(_time);
		writeD(_time2);
	}

	@Override
	public void runImpl()
	{
		_charObjId = getClient().getActiveChar().getObjectId();
	}
}
