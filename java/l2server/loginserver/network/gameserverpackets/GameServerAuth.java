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
import l2server.log.Log;
import l2server.loginserver.GameServerTable;
import l2server.loginserver.GameServerTable.GameServerInfo;
import l2server.loginserver.GameServerThread;
import l2server.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import l2server.loginserver.network.loginserverpackets.AuthResponse;
import l2server.loginserver.network.loginserverpackets.LoginServerFail;
import l2server.util.network.BaseRecievePacket;

import java.util.Arrays;
import java.util.logging.Logger;

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
public class GameServerAuth extends BaseRecievePacket
{
	protected static Logger log = Logger.getLogger(GameServerAuth.class.getName());
	GameServerThread server;
	private byte[] hexId;
	private int desiredId;
	@SuppressWarnings("unused")
	private boolean hostReserved;
	private boolean acceptAlternativeId;
	private int maxPlayers;
	private int port;
	private String[] hosts;

	/**
	 * @param decrypt
	 */
	public GameServerAuth(byte[] decrypt, GameServerThread server)
	{
		super(decrypt);
		this.server = server;
		this.desiredId = readC();
		this.acceptAlternativeId = readC() != 0;
		this.hostReserved = readC() != 0;
		this.port = readH();
		this.maxPlayers = readD();
		int size = readD();
		this.hexId = readB(size);
		size = 2 * readD();
		this.hosts = new String[size];
		for (int i = 0; i < size; i++)
		{
			this.hosts[i] = readS();
		}

		if (Config.DEBUG)
		{
			Log.info("Auth request received");
		}

		if (handleRegProcess())
		{
			AuthResponse ar = new AuthResponse(server.getGameServerInfo().getId());
			server.sendPacket(ar);
			if (Config.DEBUG)
			{
				Log.info("Authed: id: " + server.getGameServerInfo().getId());
			}
			server.setLoginConnectionState(GameServerState.AUTHED);
		}
	}

	private boolean handleRegProcess()
	{
		GameServerTable gameServerTable = GameServerTable.getInstance();

		int id = this.desiredId;
		byte[] hexId = this.hexId;

		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null)
		{
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if (gsi.isAuthed())
					{
						this.server.forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
						return false;
					}
					else
					{
						this.server.attachGameServerInfo(gsi, this.port, this.hosts, this.maxPlayers);
					}
				}
			}
			else
			{
				// there is already a server registered with the desired id and different hex id
				// try to register this one with an alternative id
				if (Config.ACCEPT_NEW_GAMESERVER && this.acceptAlternativeId)
				{
					gsi = new GameServerInfo(id, hexId, this.server);
					if (gameServerTable.registerWithFirstAvaliableId(gsi))
					{
						this.server.attachGameServerInfo(gsi, this.port, this.hosts, this.maxPlayers);
						gameServerTable.registerServerOnDB(gsi);
					}
					else
					{
						this.server.forceClose(LoginServerFail.REASON_NO_FREE_ID);
						return false;
					}
				}
				else
				{
					// server id is already taken, and we cant get a new one for you
					this.server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
					return false;
				}
			}
		}
		else
		{
			// can we register on this id?
			if (Config.ACCEPT_NEW_GAMESERVER)
			{
				gsi = new GameServerInfo(id, hexId, this.server);
				if (gameServerTable.register(id, gsi))
				{
					this.server.attachGameServerInfo(gsi, this.port, this.hosts, this.maxPlayers);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
				{
					// some one took this ID meanwhile
					this.server.forceClose(LoginServerFail.REASON_ID_RESERVED);
					return false;
				}
			}
			else
			{
				this.server.forceClose(LoginServerFail.REASON_WRONG_HEXID);
				return false;
			}
		}

		return true;
	}
}
