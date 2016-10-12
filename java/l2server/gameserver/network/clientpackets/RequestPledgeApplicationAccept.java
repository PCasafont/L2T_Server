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

import l2server.gameserver.instancemanager.ClanRecruitManager;
import l2server.gameserver.instancemanager.ClanRecruitManager.ClanRecruitWaitingUser;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.entity.Message.SendBySystem;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.AskJoinPledge;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Pere
 */
public final class RequestPledgeApplicationAccept extends L2GameClientPacket
{
	private boolean _accept;
	private int _applicantId;
	private int _pledgeType;

	@Override
	protected void readImpl()
	{
		_accept = readD() == 1;
		_applicantId = readD();
		_pledgeType = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || !activeChar.isClanLeader())
		{
			return;
		}

		ClanRecruitWaitingUser applicant = ClanRecruitManager.getInstance().getApplicant(_applicantId);
		if (applicant == null)
		{
			return;
		}

		if (_accept)
		{
			final L2Clan clan = activeChar.getClan();
			if (clan == null)
			{
				return;
			}

			final L2PcInstance target = L2World.getInstance().getPlayer(_applicantId);
			if (target == null)
			{
				activeChar
						.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
				return;
			}

			if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
			{
				return;
			}

			if (!activeChar.getRequest().setRequest(target, this))
			{
				return;
			}

			final String pledgeName = clan.getName();
			final String subPledgeName =
					clan.getSubPledge(_pledgeType) != null ? activeChar.getClan().getSubPledge(_pledgeType).getName() :
							null;
			target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), subPledgeName, _pledgeType, pledgeName));
		}
		else
		{
			Message msg = new Message(_applicantId, "Clan Application Rejected",
					"Sorry, your clan application has been rejected.", SendBySystem.SYSTEM);
			MailManager.getInstance().sendMessage(msg);

			ClanRecruitManager.getInstance().removeApplicant(_applicantId);
		}
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}
}
