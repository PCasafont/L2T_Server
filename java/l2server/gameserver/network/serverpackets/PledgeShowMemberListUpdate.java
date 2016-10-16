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
 * @author -Wooden-
 */
public final class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private int pledgeType;
	private int hasSponsor;
	private String name;
	private int level;
	private int classId;
	private int objectId;
	private boolean isOnline;
	private int race;
	private int sex;

	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		this.pledgeType = player.getPledgeType();
		if (this.pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			this.hasSponsor = player.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			this.hasSponsor = 0;
		}
		this.name = player.getName();
		this.level = player.getLevel();
		this.classId = player.getCurrentClass().getId();
		this.race = player.getRace().ordinal();
		this.sex = player.getAppearance().getSex() ? 1 : 0;
		this.objectId = player.getObjectId();
		this.isOnline = player.isOnline();
	}

	public PledgeShowMemberListUpdate(L2ClanMember member)
	{
		this.name = member.getName();
		this.level = member.getLevel();
		this.classId = member.getCurrentClass();
		this.objectId = member.getObjectId();
		this.isOnline = member.isOnline();
		this.pledgeType = member.getPledgeType();
		this.race = member.getRaceOrdinal();
		this.sex = member.getSex() ? 1 : 0;
		if (this.pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			this.hasSponsor = member.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			this.hasSponsor = 0;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeS(this.name);
		writeD(this.level);
		writeD(this.classId);
		writeD(this.sex);
		writeD(this.race);
		if (this.isOnline)
		{
			writeD(this.objectId);
			writeD(this.pledgeType);
		}
		else
		{
			// when going offline send as 0
			writeD(0);
			writeD(0);
		}
		writeD(this.hasSponsor);
	}
}
