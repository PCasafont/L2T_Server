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
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.AskJoinPledge;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestJoinPledge extends L2GameClientPacket
{

	private int _target;
	private int _pledgeType;

	@Override
	protected void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		final L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}

		final L2PcInstance target = L2World.getInstance().getPlayer(_target);
		if (target == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return;
		}

		// LasTravel
		if (target.getIsRefusingRequests())
		{
			activeChar.sendMessage("Your target have the requests blocked!");
			return;
		}

		if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
		{
			return;
		}

		if (!activeChar.getRequest().setRequest(target, this))
		{
			return;
		}

		final String pledgeName = activeChar.getClan().getName();
		final String subPledgeName = activeChar.getClan().getSubPledge(_pledgeType) != null ?
				activeChar.getClan().getSubPledge(_pledgeType).getName() : null;
		target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), subPledgeName, _pledgeType, pledgeName));
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}
}
