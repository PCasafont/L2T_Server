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

import l2server.gameserver.model.PartyMatchRoom;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Gnacik
 */
public class PartyMatchDetail extends L2GameServerPacket {
	private PartyMatchRoom room;
	
	/**
	 */
	public PartyMatchDetail(L2PcInstance player, PartyMatchRoom room) {
		this.room = room;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(room.getId()); //	Room ID
		writeD(room.getMaxMembers()); //	Max Members
		writeD(room.getMinLvl()); //	Level Min
		writeD(room.getMaxLvl()); //	Level Max
		writeD(room.getLootType()); //	Loot Type
		writeD(room.getLocation()); //	Room Location
		writeS(room.getTitle()); //	Room title
		writeC(0x05); // ???
		writeC(0x01); // ???
	}
}
