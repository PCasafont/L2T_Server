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
import l2server.log.Log;

/**
 * 0x42 WarehouseWithdrawalList  dh (h dddhh dhhh d)
 *
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public final class WareHouseWithdrawalList extends L2ItemListPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int FREIGHT = 1;

	private L2PcInstance _activeChar;
	private long _playerAdena;
	private L2ItemInstance[] _items;
	private int _whType;

	public WareHouseWithdrawalList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;

		_playerAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
			// Something went wrong!
			Log.warning("error while sending withdraw request to: " + _activeChar.getName());
			return;
		}
		else
		{
			_items = _activeChar.getActiveWarehouse().getItems();
		}

		if (Config.DEBUG)
		{
			for (L2ItemInstance item : _items)
			{
				Log.fine("item:" + item.getItem().getName() + " type1:" + item.getItem().getType1() + " type2:" +
						item.getItem().getType2());
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		/* 0x01-Private Warehouse
         * 0x02-Clan Warehouse
		 * 0x03-Castle Warehouse
		 * 0x04-Warehouse */
		writeH(_whType);
		writeQ(_playerAdena);
		writeH(_items.length);
		writeH(0x00); // GoD ???
		writeD(0x00); // TODO: Amount of already deposited items

		for (L2ItemInstance item : _items)
		{
			writeItem(item);

			writeD(item.getObjectId());

			writeD(0x00); // GoD ???
			writeD(0x00); // GoD ???
		}
	}
}
