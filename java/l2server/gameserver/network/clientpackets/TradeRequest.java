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
import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SendTradeRequest;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;
import l2server.log.Log;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class TradeRequest extends L2GameClientPacket
{

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
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
			player.sendMessage("Transactions are disable for your Access Level");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object target = L2World.getInstance().findObject(_objectId);
		if (target == null || !player.getKnownList().knowsObject(target) || !(target instanceof L2PcInstance))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}

		if (target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		L2PcInstance partner = (L2PcInstance) target;

		// cant trade with players from other instance except from multiverse
		if (partner.getInstanceId() != player.getInstanceId() && player.getInstanceId() != -1)
		{
			return;
		}

		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target cant request trade in Olympiad mode");
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getReputation() < 0 || partner.getReputation() < 0))
		{
			player.sendMessage("Chaotic players can't use Trade.");
			return;
		}

		if (Config.JAIL_DISABLE_TRANSACTION && (player.isInJail() || partner.isInJail()))
		{
			player.sendMessage("You cannot trade in Jail.");
			return;
		}

		if (player.getPrivateStoreType() != 0 || partner.getPrivateStoreType() != 0)
		{
			player.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		if (player.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				Log.fine("already trading with someone");
			}
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_TRADING));
			return;
		}

		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if (Config.DEBUG)
			{
				Log.info("transaction already in progress.");
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}

		if (partner.getTradeRefusal())
		{
			player.sendMessage("Target is in trade refusal mode");
			return;
		}

		// LasTravel
		if (partner.getIsRefusingRequests())
		{
			player.sendMessage("Your target have the requests blocked!");
			return;
		}

		if (BlockList.isBlocked(partner, player))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(partner);
			player.sendPacket(sm);
			return;
		}

		if (Util.calculateDistance(player, partner, true) > 150)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_TOO_FAR));
			return;
		}

		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}
}
