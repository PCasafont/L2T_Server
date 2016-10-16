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
import l2server.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2server.gameserver.model.L2ItemInstance;
import l2server.gameserver.model.L2Manor;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2ManorManagerInstance;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.templates.item.L2Item;
import lombok.Getter;

import static l2server.gameserver.model.actor.L2Npc.DEFAULT_INTERACTION_DISTANCE;
import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * Format: (ch) d [dddd]
 * d: size
 * [
 * d  obj id
 * d  item id
 * d  manor id
 * d  count
 * ]
 *
 * @author l3x
 */
public class RequestProcureCropList extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 20; // length of the one item

	private Crop[] items = null;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != buf.remaining())
		{
			return;
		}

		items = new Crop[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			int itemId = readD();
			int manorId = readD();
			long cnt = readQ();
			if (objId < 1 || itemId < 1 || manorId < 0 || cnt < 0)
			{
				items = null;
				return;
			}
			items[i] = new Crop(objId, itemId, manorId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		if (items == null)
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
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

		if (!player.isInsideRadius(manager, DEFAULT_INTERACTION_DISTANCE, false, false))
		{
			return;
		}

		int castleId = ((L2ManorManagerInstance) manager).getCastle().getCastleId();

		// Calculate summary values
		int slots = 0;
		int weight = 0;

		for (Crop i : items)
		{
			if (!i.getCrop())
			{
				continue;
			}

			L2Item template = ItemTable.getInstance().getTemplate(i.getReward());
			weight += i.getCount() * template.getWeight();

			if (!template.isStackable())
			{
				slots += i.getCount();
			}
			else if (player.getInventory().getItemByItemId(i.getItemId()) == null)
			{
				slots++;
			}
		}

		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}

		// Proceed the purchase
		for (Crop i : items)
		{
			if (i.getReward() == 0)
			{
				continue;
			}

			long fee = i.getFee(castleId); // fee for selling to other manors

			long rewardPrice = ItemTable.getInstance().getTemplate(i.getReward()).getReferencePrice();
			if (rewardPrice == 0)
			{
				continue;
			}

			long rewardItemCount = i.getPrice() / rewardPrice;
			if (rewardItemCount < 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(i.getItemId());
				sm.addItemNumber(i.getCount());
				player.sendPacket(sm);
				continue;
			}

			if (player.getAdena() < fee)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(i.getItemId());
				sm.addItemNumber(i.getCount());
				player.sendPacket(sm);
				sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				continue;
			}

			// check if player have correct items count
			L2ItemInstance item = player.getInventory().getItemByObjectId(i.getObjectId());
			if (item == null || item.getCount() < i.getCount())
			{
				continue;
			}

			// try modify castle crop
			if (!i.setCrop())
			{
				continue;
			}

			if (fee > 0 && !player.reduceAdena("Manor", fee, manager, true))
			{
				continue;
			}

			if (!player.destroyItem("Manor", i.getObjectId(), i.getCount(), manager, true))
			{
				continue;
			}

			player.addItem("Manor", i.getReward(), rewardItemCount, manager, true);
		}
	}

	private static class Crop
	{
		@Getter private final int objectId;
		@Getter private final int itemId;
		private final int manorId;
		@Getter private final long count;
		@Getter private int reward = 0;
		private CropProcure crop = null;

		public Crop(int obj, int id, int m, long num)
		{
			objectId = obj;
			itemId = id;
			manorId = m;
			count = num;
		}

		public long getPrice()
		{
			return crop.getPrice() * count;
		}

		public long getFee(int castleId)
		{
			if (manorId == castleId)
			{
				return 0;
			}

			return getPrice() / 100 * 5; // 5% fee for selling to other manor
		}

		public boolean getCrop()
		{
			try
			{
				crop = CastleManager.getInstance().getCastleById(manorId)
						.getCrop(itemId, CastleManorManager.PERIOD_CURRENT);
			}
			catch (NullPointerException e)
			{
				return false;
			}
			if (crop == null || crop.getId() == 0 || crop.getPrice() == 0 || count == 0)
			{
				return false;
			}

			if (count > crop.getAmount())
			{
				return false;
			}

			if (MAX_ADENA / count < crop.getPrice())
			{
				return false;
			}

			reward = L2Manor.getInstance().getRewardItem(itemId, crop.getReward());
			return true;
		}

		public boolean setCrop()
		{
			synchronized (crop)
			{
				long amount = crop.getAmount();
				if (count > amount)
				{
					return false; // not enough crops
				}
				crop.setAmount(amount - count);
			}
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				CastleManager.getInstance().getCastleById(manorId)
						.updateCrop(itemId, crop.getAmount(), CastleManorManager.PERIOD_CURRENT);
			}
			return true;
		}
	}
}
