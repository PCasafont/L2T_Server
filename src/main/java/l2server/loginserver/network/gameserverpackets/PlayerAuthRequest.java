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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import l2server.loginserver.GameServerThread;
import l2server.loginserver.LoginController;
import l2server.loginserver.SessionKey;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.loginserverpackets.PlayerAuthResponse;
import l2server.util.network.BaseRecievePacket;

/**
 * @author -Wooden-
 */
public class PlayerAuthRequest extends BaseRecievePacket {
	private static Logger log = LoggerFactory.getLogger(PlayerAuthRequest.class.getName());


	/**
	 * @param decrypt
	 */
	public PlayerAuthRequest(byte[] decrypt, GameServerThread server) {
		super(decrypt);
		String account = readS();
		int playKey1 = readD();
		int playKey2 = readD();
		int loginKey1 = readD();
		int loginKey2 = readD();
		SessionKey sessionKey = new SessionKey(loginKey1, loginKey2, playKey1, playKey2);

		PlayerAuthResponse authResponse;
		if (Config.DEBUG) {
			log.info("auth request received for Player " + account);
		}

		L2LoginClient client = LoginController.getInstance().getClientForKey(sessionKey);
		if (client != null) {
			if (Config.DEBUG) {
				log.info("auth request: OK");
			}
			LoginController.getInstance().removeAuthedLoginClient(client.getAccount());
			if (account.equalsIgnoreCase("IdEmpty")) {
				authResponse = new PlayerAuthResponse(client.getAccount() + ";" + playKey1, true);
			} else {
				authResponse = new PlayerAuthResponse(account, true);
			}
		} else {
			if (Config.DEBUG) {
				log.info("auth request: NO");
				log.info("session key sent: " + sessionKey);
			}
			authResponse = new PlayerAuthResponse(account, false);
		}
		server.sendPacket(authResponse);
	}
}
