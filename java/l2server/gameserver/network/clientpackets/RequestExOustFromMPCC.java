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

import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden-
 *         <p>
 *         D0 0F 00 5A 00 77 00 65 00 72 00 67 00 00 00
 */
public final class RequestExOustFromMPCC extends L2GameClientPacket
{
	//
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance target = L2World.getInstance().getPlayer(_name);
		L2PcInstance activeChar = getClient().getActiveChar();

		if (target != null && target.isInParty() && activeChar.isInParty() &&
				activeChar.getParty().isInCommandChannel() && target.getParty().isInCommandChannel() &&
				activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
		{
			if (activeChar.equals(target))
			{
				return;
			}

			target.getParty().getCommandChannel().removeParty(target.getParty());

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DISMISSED_FROM_COMMAND_CHANNEL);
			target.getParty().broadcastToPartyMembers(sm);

			// check if CC has not been canceled
			if (activeChar.getParty().isInCommandChannel())
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.C1_PARTY_DISMISSED_FROM_COMMAND_CHANNEL);
				sm.addString(target.getParty().getLeader().getName());
				activeChar.getParty().getCommandChannel().broadcastToChannelMembers(sm);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
		}
	}
}
