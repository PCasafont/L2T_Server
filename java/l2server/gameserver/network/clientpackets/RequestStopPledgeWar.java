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
import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.ClanWarManager;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
	//

	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		L2Clan playerClan = player.getClan();
		if (playerClan == null)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
		{
			player.sendMessage("No such clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check if player who does the request has the correct rights to do it
		if ((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		if (!playerClan.getClanWars().contains(clan))
		{
			player.sendMessage("Your clan does not have any war relation with " + clan.getName() + "'s clan.");
			return;
		}

		int repToTake = Config.CANCEL_CLAN_WAR_REPUTATION_POINTS;
		if (Config.isServer(Config.TENKAI_ESTHUS))
		{
			if (playerClan.getReputationScore() < 500000)
			{
				player.sendMessage("Your clan needst to have at least 500000 Reputation Points to end this war.");
				return;
			}

			repToTake = playerClan.getReputationScore() / 3;
		}

		if (playerClan.getReputationScore() < repToTake)
		{
			player.sendMessage("Your clan doesn't have " + Config.CANCEL_CLAN_WAR_REPUTATION_POINTS +
					" Reputation Points to end this war."); // TODO: System Message
			return;
		}

		if (!playerClan.getEnemiesQueue().contains(clan) && !playerClan.isAtWarWith(clan.getClanId()))
		{
			player.sendMessage(
					"The clan you've requested is not on the enemies queue. It might have been started or in repose or declarators are other clan.");
			return;
		}

		ClanWar war = ClanWarManager.getInstance().getWar(clan, playerClan);
		if (war != null)
		{
			war.declare(clan);
		}

		playerClan.takeReputationScore(repToTake, true);
		clan.addReputationScore(repToTake, true);

		for (L2PcInstance cha : L2World.getInstance().getAllPlayersArray())
		{
			if (cha == null)
			{
				continue;
			}

			if (cha.getClan() == player.getClan() || cha.getClan() == clan)
			{
				cha.broadcastUserInfo();
			}
		}
	}
}
