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

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private int _objId;
	private String _storeMsg;

	public PrivateStoreMsgSell(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_storeMsg = player.getSellList().getTitle();
		if (player.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_CUSTOM_SELL)
		{
			_storeMsg = player.getCustomSellList().getTitle();
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objId);
		writeS(_storeMsg);
	}
}
