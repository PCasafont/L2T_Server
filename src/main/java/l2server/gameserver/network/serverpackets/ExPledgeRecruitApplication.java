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
	private ClanRecruitWaitingUser applicant;

	public ExPledgeRecruitApplication(ClanRecruitWaitingUser applicant)
	{
		this.applicant = applicant;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(applicant.recruitData.clan.getClanId());
		writeS(applicant.recruitData.clan.getName());
		writeS(applicant.recruitData.clan.getLeaderName());
		writeD(applicant.recruitData.clan.getLevel());
		writeD(applicant.recruitData.clan.getMembersCount());
		writeD(applicant.recruitData.karma);
		writeS(applicant.recruitData.introduction);
		writeS(applicant.application);
	}
}
