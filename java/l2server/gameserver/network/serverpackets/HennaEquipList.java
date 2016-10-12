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

import java.util.List;

public class HennaEquipList extends L2GameServerPacket
{

	private L2PcInstance _player;
	private List<L2Henna> _hennaEquipList;

	public HennaEquipList(L2PcInstance player, List<L2Henna> hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(_player.getAdena()); //activeChar current amount of adena
		writeD(4); //available equip slot
		writeD(_hennaEquipList.size());

		for (L2Henna temp : _hennaEquipList)
		{
			// Player must have at least one dye in inventory
			// to be able to see the henna that can be applied with it.
			if (_player.getInventory().getItemByItemId(temp.getDyeId()) != null)
			{
				writeD(temp.getSymbolId()); //symbolId
				writeD(temp.getDyeId()); //itemId of dye
				writeQ(temp.getAmountDyeRequire()); //amount of dye require
				writeQ(temp.getPrice()); //amount of adena required
				writeD(1); //meet the requirement or not
				writeD(0x00);
			}
			else
			{
				writeD(0x00);
				writeD(0x00);
				writeQ(0x00);
				writeQ(0x00);
				writeD(0x00);
				writeD(0x00);
			}
		}
	}
}
