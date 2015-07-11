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

import java.util.ArrayList;

import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author Erlandys
 */
public class ExChangeAttributeItemList extends L2ItemListPacket
{
	
	private static final String _S__FE_117_EXCHANGEATTRIBUTEITEMLIST = "[S] FE:117 ExChangeAttributeItemList";
	
	private ArrayList<L2ItemInstance> _itemsList;
	private int _itemOID;

	public ExChangeAttributeItemList(L2PcInstance player, int itemOID)
	{
		_itemsList = new ArrayList<L2ItemInstance>();
		for (L2ItemInstance item : player.getInventory().getItems())
			if (item.isWeapon())
				if (item.getAttackElementPower() > 0)
					_itemsList.add(item);
		_itemOID = itemOID;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x118);
		writeD(_itemOID);
		writeD(_itemsList.size());
		for(L2ItemInstance item : _itemsList)
			writeItem(item);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_117_EXCHANGEATTRIBUTEITEMLIST;
	}
	
}
