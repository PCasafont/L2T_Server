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

import l2server.gameserver.model.L2Clan.RankPrivs;

/**
 * sample
 * 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72	.....H.a.m.b.u.r
 * 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000
 * 00 00
 * 00000000										   ...
 * <p>
 * format   dd ??
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgePowerGradeList extends L2GameServerPacket
{
	private RankPrivs[] _privs;

	public PledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_privs.length);
		for (RankPrivs temp : _privs)
		{
			writeD(temp.getRank());
			writeD(temp.getParty());
		}
	}
}
