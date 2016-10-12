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
import l2server.gameserver.network.serverpackets.ExPledgeWaitingListSearch;

/**
 * @author Pere
 */
public final class RequestPledgeWaitingListSearch extends L2GameClientPacket
{
	private int _minLevel;
	private int _maxLevel;
	private int _role;
	private int _sortBy;
	private boolean _desc;
	private String _name;

	@Override
	protected void readImpl()
	{
		_minLevel = readD();
		_maxLevel = readD();
		_role = readD();
		_name = readS();
		_sortBy = readD();
		_desc = readD() == 1;
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

		sendPacket(new ExPledgeWaitingListSearch(_minLevel, _maxLevel, _role, _sortBy, _desc, _name));
	}
}
