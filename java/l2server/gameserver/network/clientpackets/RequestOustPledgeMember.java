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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;

import java.util.logging.Logger;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestOustPledgeMember extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(RequestOustPledgeMember.class.getName());

	private String _target;

	@Override
	protected void readImpl()
	{
		_target = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
			return;
		}
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_DISMISS) != L2Clan.CP_CL_DISMISS)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		if (activeChar.getName().equalsIgnoreCase(_target))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF));
			return;
		}

		L2Clan clan = activeChar.getClan();

		L2ClanMember member = clan.getClanMember(_target);
		if (member == null)
		{
			Log.warning("Target (" + _target + ") is not member of the clan");
			return;
		}
		if (member.isOnline() && member.getPlayerInstance().isInCombat())
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_CANNOT_BE_DISMISSED_DURING_COMBAT));
			return;
		}

		// this also updates the database
		clan.removeClanMember(member.getObjectId(), System.currentTimeMillis() + 18000000L); //24*60*60*1000 = 86400000
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + 18000000L); //24*60*60*1000 = 86400000
		clan.updateClanInDB();

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
		sm.addString(member.getName());
		clan.broadcastToOnlineMembers(sm);
		sm = null;
		activeChar.sendPacket(
				SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER));
		activeChar.sendPacket(
				SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER));

		// Remove the Player From the Member list
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));

		if (member.isOnline())
		{
			L2PcInstance player = member.getPlayerInstance();
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));
		}
	}
}
