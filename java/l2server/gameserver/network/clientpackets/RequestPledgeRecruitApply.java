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
import l2server.gameserver.instancemanager.ClanRecruitManager;
import l2server.gameserver.instancemanager.MailManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Message;
import l2server.gameserver.model.entity.Message.SendBySystem;
import l2server.gameserver.network.serverpackets.ExPledgeRecruitApplyInfo;

/**
 * @author Pere
 */
public final class RequestPledgeRecruitApply extends L2GameClientPacket
{
	private int _enter;
	private int _clanId;
	private String _application;

	@Override
	protected void readImpl()
	{
		_enter = readD();
		_clanId = readD();
		_application = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.getClan() != null)
		{
			return;
		}

		if (_enter == 1)
		{
			if (ClanRecruitManager.getInstance().addApplicant(activeChar, _clanId, _application))
			{
				activeChar.sendPacket(new ExPledgeRecruitApplyInfo(2));
				//activeChar.sendMessage(4039);
				L2Clan clan = ClanTable.getInstance().getClan(_clanId);

				Message msg = new Message(clan.getLeaderId(), "Clan Application",
						"There's a new applicant for your clan! Check out the clan entry for further information.",
						SendBySystem.SYSTEM);
				MailManager.getInstance().sendMessage(msg);
			}
		}
		else
		{
			if (ClanRecruitManager.getInstance().removeApplicant(activeChar.getObjectId()))
			{
			}
			{
				//activeChar.sendMessage(4040);
			}
			activeChar.sendPacket(new ExPledgeRecruitApplyInfo(0));
		}
	}
}
