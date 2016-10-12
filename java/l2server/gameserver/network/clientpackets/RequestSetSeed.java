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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

import static l2server.gameserver.model.actor.L2Npc.DEFAULT_INTERACTION_DISTANCE;
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * Format: (ch) dd [ddd]
 * d - manor id
 * d - size
 * [
 * d - seed id
 * d - sales
 * d - price
 * ]
 *
 * @author l3x
 */
public class RequestSetSeed extends L2GameClientPacket
{
	//

	private static final int BATCH_LENGTH = 20; // length of the one item

	private int _manorId;
	private Seed _items[] = null;

	/**
	 */
	@Override
	protected void readImpl()
	{
		_manorId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new Seed[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long sales = readQ();
			long price = readQ();
			if (itemId < 1 || sales < 0 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Seed(itemId, sales, price);
		}
	}

	@Override
	protected void runImpl()
	{
		if (_items == null)
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		// check player privileges
		if (player == null || player.getClan() == null || (player.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) == 0)
		{
			return;
		}

		// check castle owner
		Castle currentCastle = CastleManager.getInstance().getCastleById(_manorId);
		if (currentCastle.getOwnerId() != player.getClanId())
		{
			return;
		}

		L2Object manager = player.getTarget();

		if (!(manager instanceof L2CastleChamberlainInstance))
		{
			manager = player.getLastFolkNPC();
		}

		if (!(manager instanceof L2CastleChamberlainInstance))
		{
			return;
		}

		if (((L2CastleChamberlainInstance) manager).getCastle() != currentCastle)
		{
			return;
		}

		if (!player.isInsideRadius(manager, DEFAULT_INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		List<SeedProduction> seeds = new ArrayList<>(_items.length);
		for (Seed i : _items)
		{
			SeedProduction s = i.getSeed();
			if (s == null)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to overflow while setting manor.", Config.DEFAULT_PUNISH);
				return;
			}
			seeds.add(s);
		}

		currentCastle.setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			currentCastle.saveSeedData(CastleManorManager.PERIOD_NEXT);
		}
	}

	private static class Seed
	{
		private final int _itemId;
		private final long _sales;
		private final long _price;

		public Seed(int id, long s, long p)
		{
			_itemId = id;
			_sales = s;
			_price = p;
		}

		public SeedProduction getSeed()
		{
			if (_sales != 0 && MAX_ADENA / _sales < _price)
			{
				return null;
			}

			return CastleManorManager.getInstance().getNewSeedProduction(_itemId, _sales, _price, _sales);
		}
	}
}
