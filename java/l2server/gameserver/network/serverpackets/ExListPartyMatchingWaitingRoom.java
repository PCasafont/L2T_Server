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

import l2server.gameserver.instancemanager.TownManager;
import l2server.gameserver.model.PartyMatchWaitingList;
import l2server.gameserver.model.actor.instance.L2PcInstance;

import java.util.ArrayList;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	@SuppressWarnings("unused")
	private final L2PcInstance _activeChar;
	@SuppressWarnings("unused")
	private int _page;
	private int _minlvl;
	private int _maxlvl;
	@SuppressWarnings("unused")
	private int _mode;
	private ArrayList<L2PcInstance> _members;

	public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minlvl, int maxlvl, int mode)
	{
		_activeChar = player;
		_page = page;
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_mode = mode;
		_members = new ArrayList<>();
		for (L2PcInstance cha : PartyMatchWaitingList.getInstance().getPlayers())
		{
			if (cha == null)
			{
				continue;
			}

			if (!cha.isPartyWaiting())
			{
				PartyMatchWaitingList.getInstance().removePlayer(cha);
				continue;
			}

			if (cha.getLevel() < _minlvl || cha.getLevel() > _maxlvl)
			{
				continue;
			}

			_members.add(cha);
		}
	}

	@Override
	protected final void writeImpl()
	{
		/*if (_mode == 0)
        {
			writeD(0);
			writeD(0);
			return;
		}*/

		writeD(_members.size());
		writeD(_members.size());
		for (L2PcInstance member : _members)
		{
			writeS(member.getName());
			writeD(member.getClassId());
			writeD(member.getLevel());
			writeD(TownManager.getClosestLocation(member));
			writeD(0x00); // ???
		}
	}
}
