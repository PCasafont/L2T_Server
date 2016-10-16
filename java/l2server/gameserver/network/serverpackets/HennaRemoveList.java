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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.templates.item.L2Henna;

public class HennaRemoveList extends L2GameServerPacket
{

	private L2PcInstance player;

	public HennaRemoveList(L2PcInstance player)
	{
		this.player = player;
	}

	@SuppressWarnings("unused")
	private int getHennaUsedSlots()
	{
		int slots = 0;
		switch (player.getHennaEmptySlots())
		{
			case 0:
				slots = 3;
				break;
			case 1:
				slots = 2;
				break;
			case 2:
				slots = 1;
				break;
			case 3:
				slots = 0;
				break;
		}

		return slots;
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(player.getAdena());
		writeD(4);
		writeD(4 - player.getHennaEmptySlots());

		for (int i = 0; i <= 4; i++)
		{
			L2Henna henna = player.getHenna(i);
			if (henna != null)
			{
				writeD(henna.getSymbolId());
				writeD(henna.getDyeId());
				writeQ(henna.getAmountDyeRequire() / 2);
				writeQ(henna.getPrice() / 5);
				writeD(0x00);
				writeD(0x00);
			}
		}
	}
}
