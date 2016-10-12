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
import l2server.gameserver.network.serverpackets.ExDivideAdenaStart;

/**
 * @author Pere
 */
public final class RequestDivideAdena extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (!player.isInParty())
		{
			player.sendMessage("You cannot proceed as you are not in a party."); // TODO systemmessageid
			return;
		}

		if (!player.getParty().isLeader(player))
		{
			player.sendMessage("You cannot proceed as you are not a party leader."); // TODO systemmessageid
			return;
		}

		player.sendPacket(new ExDivideAdenaStart());
	}
}
