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

import l2server.gameserver.model.BlockList;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.ExMentorAdd;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Erlandys
 */
public class RequestMenteeAdd extends L2GameClientPacket
{
	String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			return;
		}

		final L2PcInstance mentee = L2World.getInstance().getPlayer(_name);

		SystemMessage sm;
		// can't use mentee invite for locating invisible characters
		if (mentee == null || !mentee.isOnline() || mentee.getAppearance().getInvisible())
		{
			//Target is not found in the game.
			activeChar.sendPacket(
					SystemMessageId.THE_USER_YOU_REQUESTED_IS_NOT_IN_GAME); // TODO: Find other message not friends.
			return;
		}
		else if (mentee == activeChar)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_BECOME_YOUR_OWN_MENTEE);
			return;
		}
		else if (BlockList.isBlocked(activeChar, mentee))
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.BLOCKED_C1);
			sm.addCharName(mentee);
			activeChar.sendPacket(sm);
			return;
		}
		else if (BlockList.isBlocked(mentee, activeChar))
		{
			activeChar.sendMessage("You are in target's block list.");
			return;
		}
		else if (mentee.isSubClassActive())
		{
			activeChar.sendPacket(SystemMessageId.INVITATION_CAN_OCCUR_ONLY_WHEN_THE_MENTEE_IS_IN_MAIN_CLASS_STATUS);
			return;
		}
		else if (mentee.getLevel() > 85)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ABOVE_LEVEL_86_AND_CANNOT_BECOME_A_MENTEE);
			sm.addCharName(mentee);
			activeChar.sendPacket(sm);
			return;
		}

		if (activeChar.getMenteeList().contains(mentee.getObjectId()))
		{
			// Player already is in your menteelist
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_HAS_A_MENTOR);
			sm.addString(mentee.getName());
			activeChar.sendPacket(sm);
			return;
		}
		else if (mentee.isMentee())
		{
			sm = SystemMessage
					.getSystemMessage(SystemMessageId.S1_ALREADY_HAS_MENTORING_RELATIONSHIP_WITH_ANOTHER_CHARACTER);
			sm.addCharName(mentee);
			activeChar.sendPacket(sm);
			return;
		}
		else if (activeChar.getClassId() < 139)
		{
			activeChar.sendPacket(SystemMessageId.YOU_MUST_AWAKEN_IN_ORDER_TO_BECOM_A_MENTOR);
			return;
		}
		else if (activeChar.getMenteeList().size() >= 3)
		{
			activeChar.sendPacket(SystemMessageId.A_MENTOR_CAN_HAVE_UP_TO_3_MENTEES_AT_THE_SAME_TIME);
			return;
		}

		if (!mentee.isProcessingRequest())
		{
			// requets to become mentee
			activeChar.onTransactionRequest(mentee);
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OFFERED_TO_BECOME_S1_MENTOR);
			sm.addString(mentee.getName());
			mentee.sendPacket(new ExMentorAdd(activeChar));
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(mentee.getName());
		}
		activeChar.sendPacket(sm);
	}
}
