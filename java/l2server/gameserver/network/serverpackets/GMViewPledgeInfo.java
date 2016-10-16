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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * format   SdSS dddddddd d (Sddddd)
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewPledgeInfo extends L2GameServerPacket
{
	private L2Clan clan;
	private L2PcInstance activeChar;

	public GMViewPledgeInfo(L2Clan clan, L2PcInstance activeChar)
	{
		this.clan = clan;
		this.activeChar = activeChar;
	}

	@Override
	protected final void writeImpl()
	{
		writeS(this.activeChar.getName());
		writeD(this.clan.getClanId());
		writeD(0x00);
		writeS(this.clan.getName());
		writeS(this.clan.getLeaderName());
		writeD(this.clan.getCrestId()); // -> no, it's no longer used (nuocnam) fix by game
		writeD(this.clan.getLevel());
		writeD(this.clan.getHasCastle());
		writeD(this.clan.getHasHideout());
		writeD(this.clan.getHasFort());
		writeD(this.clan.getRank());
		writeD(this.clan.getReputationScore());
		writeD(0);
		writeD(0);

		writeD(this.clan.getAllyId()); //c2
		writeS(this.clan.getAllyName()); //c2
		writeD(this.clan.getAllyCrestId()); //c2
		writeD(this.clan.isAtWar() ? 1 : 0); //c3
		writeD(0); // T3 Unknown
		//writeD(0); // GoD ???
		writeD(this.clan.getMembers().length);

		for (L2ClanMember member : this.clan.getMembers())
		{
			if (member != null)
			{
				writeS(member.getName());
				writeD(member.getLevel());
				writeD(member.getCurrentClass());
				writeD(member.getSex() ? 1 : 0);
				writeD(member.getRaceOrdinal());
				writeD(member.isOnline() ? member.getObjectId() : 0);
				writeC(0x00); // ??? Activity?
				writeD(member.getSponsor() != 0 ? 1 : 0);
			}
		}
	}
}
