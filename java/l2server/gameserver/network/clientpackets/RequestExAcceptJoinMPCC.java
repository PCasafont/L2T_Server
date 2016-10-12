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

import l2server.gameserver.model.L2CommandChannel;
import l2server.gameserver.model.L2Party;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * format: (ch) d
 *
 * @author -Wooden-
 */
public final class RequestExAcceptJoinMPCC extends L2GameClientPacket
{
	private int _response;

	/**
	 */
	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player != null)
		{
			L2PcInstance requestor = player.getActiveRequester();
			SystemMessage sm;
			if (requestor == null)
			{
				return;
			}

			if (_response == 1)
			{
				boolean newCc = false;

				L2Party party = requestor.getParty();

				if (party == null)
				{
					return;
				}

				if (!party.isInCommandChannel())
				{
					new L2CommandChannel(requestor); // Create new CC
					sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED);
					requestor.sendPacket(sm);
					newCc = true;
				}

				party.getCommandChannel().addParty(player.getParty());
				if (!newCc)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL);
					player.sendPacket(sm);
				}
			}
			else
			{
				requestor.sendMessage("The player declined to join your Command Channel.");
			}

			player.setActiveRequester(null);
			requestor.onTransactionResponse();
		}
	}
}
