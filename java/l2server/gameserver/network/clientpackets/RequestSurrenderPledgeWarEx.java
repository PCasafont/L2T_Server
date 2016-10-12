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

import l2server.gameserver.datatables.ClanTable;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.ClanWarManager;
import l2server.gameserver.model.entity.ClanWarManager.ClanWar;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.Collection;

/**
 * @author Erlandys
 */
public final class RequestSurrenderPledgeWarEx extends L2GameClientPacket
{

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

		if (!playerClan.getClanWars().contains(clan))
		{
			player.sendMessage("Your clan does not have any war relation with " + clan.getName() + "'s clan.");
			return;
		}

		if (!playerClan.isAtWarWith(clan.getClanId()) && !clan.isAtWarWith(playerClan.getClanId()))
		{
			//player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
			player.sendMessage("War with " + clan.getName() + " isn't started or in repose state!");
			return;
		}

		// Check if player who does the request has the correct rights to do it
		if ((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		ClanWar war = ClanWarManager.getInstance().getWar(clan, playerClan);
		war.setLoser(playerClan);
		war.setWinner(clan);
		war.stop();
		Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
		for (L2PcInstance cha : pls)
		{
			if (cha.getClan() == player.getClan() || cha.getClan() == clan)
			{
				cha.broadcastUserInfo();
			}
		}
	}
}
