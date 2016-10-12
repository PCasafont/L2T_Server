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

/**
 * @author -Wooden-
 */
public final class SnoopQuit extends L2GameClientPacket
{

	private int _snoopID;

	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see l2server.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = L2World.getInstance().getPlayer(_snoopID);
		if (player == null)
		{
			return;
		}
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		player.removeSnooper(activeChar);
		activeChar.removeSnooped(player);
	}
}
