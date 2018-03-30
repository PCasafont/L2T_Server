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

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class CreatureSay extends L2GameServerPacket {
	// ddSS
	private int objectId;
	private int textType;
	private String charName = null;
	private int charId = 0;
	private String text = null;
	private int msgId = -1;
	private byte level = 0;
	
	/**
	 */
	public CreatureSay(int objectId, int messageType, String charName, String text) {
		this.objectId = objectId;
		textType = messageType;
		this.charName = charName;
		this.text = text;
	}
	
	public CreatureSay(L2Character activeChar, int messageType, String charName, String text) {
		objectId = activeChar.getObjectId();
		textType = messageType;
		this.charName = charName;
		this.text = text;
		level = (byte) activeChar.getLevel();
	}
	
	public CreatureSay(int objectId, int messageType, int charId, int msgId) {
		this.objectId = objectId;
		textType = messageType;
		this.charId = charId;
		this.msgId = msgId;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(objectId);
		writeD(textType);
		if (charName != null) {
			writeS(charName);
		} else {
			writeD(charId);
		}
		writeD(msgId);
		if (text != null) {
			writeS(text);
		}
		writeC(0x00);
		writeC(level);
	}
	
	@Override
	public final void runImpl() {
		L2PcInstance pci = getClient().getActiveChar();
		if (pci != null) {
			pci.broadcastSnoop(textType, charName, text);
		}
	}
}
