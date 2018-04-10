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

import l2server.Config;
import l2server.loginserver.GameServerTable;
import l2server.loginserver.GameServerThread;
import l2server.network.BaseRecievePacket;
import org.slf4j.LoggerFactory;

/**
 * @author -Wooden-
 */
public class PlayerLogout extends BaseRecievePacket {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(PlayerLogout.class.getName());

	public PlayerLogout(byte[] decrypt, GameServerThread server) {
		super(decrypt);
		String account = readS();

		server.removeAccountOnGameServer(account);
		if (Config.DEBUG) {
			log.info("Player " + account + " logged out from gameserver [" + server.getServerId() + "] " +
					GameServerTable.getInstance().getServerNameById(server.getServerId()));
		}
	}
}
