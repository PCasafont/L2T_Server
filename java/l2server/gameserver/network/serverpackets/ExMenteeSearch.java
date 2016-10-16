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
	ArrayList<L2PcInstance> mentees;
	int page, playersInPage;

	public ExMenteeSearch(int page, int minLevel, int maxLevel)
	{
		mentees = new ArrayList<>();
		this.page = page;
		playersInPage = 64;
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			if (player.getSubClasses().isEmpty() && player.getLevel() >= minLevel && player.getLevel() <= maxLevel)
			{
				mentees.add(player);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(page);
		if (!mentees.isEmpty())
		{
			writeD(mentees.size());
			writeD(mentees.size() % playersInPage);
			int i = 1;
			for (L2PcInstance player : mentees)
			{
				if (i <= playersInPage * page && i > playersInPage * (page - 1))
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
