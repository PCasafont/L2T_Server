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
 * sample
 * 5F
 * 01 00 00 00
 * <p>
 * format  cdd
 *
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestAnswerJoinAlly extends L2GameClientPacket
{
	//

	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2PcInstance requestor = activeChar.getRequest().getPartner();
		if (requestor == null)
		{
			return;
		}

		if (_response == 0)
		{
			activeChar
					.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_ALLY_INVITATION));
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NO_RESPONSE_TO_ALLY_INVITATION));
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly))
			{
				return; // hax
			}

			L2Clan clan = requestor.getClan();
			// we must double check this cause of hack
			if (clan.checkAllyJoinCondition(requestor, activeChar))
			{
				//TODO: Need correct message id
				requestor
						.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_INVITING_FRIEND));

				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ACCEPTED_ALLIANCE));

				activeChar.getClan().setAllyId(clan.getAllyId());
				activeChar.getClan().setAllyName(clan.getAllyName());
				activeChar.getClan().setAllyPenaltyExpiryTime(0, 0);
				activeChar.getClan().changeAllyCrest(clan.getAllyCrestId(), true);
				activeChar.getClan().updateClanInDB();
				for (L2Clan c : ClanTable.getInstance().getClans())
				{
					if (c.getAllyId() == clan.getAllyId())
					{
						// notify CB server about the change
					}
				}
			}
		}

		activeChar.getRequest().onRequestResponse();
	}
}
