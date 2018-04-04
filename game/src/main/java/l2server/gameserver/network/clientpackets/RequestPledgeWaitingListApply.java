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

import l2server.gameserver.instancemanager.ClanRecruitManager;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.serverpackets.ExPledgeRecruitApplyInfo;

/**
 * @author Pere
 */
public final class RequestPledgeWaitingListApply extends L2GameClientPacket {
	private boolean apply;
	private int karma;
	
	@Override
	protected void readImpl() {
		apply = readD() == 1;
		karma = readD();
	}
	
	@Override
	protected void runImpl() {
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.getClan() != null) {
			return;
		}
		
		if (apply) {
			if (ClanRecruitManager.getInstance().addWaitingUser(activeChar, karma)) {
			}
			sendPacket(new ExPledgeRecruitApplyInfo(3));
		} else {
			if (ClanRecruitManager.getInstance().removeWaitingUser(activeChar)) {
			}
			sendPacket(new ExPledgeRecruitApplyInfo(0));
		}
	}
}
