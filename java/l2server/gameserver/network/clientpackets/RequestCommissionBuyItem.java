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

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Erlandys
 */
public final class RequestCommissionBuyItem extends L2GameClientPacket
{

	@SuppressWarnings("unused")
	private long _auctionID;
	@SuppressWarnings("unused")
	private int _category;

	@Override
	protected void readImpl()
	{
		_auctionID = readQ();
		_category = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
		}

		/*AuctionManager am = AuctionManager.getInstance();
		Auctions auction;
		if (am.getAuctionById(_auctionID) != null)
		{
			auction = am.getAuctionById(_auctionID);
			long timeToFinish = auction.getFinishTime() - System.currentTimeMillis() / 1000;

			long fee = auction.getPrice();
			switch (auction.getDuration())
			{
				case 0:
					fee *= 0.005;
					break;
				case 1:
					fee *= 0.015;
					break;
				case 2:
					fee *= 0.025;
					break;
				default:
					fee *= 0.035;
			}
			if (fee < 10000)
				fee = 1000;
			long fee = 1000;
			long price = auction.getPrice() * auction.getCount();
			if (player.getInventory().getItemByItemId(57) == null || player.getInventory().getItemByItemId(57).getCount() < price)
			{
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(new ExResponseCommissionList(player, _category, -1, -1, ""));
				player.sendPacket(new ExResponseCommissionItemList(player));
				return;
			}
			player.getInventory().destroyItemByItemId("BuyFromAuction", 57, price, null, null);
			player.getInventory().addItem("BuyFromAuction", auction.getItem().getItemId(), auction.getCount(), player, null);
			Message msg = new Message(auction.getPlayerID(), "The item you registered has been sold.", auction.getItemName() + " has been sold.", SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD.getId(), SystemMessageId.S1_HAS_BEEN_SOLD.getId());
			if (price - fee > 0)
				msg.createAttachments().addItem("BuyFromAuction", 57, (price - fee), null, null);
			MailManager.getInstance().sendMessage(msg);
			SystemMessage	sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCESSFULLY_PURCHASED_S2_OF_S1);
			sm.addItemNumber(auction.getCount());
			sm.addString(auction.getItemName());
			player.sendPacket(sm);
			if (L2World.getInstance().getPlayer(auction.getPlayerID()) != null)
			{
				L2PcInstance seller = L2World.getInstance().getPlayer(auction.getPlayerID());
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD);
				seller.sendPacket(sm);
			}

			am.deleteAuction(_auctionID);
			player.sendPacket(new ExResponseCommissionList(player, _category, -1, -1, ""));
			player.sendPacket(new ExResponseCommissionItemList(player));
		}
		else
		{
			player.sendPacket(SystemMessageId.ITEM_PURCHASE_IS_NOT_AVAILABLE_BECAUSE_THE_CORRESPONDING_ITEM_DOES_NOT_EXIST);
			player.sendPacket(new ExResponseCommissionList(player, _category, -1, -1, ""));
			player.sendPacket(new ExResponseCommissionItemList(player));
		}*/
	}
}
