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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1479 $ $Date: 2005-11-09 00:47:42 +0100 (mer., 09 nov. 2005) $
 */
public final class RequestAllyInfo extends L2GameClientPacket
{

	@Override
	public void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		SystemMessage sm;
		if (activeChar.getAllyId() == 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES);
			sendPacket(sm);
			return;
		}

		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
		sendPacket(sm);
		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
		sm.addString(activeChar.getClan().getAllyName());
		sendPacket(sm);

		int clanCount = 0;
		int totalMembers = 0;
		int onlineMembers = 0;
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() != activeChar.getAllyId())
			{
				continue;
			}

			clanCount++;
			totalMembers += clan.getMembersCount();
			onlineMembers += clan.getOnlineMembersCount();
		}
		sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
		sm.addNumber(onlineMembers);
		sm.addNumber(totalMembers);
		sendPacket(sm);

		final L2Clan leaderClan = ClanTable.getInstance().getClan(activeChar.getAllyId());
		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
		sm.addString(leaderClan.getName());
		sm.addString(leaderClan.getLeaderName());
		sendPacket(sm);

		sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
		sm.addNumber(clanCount);
		sendPacket(sm);

		sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD);
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() != activeChar.getAllyId())
			{
				continue;
			}

			sendPacket(sm); // send head or separator
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
			sm.addString(clan.getName());
			sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
			sm.addString(clan.getLeaderName());
			sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
			sm.addNumber(clan.getLevel());
			sendPacket(sm);
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
		}
		sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT);
		sendPacket(sm);
	}
}
