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

import l2tserver.Config;
import l2tserver.gameserver.model.L2ItemInstance;
import l2tserver.gameserver.model.actor.instance.L2PcInstance;
import l2tserver.log.Log;

/**
 * 0x53 WareHouseDepositList  dh (h dddhh dhhh d)
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class WareHouseDepositList extends L2ItemListPacket
{
	public static final int PRIVATE = 4;
	public static final int CLAN = 4;
	public static final int CASTLE = 3; //not sure
	
	private static final String _S__53_WAREHOUSEDEPOSITLIST = "[S] 41 WareHouseDepositList";
	private final long _playerAdena;
	private final ArrayList<L2ItemInstance> _items;
	private final int _whType;
	
	public WareHouseDepositList(L2PcInstance player, int type)
	{
		_whType = type;
		_playerAdena = player.getAdena();
		_items = new ArrayList<L2ItemInstance>();
		
		final boolean isPrivate = _whType == PRIVATE;
		for (L2ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate))
		{
			if (temp != null && temp.isDepositable(isPrivate))
				_items.add(temp);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x41);
		/* 0x01-Private Warehouse
		 * 0x02-Clan Warehouse
		 * 0x03-Castle Warehouse
		 * 0x04-Warehouse */
		writeH(_whType);
		writeQ(_playerAdena);
		writeD(0x00); // Already stored items count
		final int count = _items.size();
		if (Config.DEBUG)
			Log.fine("count:" + count);
		//writeH(0x00); // Weird count that we don't care about
		writeH(count);
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
			writeD(item.getObjectId());
		}
		_items.clear();
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__53_WAREHOUSEDEPOSITLIST;
	}
}
