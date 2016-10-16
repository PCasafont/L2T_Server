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

import java.util.Map;

/**
 * Format: ch ddd [ddd]
 *
 * @author KenM
 */
public class ExGetBossRecord extends L2GameServerPacket
{
	private Map<Integer, Integer> bossRecordInfo;
	private int ranking;
	private int totalPoints;

	public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list)
	{
		this.ranking = ranking;
		totalPoints = totalScore;
		bossRecordInfo = list;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(ranking);
		writeD(totalPoints);
		if (bossRecordInfo == null)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(bossRecordInfo.size()); //list size
			for (int bossId : bossRecordInfo.keySet())
			{
				writeD(bossId);
				writeD(bossRecordInfo.get(bossId));
				writeD(0x00); //??
			}
		}
	}
}
