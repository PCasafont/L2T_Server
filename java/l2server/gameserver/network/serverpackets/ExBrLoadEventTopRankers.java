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
 * Halloween rank list server packet.
 * <p>
 * Format: (ch)ddddd
 */
public class ExBrLoadEventTopRankers extends L2GameServerPacket
{
	private int eventId;
	private int day;
	private int count;
	private int bestScore;
	private int myScore;

	public ExBrLoadEventTopRankers(int eventId, int day, int count, int bestScore, int myScore)
	{
		this.eventId = eventId;
		this.day = day;
		this.count = count;
		this.bestScore = bestScore;
		this.myScore = myScore;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(eventId);
		writeD(day);
		writeD(count);
		writeD(bestScore);
		writeD(myScore);
	}
}
