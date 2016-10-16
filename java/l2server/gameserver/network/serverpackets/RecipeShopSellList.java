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

import l2server.gameserver.model.L2ManufactureItem;
import l2server.gameserver.model.L2ManufactureList;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * dddd d(ddd)
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class RecipeShopSellList extends L2GameServerPacket
{

	private L2PcInstance buyer, manufacturer;

	public RecipeShopSellList(L2PcInstance buyer, L2PcInstance manufacturer)
	{
		this.buyer = buyer;
		this.manufacturer = manufacturer;
	}

	@Override
	protected final void writeImpl()
	{
		L2ManufactureList createList = this.manufacturer.getCreateList();

		if (createList != null)
		{
			//dddd d(ddd)
			writeD(this.manufacturer.getObjectId());
			writeD((int) this.manufacturer.getCurrentMp());//Creator's MP
			writeD(this.manufacturer.getMaxMp());//Creator's MP
			writeQ(this.buyer.getAdena());//Buyer Adena

			int count = createList.size();
			writeD(count);
			L2ManufactureItem temp;

			for (int i = 0; i < count; i++)
			{
				temp = createList.getList().get(i);
				writeD(temp.getRecipeId());
				writeD(0x00); //unknown
				writeQ(temp.getCost());
			}
		}
	}
}
