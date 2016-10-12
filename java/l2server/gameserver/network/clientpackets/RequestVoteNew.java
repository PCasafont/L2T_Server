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

import l2server.gameserver.model.L2Object;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.network.serverpackets.UserInfo;

public final class RequestVoteNew extends L2GameClientPacket
{
	private int _targetId;

	@Override
	protected void readImpl()
	{
		_targetId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		L2Object object = activeChar.getTarget();

		if (!(object instanceof L2PcInstance))
		{
			if (object == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SELECT_TARGET));
			}
			else
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			}
			return;
		}

		L2PcInstance target = (L2PcInstance) object;

		if (target.getObjectId() != _targetId)
		{
			return;
		}

		if (target == activeChar)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF));
			return;
		}

		if (activeChar.getRecomLeft() <= 0)
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOU_CURRENTLY_DO_NOT_HAVE_ANY_RECOMMENDATIONS));
			return;
		}

		if (target.getRecomHave() >= 255)
		{
			activeChar.sendPacket(
					SystemMessage.getSystemMessage(SystemMessageId.YOUR_TARGET_NO_LONGER_RECEIVE_A_RECOMMENDATION));
			return;
		}

		activeChar.giveRecom(target);

		SystemMessage sm = null;
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addNumber(activeChar.getRecomLeft());
		activeChar.sendPacket(sm);

		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(activeChar);
		target.sendPacket(sm);
		sm = null;

		activeChar.sendPacket(new UserInfo(activeChar));
		target.broadcastUserInfo();

		activeChar.sendPacket(new ExVoteSystemInfo(activeChar));
		target.sendPacket(new ExVoteSystemInfo(target));
	}
}
