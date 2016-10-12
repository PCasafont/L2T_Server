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

public class ManagePledgePower extends L2GameServerPacket
{

	private int _action;
	private L2Clan _clan;
	private int _rank;
	private int _privs;

	public ManagePledgePower(L2Clan clan, int action, int rank)
	{
		_clan = clan;
		_action = action;
		_rank = rank;
	}

	@Override
	protected final void writeImpl()
	{
		if (_action == 1)
		{
			_privs = _clan.getRankPrivs(_rank);
		}
		else
		{
			return;
			/*
            if (L2World.getInstance().findObject(_clanId) == null)
				return;

			privs = ((L2PcInstance)L2World.getInstance().findObject(_clanId)).getClanPrivileges();
			 */
		}

		writeD(0);
		writeD(0);
		writeD(_privs);
	}
}
