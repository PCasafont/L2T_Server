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

import l2server.gameserver.model.itemcontainer.Inventory;

import java.util.Map;

/**
 * * @author Gnacik
 */
public class ShopPreviewInfo extends L2GameServerPacket
{
	private Map<Integer, Integer> _itemlist;

	public ShopPreviewInfo(Map<Integer, Integer> itemlist)
	{
		_itemlist = itemlist;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(Inventory.PAPERDOLL_TOTALSLOTS);
		// Slots
		writeD(getFromList(Inventory.PAPERDOLL_UNDER));
		writeD(getFromList(Inventory.PAPERDOLL_REAR));
		writeD(getFromList(Inventory.PAPERDOLL_LEAR));
		writeD(getFromList(Inventory.PAPERDOLL_NECK));
		writeD(getFromList(Inventory.PAPERDOLL_RFINGER));
		writeD(getFromList(Inventory.PAPERDOLL_LFINGER));
		writeD(getFromList(Inventory.PAPERDOLL_HEAD));
		writeD(getFromList(Inventory.PAPERDOLL_RHAND));
		writeD(getFromList(Inventory.PAPERDOLL_LHAND));
		writeD(getFromList(Inventory.PAPERDOLL_GLOVES));
		writeD(getFromList(Inventory.PAPERDOLL_CHEST));
		writeD(getFromList(Inventory.PAPERDOLL_LEGS));
		writeD(getFromList(Inventory.PAPERDOLL_FEET));
		writeD(getFromList(Inventory.PAPERDOLL_CLOAK));
		writeD(getFromList(Inventory.PAPERDOLL_RHAND));
		writeD(getFromList(Inventory.PAPERDOLL_HAIR));
		writeD(getFromList(Inventory.PAPERDOLL_HAIR2));
		writeD(getFromList(Inventory.PAPERDOLL_RBRACELET));
		writeD(getFromList(Inventory.PAPERDOLL_LBRACELET));
	}

	private int getFromList(int key)
	{
		return _itemlist.get(key) != null ? _itemlist.get(key) : 0;
	}
}
