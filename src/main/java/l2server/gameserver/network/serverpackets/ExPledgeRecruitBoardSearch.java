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
import l2server.gameserver.instancemanager.ClanRecruitManager.ClanRecruitData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pere
 */
public class ExPledgeRecruitBoardSearch extends L2GameServerPacket
{
	private int page;
	private int pageCount;
	private List<ClanRecruitData> data;

	public ExPledgeRecruitBoardSearch(int level, int karma, boolean clanName, String name, int sortBy, boolean desc, int page)
	{
		this.page = page;
		List<ClanRecruitData> list =
				ClanRecruitManager.getInstance().getRecruitData(level, karma, clanName, name, sortBy, desc);
		pageCount = (list.size() - 1) / 12 + 1;

		data = new ArrayList<>();
		int index = (page - 1) * 12;
		while (index < page * 12 && index < list.size())
		{
			data.add(list.get(index));
			index++;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(page);
		writeD(pageCount);
		writeD(data.size());

		for (ClanRecruitData data : data)
		{
			writeD(data.clan.getClanId());
			writeD(data.clan.getAllyId());
		}

		for (ClanRecruitData data : data)
		{
			writeD(data.clan.getCrestId());
			writeD(data.clan.getAllyCrestId());
			writeS(data.clan.getName());
			writeS(data.clan.getLeaderName());
			writeD(data.clan.getLevel());
			writeD(data.clan.getMembersCount());
			writeD(data.karma);
			writeS(data.introduction);
		}
	}
}
