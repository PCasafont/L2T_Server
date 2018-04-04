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

import l2server.gameserver.model.actor.instance.Player;

/**
 * @author Pere
 */
public class ExListMpccWaiting extends L2GameServerPacket {
	@SuppressWarnings("unused")
	private final Player activeChar;
	@SuppressWarnings("unused")
	private int page;
	@SuppressWarnings("unused")
	private int location;
	@SuppressWarnings("unused")
	private int anyLevel;
	
	public ExListMpccWaiting(Player player, int page, int location, int anyLevel) {
		activeChar = player;
		this.page = page;
		this.location = location;
		this.anyLevel = anyLevel;
	}
	
	@Override
	protected final void writeImpl() {
	}
}
