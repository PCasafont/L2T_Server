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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Stats;

/**
 * Format: (ch)ddddddd
 * d: Number of Inventory Slots
 * d: Number of Warehouse Slots
 * d: Number of Freight Slots (unconfirmed) (200 for a low level dwarf)
 * d: Private Sell Store Slots (unconfirmed) (4 for a low level dwarf)
 * d: Private Buy Store Slots (unconfirmed) (5 for a low level dwarf)
 * d: Dwarven Recipe Book Slots
 * d: Normal Recipe Book Slots
 *
 * @author -Wooden-
 * format from KenM
 */
public class ExStorageMaxCount extends L2GameServerPacket {
	private L2PcInstance activeChar;
	private int inventory;
	private int warehouse;
	private int clan;
	private int privateSell;
	private int privateBuy;
	private int receipeD;
	private int recipe;
	private int inventoryExtraSlots;
	private int inventoryQuestItems;
	
	public ExStorageMaxCount(L2PcInstance character) {
		activeChar = character;
		inventory = activeChar.getInventoryLimit();
		warehouse = activeChar.getWareHouseLimit();
		privateSell = activeChar.getPrivateSellStoreLimit();
		privateBuy = activeChar.getPrivateBuyStoreLimit();
		clan = Config.WAREHOUSE_SLOTS_CLAN;
		receipeD = activeChar.getDwarfRecipeLimit();
		recipe = activeChar.getCommonRecipeLimit();
		inventoryExtraSlots = (int) activeChar.getStat().calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
		inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(inventory);
		writeD(warehouse);
		writeD(clan);
		writeD(privateSell);
		writeD(privateBuy);
		writeD(receipeD);
		writeD(recipe);
		writeD(inventoryExtraSlots); // Belt inventory slots increase count
		writeD(inventoryQuestItems);
		writeD(40);
		writeD(40);
	}
}
