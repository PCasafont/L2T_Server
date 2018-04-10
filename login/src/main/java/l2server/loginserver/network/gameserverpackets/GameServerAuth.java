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
import l2server.loginserver.GameServerTable.GameServerInfo;
import l2server.loginserver.GameServerThread;
import l2server.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import l2server.loginserver.network.loginserverpackets.AuthResponse;
import l2server.loginserver.network.loginserverpackets.LoginServerFail;
import l2server.network.BaseRecievePacket;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Format: cccddb
 * c desired ID
 * c accept alternative ID
 * c reserve Host
 * s ExternalHostName
 * s InetranlHostName
 * d max players
 * d hexid size
 * b hexid
 *
 * @author -Wooden-
 */
public class GameServerAuth extends BaseRecievePacket {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(GameServerAuth.class.getName());

	GameServerThread server;
	private byte[] hexId;
	private int desiredId;
	@SuppressWarnings("unused")
	private boolean hostReserved;
	private boolean acceptAlternativeId;
	private int maxPlayers;
	private int port;
	private String[] hosts;

	public GameServerAuth(byte[] decrypt, GameServerThread server) {
		super(decrypt);
		this.server = server;
		desiredId = readC();
		acceptAlternativeId = readC() != 0;
		hostReserved = readC() != 0;
		port = readH();
		maxPlayers = readD();
		int size = readD();
		hexId = readB(size);
		size = 2 * readD();
		hosts = new String[size];
		for (int i = 0; i < size; i++) {
			hosts[i] = readS();
		}

		if (Config.DEBUG) {
			log.info("Auth request received");
		}

		if (handleRegProcess()) {
			AuthResponse ar = new AuthResponse(server.getGameServerInfo().getId());
			server.sendPacket(ar);
			if (Config.DEBUG) {
				log.info("Authed: id: " + server.getGameServerInfo().getId());
			}
			server.setLoginConnectionState(GameServerState.AUTHED);
		}
	}

	private boolean handleRegProcess() {
		GameServerTable gameServerTable = GameServerTable.getInstance();

		int id = desiredId;
		byte[] hexId = this.hexId;

		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null) {
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId)) {
				// check to see if this GS is already connected
				synchronized (gsi) {
					if (gsi.isAuthed()) {
						server.forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
						return false;
					} else {
						server.attachGameServerInfo(gsi, port, hosts, maxPlayers);
					}
				}
			} else {
				// there is already a server registered with the desired id and different hex id
				// try to register this one with an alternative id
				if (Config.ACCEPT_NEW_GAMESERVER && acceptAlternativeId) {
					gsi = new GameServerInfo(id, hexId, server);
					if (gameServerTable.registerWithFirstAvaliableId(gsi)) {
						server.attachGameServerInfo(gsi, port, hosts, maxPlayers);
						gameServerTable.registerServerOnDB(gsi);
					} else {
						server.forceClose(LoginServerFail.REASON_NO_FREE_ID);
						return false;
					}
				} else {
					// server id is already taken, and we cant get a new one for you
					server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
					return false;
				}
			}
		} else {
			// can we register on this id?
			if (Config.ACCEPT_NEW_GAMESERVER) {
				gsi = new GameServerInfo(id, hexId, server);
				if (gameServerTable.register(id, gsi)) {
					server.attachGameServerInfo(gsi, port, hosts, maxPlayers);
					gameServerTable.registerServerOnDB(gsi);
				} else {
					// some one took this ID meanwhile
					server.forceClose(LoginServerFail.REASON_ID_RESERVED);
					return false;
				}
			} else {
				server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
				return false;
			}
		}

		return true;
	}
}
