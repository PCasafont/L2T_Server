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
import l2server.gameserver.model.base.SubClass;

/**
 * @author Pere
 */
public final class ExSubjobInfo extends L2GameServerPacket {
	private Player activeChar;
	
	public ExSubjobInfo(Player activeChar) {
		this.activeChar = activeChar;
	}
	
	@Override
	protected final void writeImpl() {
		writeC(0x00); // GoD ???
		writeD(activeChar.getCurrentClass().getId()); // Current Class
		writeD(0x00); // GoD ???
		
		writeD(activeChar.getSubClasses().size() + 1); // Class amount
		
		writeD(0x00); // Base class index
		writeD(activeChar.getBaseClass());
		writeD(activeChar.getBaseClassLevel());
		writeC(0x00); // 0x00 Red (base)
		for (SubClass sc : activeChar.getSubClasses().values()) {
			writeD(sc.getClassIndex());
			writeD(sc.getClassId());
			writeD(sc.getLevel());
			writeC(sc.isDual() ? 0x01 : 0x02);
		}
	}
}
