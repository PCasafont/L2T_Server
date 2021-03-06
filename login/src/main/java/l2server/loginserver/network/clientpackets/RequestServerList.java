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

package l2server.loginserver.network.clientpackets;

import l2server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2server.loginserver.network.serverpackets.ServerList;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 */
public class RequestServerList extends L2LoginClientPacket {
	private int skey1;
	private int skey2;
	private int data3;
	
	public int getSessionKey1() {
		return skey1;
	}
	
	public int getSessionKey2() {
		return skey2;
	}
	
	public int getData3() {
		return data3;
	}
	
	@Override
	public boolean readImpl() {
		if (super.buf.remaining() >= 8) {
			skey1 = readD(); // loginOk 1
			skey2 = readD(); // loginOk 2
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void run() {
		if (getClient().getSessionKey().checkLoginPair(skey1, skey2)) {
			getClient().sendPacket(new ServerList(getClient()));
		} else {
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
