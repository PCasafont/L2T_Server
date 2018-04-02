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

import l2server.gameserver.model.Item;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.templates.item.ItemTemplate;
import l2server.gameserver.templates.item.WeaponTemplate;

public class ExShowBaseAttributeCancelWindow extends L2GameServerPacket {
	private Item[] items;
	private long price;
	
	public ExShowBaseAttributeCancelWindow(Player player) {
		items = player.getInventory().getElementItems();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(items.length);
		for (Item item : items) {
			writeD(item.getObjectId());
			writeQ(getPrice(item));
		}
	}
	
	private long getPrice(Item item) {
		switch (item.getItem().getCrystalType()) {
			case ItemTemplate.CRYSTAL_S:
				if (item.getItem() instanceof WeaponTemplate) {
					price = 50000;
				} else {
					price = 40000;
				}
				break;
			case ItemTemplate.CRYSTAL_S80:
				if (item.getItem() instanceof WeaponTemplate) {
					price = 100000;
				} else {
					price = 80000;
				}
				break;
			case ItemTemplate.CRYSTAL_S84:
				if (item.getItem() instanceof WeaponTemplate) {
					price = 200000;
				} else {
					price = 160000;
				}
				break;
			case ItemTemplate.CRYSTAL_R:
				if (item.getItem() instanceof WeaponTemplate) {
					price = 250000;
				} else {
					price = 240000;
				}
				break;
			case ItemTemplate.CRYSTAL_R95:
				if (item.getItem() instanceof WeaponTemplate) {
					price = 300000;
				} else {
					price = 280000;
				}
				break;
			case ItemTemplate.CRYSTAL_R99:
				if (item.getItem() instanceof WeaponTemplate) {
					price = 350000;
				} else {
					price = 320000;
				}
				break;
		}
		
		return price;
	}
}
