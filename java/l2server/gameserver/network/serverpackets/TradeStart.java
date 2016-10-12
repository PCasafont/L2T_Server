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
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class TradeStart extends L2ItemListPacket
{
	private L2PcInstance _activeChar;
	private L2ItemInstance[] _itemList;

	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
		_itemList = _activeChar.getInventory()
				.getAvailableItems(true, _activeChar.isGM() && Config.GM_TRADE_RESTRICTED_ITEMS);
	}

	@Override
	protected final void writeImpl()
	{
		if (_activeChar.getActiveTradeList() == null || _activeChar.getActiveTradeList().getPartner() == null)
		{
			return;
		}

		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
		//writeD((_activeChar != null || _activeChar.getTransactionRequester() != null)? _activeChar.getTransactionRequester().getObjectId() : 0);

		writeC(0x00); // Relationship mask
		writeC(_activeChar.getLevel());

		writeH(_itemList.length);
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
		}
	}
}
