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

package l2server.loginserver.network.gameserverpackets;

import l2server.L2DatabaseFactory;
import l2server.loginserver.LoginController;
import l2server.util.network.BaseRecievePacket;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author mrTJO
 */
public class RequestTempBan extends BaseRecievePacket {
	String accountName, banReason, ip;
	long banTime;

	/**
	 * @param decrypt
	 */
	public RequestTempBan(byte[] decrypt) {
		super(decrypt);
		accountName = readS();
		ip = readS();
		banTime = readQ();
		boolean haveReason = readC() != 0;
		if (haveReason) {
			banReason = readS();
		}
		banUser();
	}

	private void banUser() {
		Connection con = null;
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO account_data VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?");
			statement.setString(1, accountName);
			statement.setString(2, "ban_temp");
			statement.setString(3, Long.toString(banTime));
			statement.setString(4, Long.toString(banTime));
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			L2DatabaseFactory.close(con);
		}

		try {
			LoginController.getInstance().addBanForAddress(ip, banTime);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
