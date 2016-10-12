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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.instancemanager.ClanRecruitManager;
import l2server.gameserver.instancemanager.ClanRecruitManager.ClanRecruitWaitingUser;

import java.util.List;

/**
 * @author Pere
 */
public class ExPledgeWaitingListSearch extends L2GameServerPacket
{
	private List<ClanRecruitWaitingUser> _users;

	public ExPledgeWaitingListSearch(int minLevel, int maxLevel, int role, int sortBy, boolean desc, String name)
	{
		_users = ClanRecruitManager.getInstance().getWaitingUsers(minLevel, maxLevel, role, sortBy, desc, name);
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_users.size());

		for (ClanRecruitWaitingUser user : _users)
		{
			writeD(user.id);
			writeS(user.name);
			writeD(user.karma);
			writeD(user.classId);
			writeD(user.level);
		}
	}
}
