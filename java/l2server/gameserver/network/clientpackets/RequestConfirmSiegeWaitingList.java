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
import l2server.gameserver.instancemanager.CastleManager;
import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.entity.Castle;
import l2server.gameserver.network.serverpackets.SiegeDefenderList;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
	//

	private int approved;
	private int castleId;
	private int clanId;

	@Override
	protected void readImpl()
	{
		this.castleId = readD();
		this.clanId = readD();
		this.approved = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		// Check if the player has a clan
		if (activeChar.getClan() == null)
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(this.castleId);
		if (castle == null)
		{
			return;
		}

		// Check if leader of the clan who owns the castle?
		if (castle.getOwnerId() != activeChar.getClanId() || !activeChar.isClanLeader())
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClan(this.clanId);
		if (clan == null)
		{
			return;
		}

		if (!castle.getSiege().getIsRegistrationOver())
		{
			if (this.approved == 1)
			{
				if (castle.getSiege().checkIsDefenderWaiting(clan))
				{
					castle.getSiege().approveSiegeDefenderClan(this.clanId);
				}
				else
				{
					return;
				}
			}
			else
			{
				if (castle.getSiege().checkIsDefenderWaiting(clan) || castle.getSiege().checkIsDefender(clan))
				{
					castle.getSiege().removeSiegeClan(this.clanId);
				}
			}
		}

		//Update the defender list
		activeChar.sendPacket(new SiegeDefenderList(castle));
	}
}
