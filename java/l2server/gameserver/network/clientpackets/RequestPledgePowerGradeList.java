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
import l2server.gameserver.model.L2Clan.RankPrivs;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.PledgePowerGradeList;

/**
 * Format: (ch)
 *
 * @author -Wooden-
 */
public final class RequestPledgePowerGradeList extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// trigger
	}

	/**
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		L2Clan clan = player.getClan();
		if (clan != null)
		{
			RankPrivs[] privs = clan.getAllRankPrivs();
			player.sendPacket(new PledgePowerGradeList(privs));
			//Logozo.warning("plegdepowergradelist send, privs length: "+privs.length);
		}
	}
}
