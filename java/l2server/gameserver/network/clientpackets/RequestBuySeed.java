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
import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.instancemanager.CastleManorManager;
import l2server.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;
import l2server.gameserver.util.Util;

import static l2server.gameserver.model.actor.L2Npc.DEFAULT_INTERACTION_DISTANCE;
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * Format: cdd[dd]
 * c	// id (0xC4)
 * <p>
 * d	// manor id
 * d	// seeds to buy
 * [
 * d	// seed id
 * q	// count
 * ]
 *
 * @author l3x
 */

public class RequestBuySeed extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 12; // length of the one item

	private int _manorId;
	private Seed[] _seeds = null;

	@Override
	protected void readImpl()
	{
		_manorId = readD();

		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_seeds = new Seed[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			if (cnt < 1)
			{
				_seeds = null;
				return;
			}
			_seeds[i] = new Seed(itemId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getManor().tryPerformAction("BuySeed"))
		{
			return;
		}

		if (_seeds == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object manager = player.getTarget();

		if (!(manager instanceof L2ManorManagerInstance))
		{
			manager = player.getLastFolkNPC();
		}

		if (!(manager instanceof L2ManorManagerInstance))
		{
			return;
		}

		if (!player.isInsideRadius(manager, DEFAULT_INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(_manorId);

		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		for (Seed i : _seeds)
		{
			if (!i.setProduction(castle))
			{
				return;
			}

			totalPrice += i.getPrice();

			if (totalPrice > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to purchase over " + MAX_ADENA + " adena worth of goods.",
						Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(i.getSeedId());
			totalWeight += i.getCount() * template.getWeight();
			if (!template.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(i.getSeedId()) == null)
			{
				slots++;
			}
		}

		if (!player.getInventory().validateWeight(totalWeight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}

		// test adena
		if (totalPrice < 0 || player.getAdena() < totalPrice)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		// Proceed the purchase
		for (Seed i : _seeds)
		{
			// take adena and check seed amount once again
			if (!player.reduceAdena("Buy", i.getPrice(), player, false) || !i.updateProduction(castle))
			{
				// failed buy, reduce total price
				totalPrice -= i.getPrice();
				continue;
			}

			// Add item to Inventory and adjust update packet
			player.addItem("Buy", i.getSeedId(), i.getCount(), manager, true);
		}

		// Adding to treasury for Manor Castle
		if (totalPrice > 0)
		{
			castle.addToTreasuryNoTax(totalPrice);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
			sm.addItemNumber(totalPrice);
			player.sendPacket(sm);
		}
	}

	private static class Seed
	{
		private final int _seedId;
		private final long _count;
		SeedProduction _seed;

		public Seed(int id, long num)
		{
			_seedId = id;
			_count = num;
		}

		public int getSeedId()
		{
			return _seedId;
		}

		public long getCount()
		{
			return _count;
		}

		public long getPrice()
		{
			return _seed.getPrice() * _count;
		}

		public boolean setProduction(Castle c)
		{
			_seed = c.getSeed(_seedId, CastleManorManager.PERIOD_CURRENT);
			// invalid price - seed disabled
			if (_seed.getPrice() <= 0)
			{
				return false;
			}
			// try to buy more than castle can produce
			if (_seed.getCanProduce() < _count)
			{
				return false;
			}
			// check for overflow
			return MAX_ADENA / _count >= _seed.getPrice();

		}

		public boolean updateProduction(Castle c)
		{
			synchronized (_seed)
			{
				long amount = _seed.getCanProduce();
				if (_count > amount)
				{
					return false; // not enough seeds
				}
				_seed.setCanProduce(amount - _count);
			}
			// Update Castle Seeds Amount
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				c.updateSeed(_seedId, _seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			}
			return true;
		}
	}
}
