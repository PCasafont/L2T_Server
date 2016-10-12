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
	private String _name;
	private int _lvl;
	private int _classId;
	private int _isOnline;
	private int _pledgeType;

	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getCurrentClass().getId();
		_isOnline = player.isOnline() ? player.getObjectId() : 0;
		_pledgeType = player.getPledgeType();
	}

	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getCurrentClass();
		_isOnline = cm.isOnline() ? cm.getObjectId() : 0;
		_pledgeType = cm.getPledgeType();
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(0);
		writeD(1);
		writeD(_isOnline); // 1=online 0=offline
		writeD(_pledgeType);
	}
}
