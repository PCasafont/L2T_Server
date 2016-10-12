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
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author KenM
 */
public final class RequestJoinSiege extends L2GameClientPacket
{
	//

	private int _castleId;
	private int _isAttacker;
	private int _isJoining;

	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if ((activeChar.getClanPrivileges() & L2Clan.CP_CS_MANAGE_SIEGE) != L2Clan.CP_CS_MANAGE_SIEGE)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}

		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
		{
			return;
		}

		//NOT FOR ERTHEIA SERVER
		/*if (Config.isServer(Config.TENKAI) && !canRegister(activeChar))
            return;*/

		if (_isJoining == 1)
		{
			if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
			{
				activeChar.sendPacket(SystemMessage
						.getSystemMessage(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
				return;
			}
			if (_isAttacker == 1)
			{
				castle.getSiege().registerAttacker(activeChar);
			}
			else
			{
				castle.getSiege().registerDefender(activeChar);
			}
		}
		else
		{
			castle.getSiege().removeSiegeClan(activeChar);
		}

		castle.getSiege().listRegisterClan(activeChar);
	}

	public static boolean canRegister(L2PcInstance player)
	{
		if (player == null)
		{
			return false;
		}

		//Config
		int minClanLevel = 8;
		int minClanSize = 7;
		int numberOfPlayersToBeChecked = 5;
		int shouldHaveLevel = 99;
		int shouldHavePvPs = 5;
		int shouldBeCreatedDaysAgo = 5;

		//Vars
		int varHaveLevel = 0;
		int varHavePvPs = 0;
		int varBeCreatedDaysAgo = 0;

		List<String> ips = new ArrayList<>();

		L2Clan clan = ClanTable.getInstance().getClan(player.getClanId());

		if (clan.getLevel() < minClanLevel)
		{
			player.sendMessage("Your clan should have at least level " + minClanLevel + ".");
			return false;
		}

		if (clan.getMembersCount() < minClanSize)
		{
			player.sendMessage("Your clan should have at least " + minClanSize + " members in your clan.");
			return false;
		}

		if (clan.getOnlineMembersCount() < numberOfPlayersToBeChecked)
		{
			player.sendMessage("Your clan should have at least " + numberOfPlayersToBeChecked + " members online.");
			return false;
		}

		for (L2PcInstance member : clan.getOnlineMembers(0))
		{
			if (member == null)
			{
				continue;
			}

			if (ips.contains(member.getExternalIP()))
			{
				player.sendMessage(
						"Clan Member: " + member.getName() + " detected as dual box, doesn't count as online member!");
				continue;
			}

			if ((Calendar.getInstance().getTimeInMillis() - member.getCreateTime()) / 86400000L >
					shouldBeCreatedDaysAgo)
			{
				varBeCreatedDaysAgo++;
			}

			if (member.getLevel() >= shouldHaveLevel)
			{
				varHaveLevel++;
			}

			if (member.getPvpKills() >= shouldHavePvPs)
			{
				varHavePvPs++;
			}

			ips.add(member.getExternalIP());
		}

		if (varHaveLevel < numberOfPlayersToBeChecked || varHavePvPs < numberOfPlayersToBeChecked ||
				varBeCreatedDaysAgo < numberOfPlayersToBeChecked)
		{
			player.sendMessage(
					"Your clan looks weak to have a castle, you should train more your clan and your clan members.");
			player.sendMessage("Info: " + varHaveLevel + "/" + numberOfPlayersToBeChecked + ", " + varHavePvPs + "/" +
					numberOfPlayersToBeChecked + ", " + varBeCreatedDaysAgo + "/" + numberOfPlayersToBeChecked);
			return false;
		}

		return true;
	}
}
