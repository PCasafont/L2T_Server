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
import l2server.gameserver.model.actor.instance.L2PetInstance;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class PetItemList extends L2ItemListPacket
{

	private L2PetInstance _activeChar;

	public PetItemList(L2PetInstance character)
	{
		_activeChar = character;
		if (Config.DEBUG)
		{
			L2ItemInstance[] items = _activeChar.getInventory().getItems();
			for (L2ItemInstance temp : items)
			{
				Log.fine("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" +
						temp.getItem().getType2());
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		L2ItemInstance[] items = _activeChar.getInventory().getItems();
		int count = items.length;
		writeH(count);

		for (L2ItemInstance item : items)
		{
			writeItem(item);
		}
	}
}
