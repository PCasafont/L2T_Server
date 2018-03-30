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

package handlers.usercommandhandlers;

import l2server.gameserver.handler.IUserCommandHandler;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.model.olympiad.Olympiad;
import l2server.gameserver.model.olympiad.OlympiadNobleInfo;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.serverpackets.SystemMessage;

/**
 * Support for /olympiadstat command
 * Added by kamy
 */
public class OlympiadStat implements IUserCommandHandler {
	private static final int[] COMMAND_IDS = {109};

	/**
	 * @see l2server.gameserver.handler.IUserCommandHandler#useUserCommand(int, l2server.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar) {
		if (id != COMMAND_IDS[0]) {
			return false;
		}

		L2PcInstance noble = activeChar;
		if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance) {
			noble = activeChar.getTarget().getActingPlayer();
		}

		OlympiadNobleInfo nobleInfo = Olympiad.getInstance().getNobleInfo(noble.getObjectId());
		if (nobleInfo == null) {
			activeChar.sendMessage("This target did not participate to the Olympiads yet.");
			return false;
		}

		// TODO Retail system message says YOU even on other targets' info
		SystemMessage sm =
				SystemMessage.getSystemMessage(SystemMessageId.THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS);
		sm.addNumber(nobleInfo.getMatches());
		sm.addNumber(nobleInfo.getVictories());
		sm.addNumber(nobleInfo.getDefeats());
		sm.addNumber(nobleInfo.getPoints());
		activeChar.sendPacket(sm);
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_S1_MATCHES_S2_1V1_CLASS_S3_1V1_S4_TEAM);
		sm.addNumber(Olympiad.MAX_WEEKLY_MATCHES - nobleInfo.getMatchesThisWeek());
		sm.addNumber(0);
		sm.addNumber(0);
		sm.addNumber(0);
		activeChar.sendPacket(sm);
		return true;
	}

	/**
	 * @see l2server.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList() {
		return COMMAND_IDS;
	}
}
