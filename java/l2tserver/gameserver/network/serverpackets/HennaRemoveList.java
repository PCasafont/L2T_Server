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

import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.gameserver.templates.item.L2Henna;

public class HennaRemoveList extends L2GameServerPacket
{
	private static final String _S__E2_HennaRemoveList = "[S] ee HennaRemoveList";
	
	private L2PcInstance _player;
	
	public HennaRemoveList(L2PcInstance player)
	{
		_player = player;
	}
	
	private final int getHennaUsedSlots()
	{
		int _slots = 0;
		switch (_player.getHennaEmptySlots())
		{
			case 0:
				_slots = 3;
				break;
			case 1:
				_slots = 2;
				break;
			case 2:
				_slots = 1;
				break;
			case 3:
				_slots = 0;
				break;
		}
		
		return _slots;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe6);
		writeQ(_player.getAdena());
		writeD(0x00);
		writeD(getHennaUsedSlots());
		
		for (int i = 1; i <= 4; i++)
		{
			L2Henna henna = _player.getHenna(i);
			if (henna != null)
			{
				writeD(henna.getSymbolId());
				writeD(henna.getDyeId());
				writeD(henna.getAmountDyeRequire() / 2);
				writeD(0x00);
				writeQ(henna.getPrice() / 5);
				writeD(0x01);
				writeD(0x01);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__E2_HennaRemoveList;
	}
}