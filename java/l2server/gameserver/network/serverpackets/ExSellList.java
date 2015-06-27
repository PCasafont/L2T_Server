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

import java.util.ArrayList;
import java.util.List;

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2TradeList;
import l2server.gameserver.model.L2TradeList.L2TradeItem;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 * @author ShanSoft
 *
 */
public class ExSellList extends L2ItemListPacket
{
	private static final String _S__B7_ExBuySellListPacket = "[S] B7 ExBuySellListPacket";
	
	private List<L2TradeItem> _buyList = new ArrayList<L2TradeItem>();
	private L2ItemInstance[] _sellList = null;
	private L2ItemInstance[] _refundList = null;
	private boolean _done;
	
	public ExSellList(L2PcInstance player, L2TradeList list, double taxRate, boolean done)
	{
		for (L2TradeItem item : list.getItems())
		{
			if (item.hasLimitedStock() && item.getCurrentCount() <= 0)
				continue;
			_buyList.add(item);
		}
		_sellList = player.getInventory().getAvailableItems(false, true);
		if (player.hasRefund())
			_refundList = player.getRefund().getItems();
		_done = done;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB8);
		writeD(0x01);
		writeD(0x00); // GoD ???
		
		if (_sellList != null && _sellList.length > 0)
		{
			writeH(_sellList.length);
			for (L2ItemInstance item : _sellList)
			{
				writeItem(item);
				
				writeQ(item.getItem().getReferencePrice() / 2);
			}
		}
		else
			writeH(0x00);
		
		if (_refundList != null && _refundList.length > 0)
		{
			writeH(_refundList.length);
			for (L2ItemInstance item : _refundList)
			{
				writeItem(item);
				
				writeD(0); // ???
				writeQ(item.getItem().getReferencePrice() / 2 * item.getCount());
			}
		}
		else
			writeH(0x00);
		
		writeC(_done ? 0x01 : 0x00);
		
		_buyList.clear();
	}
	
	@Override
	public String getType()
	{
		return _S__B7_ExBuySellListPacket;
	}
}