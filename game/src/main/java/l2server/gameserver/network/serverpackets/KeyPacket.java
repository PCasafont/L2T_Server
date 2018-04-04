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

import l2server.Config;

/**
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class KeyPacket extends L2GameServerPacket {

	private byte[] key;
	private int id;

	public KeyPacket(byte[] key, int id) {
		this.key = key;
		this.id = id;
	}

	public KeyPacket(byte data[]) {
		key = data;
		id = 2;
	}

	@Override
	protected final void writeImpl() {
		if (id == 2) {
			writeC(key == null ? 0x00 : 0x01);
			if (key != null) {
				writeB(key);
			}
		} else {
			writeC(id); //0 - wrong protocol, 1 - protocol ok
			for (int i = 0; i < 8; i++) {
				writeC(key[i]); // key
			}
		}
		writeD(0x01);
		writeD(0x01); // server id
		writeC(0x01);
		writeD(0x00); // obfuscation key
		writeC(Config.IS_CLASSIC ? 0x01 : 0x00); // is classic
		writeC(0x00); // ???
	}
}
