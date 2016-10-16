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
 *         format from KenM
 */
public class ExStorageMaxCount extends L2GameServerPacket
{
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

	public ExStorageMaxCount(L2PcInstance character)
	{
		this.activeChar = character;
		this.inventory = this.activeChar.getInventoryLimit();
		this.warehouse = this.activeChar.getWareHouseLimit();
		this.privateSell = this.activeChar.getPrivateSellStoreLimit();
		this.privateBuy = this.activeChar.getPrivateBuyStoreLimit();
		this.clan = Config.WAREHOUSE_SLOTS_CLAN;
		this.receipeD = this.activeChar.getDwarfRecipeLimit();
		this.recipe = this.activeChar.getCommonRecipeLimit();
		this.inventoryExtraSlots = (int) this.activeChar.getStat().calcStat(Stats.INVENTORY_LIMIT, 0, null, null);
		this.inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(this.inventory);
		writeD(this.warehouse);
		writeD(this.clan);
		writeD(this.privateSell);
		writeD(this.privateBuy);
		writeD(this.receipeD);
		writeD(this.recipe);
		writeD(this.inventoryExtraSlots); // Belt inventory slots increase count
		writeD(this.inventoryQuestItems);
		writeD(40);
		writeD(40);
	}
}
