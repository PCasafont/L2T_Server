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

package l2server.loginserver.network.serverpackets;

import l2server.loginserver.SessionKey;

/**
 *
 */
public final class PlayOk extends L2LoginServerPacket {
	private int playOk1, playOk2;
	
	public PlayOk(SessionKey sessionKey) {
		playOk1 = sessionKey.playOkID1;
		playOk2 = sessionKey.playOkID2;
	}
	
	@Override
	protected void write() {
		writeC(0x07);
		writeD(playOk1);
		writeD(playOk2);
	}
}
