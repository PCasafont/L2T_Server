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

/**
 *
 *
 *	sample
 *	0000: 85 00 00 00 00 f0 1a 00 00
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class SetupGauge extends L2GameServerPacket
{
	private static final String _S__85_SETUPGAUGE = "[S] 6b SetupGauge";
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
		writeC(0x6b);
		writeD(_charObjId);
		writeD(_color);
		writeD(_time);
		writeD(_time2);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__85_SETUPGAUGE;
	}
	
	@Override
	public void runImpl()
	{
		_charObjId = getClient().getActiveChar().getObjectId();
	}
}
