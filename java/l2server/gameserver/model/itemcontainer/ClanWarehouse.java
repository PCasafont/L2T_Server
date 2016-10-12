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

package l2server.gameserver.model.itemcontainer;

import l2server.Config;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2ItemInstance.ItemLocation;
import l2server.gameserver.model.actor.instance.L2PcInstance;

public final class ClanWarehouse extends Warehouse
{
	private L2Clan _clan;

	public ClanWarehouse(L2Clan clan)
	{
		_clan = clan;
	}

	@Override
	public String getName()
	{
		return "ClanWarehouse";
	}

	@Override
	public int getOwnerId()
	{
		return _clan.getLeaderId();
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _clan.getLeader() != null ? _clan.getLeader().getPlayerInstance() : null;
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return _items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
	}

	public void updateItemsOwnerId()
	{
		int newOwnerId = getOwnerId();
		for (L2ItemInstance item : _items.values())
		{
			item.setOwnerId(newOwnerId);
		}
	}
}
