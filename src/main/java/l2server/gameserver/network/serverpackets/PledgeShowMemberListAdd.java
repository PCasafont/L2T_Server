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

import l2server.gameserver.model.L2ClanMember;
import l2server.gameserver.model.actor.instance.Player;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class PledgeShowMemberListAdd extends L2GameServerPacket {
	private String name;
	private int lvl;
	private int classId;
	private int isOnline;
	private int pledgeType;
	
	public PledgeShowMemberListAdd(Player player) {
		name = player.getName();
		lvl = player.getLevel();
		classId = player.getCurrentClass().getId();
		isOnline = player.isOnline() ? player.getObjectId() : 0;
		pledgeType = player.getPledgeType();
	}
	
	public PledgeShowMemberListAdd(L2ClanMember cm) {
		name = cm.getName();
		lvl = cm.getLevel();
		classId = cm.getCurrentClass();
		isOnline = cm.isOnline() ? cm.getObjectId() : 0;
		pledgeType = cm.getPledgeType();
	}
	
	@Override
	protected final void writeImpl() {
		writeS(name);
		writeD(lvl);
		writeD(classId);
		writeD(0);
		writeD(1);
		writeD(isOnline); // 1=online 0=offline
		writeD(pledgeType);
	}
}
