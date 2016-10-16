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
import l2server.gameserver.network.serverpackets.ExPledgeRecruitBoardSearch;

/**
 * @author Pere
 */
public final class RequestPledgeRecruitBoardSearch extends L2GameClientPacket
{
	private int level;
	private int karma;
	private boolean clanName;
	private String name;
	private int sortBy;
	private boolean desc;
	private int page;

	@Override
	protected void readImpl()
	{
		level = readD();
		karma = readD();
		clanName = readD() == 1;
		name = readS();
		sortBy = readD();
		desc = readD() == 1;
		page = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}

		if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction("clanRecruitSearch"))
		{
			return;
		}

		sendPacket(new ExPledgeRecruitBoardSearch(level, karma, clanName, name, sortBy, desc, page));
	}
}
