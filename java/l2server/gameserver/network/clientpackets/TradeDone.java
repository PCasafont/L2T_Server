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
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.TradeList;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.6.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class TradeDone extends L2GameClientPacket
{

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("trade"))
		{
			player.sendMessage("You trading too fast.");
			return;
		}

		final TradeList trade = player.getActiveTradeList();
		if (trade == null)
		{
			if (Config.DEBUG)
			{
				Log.warning("player.getTradeList == null in " + getType() + " for player " + player.getName());
			}
			return;
		}
		if (trade.isLocked())
		{
			return;
		}

		if (_response == 1)
		{
			if (trade.getPartner() == null || L2World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null)
			{
				// Trade partner not found, cancel trade
				player.cancelActiveTrade();
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
				return;
			}

			if (trade.getOwner().getActiveEnchantItem() != null || trade.getPartner().getActiveEnchantItem() != null)
			{
				return;
			}

			if (player.getEvent() != null)
			{
				player.sendMessage("You cannot trade items while being involved in an event!");
				return;
			}

			if (player.getOlympiadGameId() > -1)
			{
				player.sendMessage("You cannot trade items while being involved in the Grand Olympiad!");
				return;
			}

			if (!player.getAccessLevel().allowTransaction())
			{
				player.cancelActiveTrade();
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
				return;
			}

			if (player.getInstanceId() != trade.getPartner().getInstanceId() && player.getInstanceId() != -1)
			{
				player.cancelActiveTrade();
				return;
			}

			trade.confirm();
		}
		else
		{
			player.cancelActiveTrade();
		}
	}
}
