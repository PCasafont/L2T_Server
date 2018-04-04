/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.TimeController;
import l2server.gameserver.model.actor.instance.Player;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.5.2.6 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharSelected extends L2GameServerPacket {
	// SdSddddddddddffddddddddddddddddddddddddddddddddddddddddd d
	private Player activeChar;
	private int sessionId;
	
	public CharSelected(Player cha, int sessionId) {
		activeChar = cha;
		this.sessionId = sessionId;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected final void writeImpl() {
		writeS(activeChar.getName());
		writeD(activeChar.getCharId()); // ??
		writeS(activeChar.getTitle());
		writeD(sessionId);
		writeD(activeChar.getClanId());
		writeD(0x00); // ??
		writeD(activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(activeChar.getRace().ordinal());
		writeD(activeChar.getCurrentClass().getId());
		writeD(0x01); // active ??
		writeD(activeChar.getX());
		writeD(activeChar.getY());
		writeD(activeChar.getZ());
		
		writeF(activeChar.getCurrentHp());
		writeF(activeChar.getCurrentMp());
		writeQ(activeChar.getSp());
		writeQ(activeChar.getExp());
		writeD(activeChar.getLevel());
		writeD(activeChar.getReputation()); // thx evill33t
		writeD(activeChar.getPkKills());
		
		writeD(TimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
		writeD(0x00);
		
		writeD(activeChar.getCurrentClass().getId());
		
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		
		writeB(new byte[64]);
		writeD(0x00);
	}
}
