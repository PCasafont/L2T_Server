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
import l2server.gameserver.model.actor.instance.Player;

/**
 * @author -Wooden-
 */
public final class PledgeShowMemberListUpdate extends L2GameServerPacket {
	private int pledgeType;
	private int hasSponsor;
	private String name;
	private int level;
	private int classId;
	private int objectId;
	private boolean isOnline;
	private int race;
	private int sex;
	
	public PledgeShowMemberListUpdate(Player player) {
		pledgeType = player.getPledgeType();
		if (pledgeType == L2Clan.SUBUNIT_ACADEMY) {
			hasSponsor = player.getSponsor() != 0 ? 1 : 0;
		} else {
			hasSponsor = 0;
		}
		name = player.getName();
		level = player.getLevel();
		classId = player.getCurrentClass().getId();
		race = player.getRace().ordinal();
		sex = player.getAppearance().getSex() ? 1 : 0;
		objectId = player.getObjectId();
		isOnline = player.isOnline();
	}
	
	public PledgeShowMemberListUpdate(L2ClanMember member) {
		name = member.getName();
		level = member.getLevel();
		classId = member.getCurrentClass();
		objectId = member.getObjectId();
		isOnline = member.isOnline();
		pledgeType = member.getPledgeType();
		race = member.getRaceOrdinal();
		sex = member.getSex() ? 1 : 0;
		if (pledgeType == L2Clan.SUBUNIT_ACADEMY) {
			hasSponsor = member.getSponsor() != 0 ? 1 : 0;
		} else {
			hasSponsor = 0;
		}
	}
	
	@Override
	protected final void writeImpl() {
		writeS(name);
		writeD(level);
		writeD(classId);
		writeD(sex);
		writeD(race);
		if (isOnline) {
			writeD(objectId);
			writeD(pledgeType);
		} else {
			// when going offline send as 0
			writeD(0);
			writeD(0);
		}
		writeD(hasSponsor);
	}
}
