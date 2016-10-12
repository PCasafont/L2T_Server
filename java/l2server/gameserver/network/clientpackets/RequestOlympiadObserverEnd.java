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

/**
 * format ch
 * c: (id) 0xD0
 * h: (subid) 0x12
 *
 * @author -Wooden-
 */
public final class RequestOlympiadObserverEnd extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// trigger
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (activeChar.inObserverMode())
		{
			if (activeChar.getOlympiadGameId() == -1)
			{
				activeChar.leaveEventObserverMode();
			}
			else
			{
				activeChar.leaveOlympiadObserverMode();
			}
		}
	}
}
