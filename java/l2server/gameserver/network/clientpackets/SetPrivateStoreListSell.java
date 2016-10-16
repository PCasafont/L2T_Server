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
import l2server.gameserver.model.TradeList;
import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.*;
import l2server.gameserver.taskmanager.AttackStanceTaskManager;
import l2server.gameserver.util.Util;

import static l2server.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class SetPrivateStoreListSell extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 20; // length of the one item

	private boolean packageSale;
	private Item[] items = null;

	@Override
	protected void readImpl()
	{
		this.packageSale = readD() == 1;
		int count = readD();
		if (count < 1 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != this.buf.remaining())
		{
			return;
		}

		this.items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			long price = readQ();

			if (itemId < 1 || cnt < 1 || price < 0)
			{
				this.items = null;
				return;
			}
			this.items[i] = new Item(itemId, cnt, price);
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

		if (this.items == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_ITEM_COUNT));
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}

		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) || player.isInDuel())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT));
			player.sendPacket(new PrivateStoreManageListSell(player, this.packageSale));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isInsideZone(L2Character.ZONE_NOSTORE))
		{
			player.sendPacket(new PrivateStoreManageListSell(player, this.packageSale));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_PRIVATE_STORE_HERE));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check maximum number of allowed slots for pvt shops
		if (this.items.length > player.getPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player, this.packageSale));
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		for (L2Character c : player.getKnownList().getKnownCharactersInRadius(70))
		{
			if (!(c instanceof L2PcInstance &&
					((L2PcInstance) c).getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE))
			{
				player.sendPacket(new PrivateStoreManageListSell(player, this.packageSale));
				player.sendMessage("Try to put your store a little further from " + c.getName() + ", please.");
				return;
			}
		}

		TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(this.packageSale);

		long totalCost = player.getAdena();
		for (Item i : this.items)
		{
			if (!i.addToTradeList(tradeList))
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to set price more than " + MAX_ADENA + " adena in Private Store - Sell.",
						Config.DEFAULT_PUNISH);
				return;
			}

			totalCost += i.getPrice();
			if (totalCost > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player,
						"Warning!! Character " + player.getName() + " of account " + player.getAccountName() +
								" tried to set total price more than " + MAX_ADENA + " adena in Private Store - Sell.",
						Config.DEFAULT_PUNISH);
				return;
			}
		}

		player.sitDown();

		if (this.packageSale)
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_PACKAGE_SELL);
		}
		else
		{
			player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL);
		}

		player.broadcastUserInfo();

		if (this.packageSale)
		{
			player.broadcastPacket(new ExPrivateStoreSetWholeMsg(player));
		}
		else
		{
			player.broadcastPacket(new PrivateStoreMsgSell(player));
		}
	}

	private static class Item
	{
		private final int itemId;
		private final long count;
		private final long price;

		public Item(int id, long num, long pri)
		{
			this.itemId = id;
			this.count = num;
			this.price = pri;
		}

		public boolean addToTradeList(TradeList list)
		{
			if (MAX_ADENA / this.count < this.price)
			{
				return false;
			}

			list.addItem(this.itemId, this.count, this.price);
			return true;
		}

		public long getPrice()
		{
			return this.count * this.price;
		}
	}
}
