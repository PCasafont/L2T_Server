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
import l2server.gameserver.model.ItemRequest;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.TradeList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.util.HashSet;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreBuy extends L2GameClientPacket
{

	private static final int BATCH_LENGTH = 20; // length of the one item

	private int _storePlayerId;
	private HashSet<ItemRequest> _items = null;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}
		_items = new HashSet<>();

		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			long cnt = readQ();
			long price = readQ();

			if (objectId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}

			_items.add(new ItemRequest(objectId, cnt, price));
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

		if (_items == null)
		{
			player.sendMessage("Couldn't find the items you are trying to buy.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestorebuy"))
		{
			player.sendMessage("You buying items too fast.");
			return;
		}

		L2Object object = L2World.getInstance().getPlayer(_storePlayerId);
		if (object == null)
		{
			player.sendMessage("ERR1.");
			return;
		}

		if (player.isCursedWeaponEquipped() || player.isInJail())
		{
			player.sendMessage("You can't do this while in jail or having a cursed weapon equipped.");
			return;
		}

		L2PcInstance storePlayer = (L2PcInstance) object;

		if (player.getInstanceId() != storePlayer.getInstanceId() && player.getInstanceId() != -1)
		{
			player.sendMessage("ERR2.");
			return;
		}

		if (!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL ||
				storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
		{
			player.sendMessage("ERR3.");
			return;
		}

		storePlayer.hasBeenStoreActive();

		TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
		{
			player.sendMessage("ERR4.");
			return;
		}

		if (player.getEvent() != null)
		{
			player.sendMessage("You cannot buy items while being involved in an event!");
			return;
		}

		if (player.getOlympiadGameId() > -1)
		{
			player.sendMessage("You cannot buy items while being involved in the Grand Olympiad!");
			return;
		}

		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _items.size())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() +
						" tried to buy less items than sold by package-sell, ban this player for bot usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}

		int result = storeList.privateStoreBuy(player, _items);

		if (result > 0)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			if (result > 1)
			{
				Log.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() +
						", Private store of: " + storePlayer.getName());
			}
			return;
		}

		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}

		/*   Lease holders are currently not implemented
				else if (_seller != null)
				{
					// lease shop sell
					L2MerchantInstance seller = (L2MerchantInstance)_seller;
					L2ItemInstance ladena = seller.getLeaseAdena();
					for (TradeItem ti : buyerlist) {
						L2ItemInstance li = seller.getLeaseItemByObjectId(ti.getObjectId());
						if (li == null) {
							if (ti.getObjectId() == ladena.getObjectId())
							{
								buyer.addAdena(ti.getCount());
								ladena.setCount(ladena.getCount()-ti.getCount());
								ladena.updateDatabase();
							}
							continue;
						}
						int cnt = li.getCount();
						if (cnt < ti.getCount())
							ti.setCount(cnt);
						if (ti.getCount() <= 0)
							continue;
						L2ItemInstance inst = ItemTable.getInstance().createItem(li.getItemId());
						inst.setCount(ti.getCount());
						inst.setEnchantLevel(li.getEnchantLevel());
						buyer.getInventory().addItem(inst);
						li.setCount(li.getCount()-ti.getCount());
						li.updateDatabase();
						ladena.setCount(ladena.getCount()+ti.getCount()*ti.getOwnersPrice());
						ladena.updateDatabase();
					}
				}*/
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.clientpackets.L2GameClientPacket#cleanUp()
	 */
	@Override
	protected void cleanUp()
	{
		_items = null;
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
