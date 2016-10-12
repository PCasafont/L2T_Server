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
 * @author Xavi
 */
public class ExLoadStatHotLink extends L2GameServerPacket
{

	private int _pid1;
	private int _pid2;

	public ExLoadStatHotLink(int pid1)
	{
		_pid1 = pid1;
		_pid2 = 0;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		if (getClient().getActiveChar() == null)
		{
			return;
		}

		//Map<Integer, Long> lastMap = MuseumManager.getInstance().getRanking(_pid1, _pid2, true);
		//Map<Integer, Long> overallMap = MuseumManager.getInstance().getRanking(_pid1, _pid2, false);

		writeD(_pid1);
		writeD(_pid2);
		/*writeD(lastMap.size() < 5 ? lastMap.size() : 5);
        int position = 1;
		for (Integer key : lastMap.keySet())
		{
			writeH(position);
			writeD(key);
			writeS(CharNameTable.getInstance().getNameById(key));
			writeQ(lastMap.get(key));
			writeH(0x00); // GoD ???
			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
			if (position == 5)
				break;
			position++;
		}
		writeD(overallMap.size() < 5 ? overallMap.size() : 5);
		position = 1;
		for (Integer key : overallMap.keySet())
		{
			writeH(position);
			writeD(key);
			writeS(CharNameTable.getInstance().getNameById(key));
			writeQ(overallMap.get(key));
			writeH(0x00); // GoD ???
			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
			if (position == 5)
				break;
			position++;
		}*/
	}
}
