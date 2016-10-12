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

import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;

/**
 * @author Erlandys
 */
public class ExMenteeSearch extends L2GameServerPacket
{
	ArrayList<L2PcInstance> _mentees;
	int _page, _playersInPage;

	public ExMenteeSearch(int page, int minLevel, int maxLevel)
	{
		_mentees = new ArrayList<>();
		_page = page;
		_playersInPage = 64;
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (player.getSubClasses().isEmpty() && player.getLevel() >= minLevel && player.getLevel() <= maxLevel)
			{
				_mentees.add(player);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_page);
		if (!_mentees.isEmpty())
		{
			writeD(_mentees.size());
			writeD(_mentees.size() % _playersInPage);
			int i = 1;
			for (L2PcInstance player : _mentees)
			{
				if (i <= _playersInPage * _page && i > _playersInPage * (_page - 1))
				{
					writeS(player.getName());
					writeD(player.getClassId());
					writeD(player.getLevel());
				}
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}
}
