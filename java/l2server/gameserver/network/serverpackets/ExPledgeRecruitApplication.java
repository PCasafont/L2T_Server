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

import l2server.gameserver.instancemanager.ClanRecruitManager.ClanRecruitWaitingUser;

/**
 * @author Pere
 */
public class ExPledgeRecruitApplication extends L2GameServerPacket
{
	private ClanRecruitWaitingUser _applicant;

	public ExPledgeRecruitApplication(ClanRecruitWaitingUser applicant)
	{
		_applicant = applicant;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_applicant.recruitData.clan.getClanId());
		writeS(_applicant.recruitData.clan.getName());
		writeS(_applicant.recruitData.clan.getLeaderName());
		writeD(_applicant.recruitData.clan.getLevel());
		writeD(_applicant.recruitData.clan.getMembersCount());
		writeD(_applicant.recruitData.karma);
		writeS(_applicant.recruitData.introduction);
		writeS(_applicant.application);
	}
}
