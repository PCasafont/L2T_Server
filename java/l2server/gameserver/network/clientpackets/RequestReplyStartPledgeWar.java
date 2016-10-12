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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.ClanWarManager;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestReplyStartPledgeWar extends L2GameClientPacket
{
	//

	private int _answer;

	@Override
	protected void readImpl()
	{
		@SuppressWarnings("unused") String _reqName = readS();
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
		{
			return;
		}

		if (_answer == 1)
		{
			ClanWarManager.getInstance()
					.storeClansWars(requestor.getClanId(), activeChar.getClanId(), requestor.getObjectId());
		}
		else
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED));
		}
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
}
