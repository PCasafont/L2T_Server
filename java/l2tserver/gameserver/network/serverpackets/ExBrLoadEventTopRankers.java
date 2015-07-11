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
 * Halloween rank list server packet.
 * 
 * Format: (ch)ddddd
 */
public class ExBrLoadEventTopRankers extends L2GameServerPacket
{
	private int _eventId;
	private int _day;
	private int _count;
	private int _bestScore;
	private int _myScore;
	
	public ExBrLoadEventTopRankers(int eventId, int day, int count, int bestScore, int myScore)
	{
		_eventId = eventId;
		_day = day;
		_count = count;
		_bestScore = bestScore;
		_myScore = myScore;
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0xbe);
		writeD(_eventId);
		writeD(_day);
		writeD(_count);
		writeD(_bestScore);
		writeD(_myScore);
		
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:BD ExBrLoadEventTopRankers".intern();
	}
}
