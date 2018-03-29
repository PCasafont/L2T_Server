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
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.serverpackets.ManagePledgePower;

import java.util.logging.Logger;

public final class RequestPledgePower extends L2GameClientPacket
{
	static Logger log = Logger.getLogger(ManagePledgePower.class.getName());
	private int rank;
	private int action;
	private int privs;

	@Override
	protected void readImpl()
	{
		rank = readD();
		action = readD();
		if (action == 2)
		{
			privs = readD();
		}
		else
		{
			privs = 0;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}

		if (action == 2)
		{
			if (player.getClan() != null && player.isClanLeader())
			{
				if (rank == 9)
				{
					//The rights below cannot be bestowed upon Academy members:
					//Join a clan or be dismissed
					//Title management, crest management, master management, level management,
					//bulletin board administration
					//Clan war, right to dismiss, set functions
					//Auction, manage taxes, attack/defend registration, mercenary management
					//=> Leaves only CP_CL_VIEW_WAREHOUSE, CP_CH_OPEN_DOOR, CP_CS_OPEN_DOOR?
					privs = (privs & L2Clan.CP_CL_VIEW_WAREHOUSE) + (privs & L2Clan.CP_CH_OPEN_DOOR) +
							(privs & L2Clan.CP_CS_OPEN_DOOR);
				}
				player.getClan().setRankPrivs(rank, privs);
			}
		}
		else
		{
			ManagePledgePower mpp = new ManagePledgePower(getClient().getActiveChar().getClan(), action, rank);
			player.sendPacket(mpp);
		}
	}
}
