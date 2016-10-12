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
import l2server.gameserver.network.serverpackets.ExRotation;
import l2server.gameserver.network.serverpackets.SocialAction;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.gameserver.util.Util;

/**
 * @author JIV
 */
public class AnswerCoupleAction extends L2GameClientPacket
{

	private int _charObjId;
	private int _actionId;
	private int _answer;

	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_answer = readD();
		_charObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_charObjId);
		if (activeChar == null || target == null)
		{
			return;
		}
		if (target.getMultiSocialTarget() != activeChar.getObjectId() || target.getMultiSociaAction() != _actionId)
		{
			return;
		}
		if (_answer == 0) // cancel
		{
			target.setMultiSocialAction(0, 0);
			target.sendPacket(SystemMessageId.COUPLE_ACTION_DENIED);
		}
		else if (_answer == 1) // approve
		{
			double distance = activeChar.getPlanDistanceSq(target);
			if (distance > 2000 || distance < 70)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
				target.sendPacket(SystemMessageId.TARGET_DO_NOT_MEET_LOC_REQUIREMENTS);
				return;
			}
			int heading = Util.calculateHeadingFrom(activeChar, target);
			activeChar.broadcastPacket(new ExRotation(activeChar.getObjectId(), heading));
			activeChar.setHeading(heading);
			heading = Util.calculateHeadingFrom(target, activeChar);
			target.setHeading(heading);
			target.broadcastPacket(new ExRotation(target.getObjectId(), heading));
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), _actionId));
			target.broadcastPacket(new SocialAction(target.getObjectId(), _actionId));
		}
		else if (_answer == -1) // refused
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_COUPLE_ACTIONS);
			sm.addPcName(activeChar);
			target.sendPacket(sm);
		}
	}
}
