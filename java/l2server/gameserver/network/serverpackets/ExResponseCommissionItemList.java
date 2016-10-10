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

import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Erlandys
 */
public class ExResponseCommissionItemList extends L2ItemListPacket
{

    private List<L2ItemInstance> _items = new ArrayList<L2ItemInstance>();

    public ExResponseCommissionItemList(L2PcInstance player)
    {
        for (L2ItemInstance item : player.getInventory().getItems())
        {
            if (item.isSellable() && item.isTradeable() && !item.isEquipped() && item.getItemId() != 57 &&
                    !item.isQuestItem())
            {
                _items.add(item);
            }
        }
    }

    @Override
    protected final void writeImpl()
    {
        writeD(_items.size());

        for (L2ItemInstance item : _items)
        {
            writeItem(item);
        }
    }
}
