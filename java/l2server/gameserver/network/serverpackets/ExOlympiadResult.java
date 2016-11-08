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

import l2server.Config;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.Olympiad;

/**
 * @author Pere
 */
public class ExOlympiadResult extends L2GameServerPacket
{
	private Object[] _info;

	public ExOlympiadResult(Object[] info)
	{
		_info = info;
	}

	@Override
	protected final void writeImpl()
	{
		writeD((Integer) _info[0] < 0 ? 1 : 0); // Victory or Tie
		writeS((String) _info[1]); // Winner
		for (int i = 0; i < 2; i++)
		{
			writeD(i + 1);
			int multiplier = (Integer) _info[0] == i ? 1 : -1;
			int participants = (Integer) _info[2];
			writeD(participants);
			for (int j = 0; j < participants; j++)
			{
				L2PcInstance player = (L2PcInstance) _info[i * participants + j + 4];
				if (player == null)
				{
					writeS("");
					writeS("");
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
					writeD(0x00);
				}
				else
				{
					writeS(player.getName());
					writeS(player.getClan() == null ? "" : player.getClan().getName());
					writeD(0x00); // ???
					writeD(player.getCurrentClass().getId());
					writeD(player.getOlyGivenDmg());
					writeD(Olympiad.getInstance().getNobleInfo(player.getObjectId()).getPoints());
					writeD((Integer) _info[3] * multiplier);
					writeD(Config.SERVER_ID);
				}
			}
		}
	}
}
