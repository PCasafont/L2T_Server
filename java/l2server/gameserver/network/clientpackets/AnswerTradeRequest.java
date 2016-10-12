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

import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.TradeDone;

/**
 * This class ...
 *
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AnswerTradeRequest extends L2GameClientPacket
{
	//

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (player.getEvent() != null)
		{
			player.sendMessage("You cannot trade while being involved in an event!");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getOlympiadGameId() > -1)
		{
			player.sendMessage("You cannot trade while being involved in the Grand Olympiad!");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2PcInstance partner = player.getActiveRequester();
		if (partner == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new TradeDone(0));
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setActiveRequester(null);
			msg = null;
			return;
		}
		else if (L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new TradeDone(0));
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.sendPacket(msg);
			player.setActiveRequester(null);
			msg = null;
			return;
		}

		if (_response == 1 && !partner.isRequestExpired())
		{
			player.startTrade(partner);
		}
		else
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_DENIED_TRADE_REQUEST);
			msg.addString(player.getName());
			partner.sendPacket(msg);
			msg = null;
		}

		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
}
