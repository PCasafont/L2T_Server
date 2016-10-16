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
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public final class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private String name;
	private int lvl;
	private int classId;
	private int isOnline;
	private int pledgeType;

	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		this.name = player.getName();
		this.lvl = player.getLevel();
		this.classId = player.getCurrentClass().getId();
		this.isOnline = player.isOnline() ? player.getObjectId() : 0;
		this.pledgeType = player.getPledgeType();
	}

	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		this.name = cm.getName();
		this.lvl = cm.getLevel();
		this.classId = cm.getCurrentClass();
		this.isOnline = cm.isOnline() ? cm.getObjectId() : 0;
		this.pledgeType = cm.getPledgeType();
	}

	@Override
	protected final void writeImpl()
	{
		writeS(this.name);
		writeD(this.lvl);
		writeD(this.classId);
		writeD(0);
		writeD(1);
		writeD(this.isOnline); // 1=online 0=offline
		writeD(this.pledgeType);
	}
}
