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
public final class RequestCommissionBuyInfo extends L2GameClientPacket
{

	@SuppressWarnings("unused")
	private long _auctionID;

	@Override
	protected void readImpl()
	{
		_auctionID = readQ();
		readD(); // Category - unused
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
		}

		/*AuctionManager am = AuctionManager.getInstance();
		Auctions auction = am.getAuctionById(_auctionID);
		if (auction != null)
		{
			if (activeChar.getObjectId() == auction.getPlayerID())
				activeChar.sendPacket(SystemMessageId.ITEM_PURCHASE_HAS_FAILED);
			else
				activeChar.sendPacket(new ExResponseCommissionBuyInfo(auction));
		}
		else
			activeChar.sendPacket(SystemMessageId.ITEM_PURCHASE_IS_NOT_AVAILABLE_BECAUSE_THE_CORRESPONDING_ITEM_DOES_NOT_EXIST);*/
	}
}
