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

import l2server.gameserver.instancemanager.ItemAuctionManager;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.itemauction.ItemAuction;
import l2server.gameserver.model.itemauction.ItemAuctionInstance;
import l2server.gameserver.model.itemcontainer.PcInventory;

/**
 * @author Forsaiken
 */
public final class RequestBidItemAuction extends L2GameClientPacket
{
	private int _instanceId;
	private long _bid;

	@Override
	protected final void readImpl()
	{
		_instanceId = super.readD();
		_bid = super.readQ();
	}

	@Override
	protected final void runImpl()
	{
		final L2PcInstance activeChar = super.getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		// can't use auction fp here
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("auction"))
		{
			activeChar.sendMessage("You bidding too fast.");
			return;
		}

		if (_bid < 0 || _bid > PcInventory.MAX_ADENA)
		{
			return;
		}

		final ItemAuctionInstance instance = ItemAuctionManager.getInstance().getManagerInstance(_instanceId);
		if (instance != null)
		{
			final ItemAuction auction = instance.getCurrentAuction();
			if (auction != null)
			{
				auction.registerBid(activeChar, _bid);
			}
		}
	}
}
