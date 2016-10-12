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
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

public final class AllyLeave extends L2GameClientPacket
{

	//

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		if (player.getClan() == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
			return;
		}
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEADER_WITHDRAW_ALLY));
			return;
		}
		L2Clan clan = player.getClan();
		if (clan.getAllyId() == 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
			return;
		}
		if (clan.getClanId() == clan.getAllyId())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW));
			return;
		}

		long currentTime = System.currentTimeMillis();
		clan.setAllyId(0);
		clan.setAllyName(null);
		clan.changeAllyCrest(0, true);
		clan.setAllyPenaltyExpiryTime(currentTime + Config.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 86400000L,
				L2Clan.PENALTY_TYPE_CLAN_LEAVED); //24*60*60*1000 = 86400000
		clan.updateClanInDB();
		// notify CB server about the change

		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE));
	}
}
