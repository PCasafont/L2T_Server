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
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ActionFailed;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.Collection;

public final class RequestStartPledgeWar extends L2GameClientPacket
{
	//

	private String _pledgeName;
	private L2Clan _clan;
	private L2PcInstance player;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		_clan = getClient().getActiveChar().getClan();
		if (_clan == null)
		{
			return;
		}

		if (_clan.getLevel() < Config.CLAN_WAR_MIN_CLAN_LEVEL ||
				!player.isGM() && _clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			SystemMessage sm =
					SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if ((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_CANNOT_DECLARED_CLAN_NOT_EXIST);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (_clan.getAllyId() == clan.getAllyId() && _clan.getAllyId() != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_AGAINST_A_ALLIED_CLAN_NOT_WORK);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (clan.getLevel() < Config.CLAN_WAR_MIN_CLAN_LEVEL ||
				!player.isGM() && clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
		{
			SystemMessage sm =
					SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_IF_CLAN_LVL3_OR_15_MEMBER);
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (_clan.isAtWarWith(clan.getClanId()))
		{
			// TODO: Update msg id
			SystemMessage sm =
					SystemMessage.getSystemMessage(SystemMessageId.ALREADY_AT_WAR_WITH_S1_WAIT_5_DAYS); //msg id 628
			sm.addString(clan.getName());
			player.sendPacket(sm);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (_clan.getWarList().size() + _clan.getEnemiesQueue().size() >= 30)
		{
			player.sendMessage("You can not declare another war if you have 30 wars declared.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (_clan.isOnWarRepose(clan) || clan.isOnWarRepose(_clan))
		{
			player.sendMessage("You can not declare a war to the same clan within 7 days.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		else if (_clan.getEnemiesQueue().contains(clan))
		{
			player.sendMessage("You have already declared war against " + clan.getName() + "!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		ClanWarManager.getInstance().storeClansWars(player.getClanId(), clan.getClanId(), player.getObjectId());
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
