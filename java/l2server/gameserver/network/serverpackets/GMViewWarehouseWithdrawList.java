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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * Sdh(h dddhh [dhhh] d)
 * Sdh ddddd ddddd ddddd ddddd
 *
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList extends L2ItemListPacket
{
	private L2ItemInstance[] _items;
	private String _playerName;
	private L2PcInstance _activeChar;
	private long _money;

	public GMViewWarehouseWithdrawList(L2PcInstance cha)
	{
		_activeChar = cha;
		_items = _activeChar.getWarehouse().getItems();
		_playerName = _activeChar.getName();
		_money = _activeChar.getWarehouse().getAdena();
	}

	public GMViewWarehouseWithdrawList(L2Clan clan)
	{
		_playerName = clan.getLeaderName();
		_items = clan.getWarehouse().getItems();
		_money = clan.getWarehouse().getAdena();
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_playerName);
		writeQ(_money);
		writeH(_items.length);
		writeD(0x00); // GoD ???

		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
	}
}
