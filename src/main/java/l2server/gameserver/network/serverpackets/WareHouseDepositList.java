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
import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;

import java.util.ArrayList;

/**
 * 0x53 WareHouseDepositList  dh (h dddhh dhhh d)
 *
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class WareHouseDepositList extends L2ItemListPacket {
	public static final int PRIVATE = 4;
	public static final int CLAN = 4;
	public static final int CASTLE = 3; //not sure
	
	private final long playerAdena;
	private final ArrayList<Item> items;
	private final int whType;
	
	public WareHouseDepositList(Player player, int type) {
		whType = type;
		playerAdena = player.getAdena();
		items = new ArrayList<>();
		
		final boolean isPrivate = whType == PRIVATE;
		for (Item temp : player.getInventory().getAvailableItems(true, isPrivate)) {
			if (temp != null && temp.isDepositable(isPrivate)) {
				items.add(temp);
			}
		}
	}
	
	@Override
	protected final void writeImpl() {
		/* 0x01-Private Warehouse
		 * 0x02-Clan Warehouse
		 * 0x03-Castle Warehouse
		 * 0x04-Warehouse */
		writeH(whType);
		writeQ(playerAdena);
		writeD(0x00); // Already stored items count
		final int count = items.size();
		if (Config.DEBUG) {
			log.debug("count:" + count);
		}
		//writeH(0x00); // Weird count that we don't care about
		writeH(count);
		
		for (Item item : items) {
			writeItem(item);
			writeD(item.getObjectId());
		}
		items.clear();
	}
}
