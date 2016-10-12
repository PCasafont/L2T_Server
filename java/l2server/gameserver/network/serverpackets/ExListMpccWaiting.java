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

import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Pere
 */
public class ExListMpccWaiting extends L2GameServerPacket
{
	@SuppressWarnings("unused")
	private final L2PcInstance _activeChar;
	@SuppressWarnings("unused")
	private int _page;
	@SuppressWarnings("unused")
	private int _location;
	@SuppressWarnings("unused")
	private int _anyLevel;

	public ExListMpccWaiting(L2PcInstance player, int page, int location, int anyLevel)
	{
		_activeChar = player;
		_page = page;
		_location = location;
		_anyLevel = anyLevel;
	}

	@Override
	protected final void writeImpl()
	{
	}
}
