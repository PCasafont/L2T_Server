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

import l2server.loginserver.GameServerTable;
import l2server.loginserver.GameServerTable.GameServerInfo;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.gameserverpackets.ServerStatus;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ServerList
 * Format: cc [cddcchhcdc]
 * <p>
 * c: server list size (number of servers)
 * c: ?
 * [ (repeat for each servers)
 * c: server id (ignored by client?)
 * d: server ip
 * d: server port
 * c: age limit (used by client?)
 * c: pvp or not (used by client?)
 * h: current number of players
 * h: max number of players
 * c: 0 if server is down
 * d: 2nd bit: clock
 * 3rd bit: wont dsiplay server name
 * 4th bit: test server (used by client?)
 * c: 0 if you dont want to display brackets in front of sever name
 * ]
 * <p>
 * Server will be considered as Good when the number of  online players
 * is less than half the maximum. as Normal between half and 4/5
 * and Full when there's more than 4/5 of the maximum number of players
 */
public final class ServerList extends L2LoginServerPacket
{
	private List<ServerData> servers;
	private int lastServer;
	private Map<Integer, Integer> charsOnServers;
	@SuppressWarnings("unused")
	private Map<Integer, long[]> charsToDelete;

	class ServerData
	{
		protected byte[] ip;
		protected int port;
		protected int ageLimit;
		protected boolean pvp;
		protected int currentPlayers;
		protected int maxPlayers;
		protected boolean brackets;
		protected boolean clock;
		protected int status;
		protected int serverId;
		protected int serverType;

		ServerData(L2LoginClient client, GameServerInfo gsi)
		{
			try
			{
				ip = InetAddress.getByName(gsi.getServerAddress(client.getConnection().getInetAddress())).getAddress();
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				ip = new byte[4];
				ip[0] = 127;
				ip[1] = 0;
				ip[2] = 0;
				ip[3] = 1;
			}

			port = gsi.getPort();
			pvp = gsi.isPvp();
			serverType = gsi.getServerType();
			currentPlayers = gsi.getCurrentPlayerCount();
			maxPlayers = gsi.getMaxPlayers();
			ageLimit = 0;
			brackets = gsi.isShowingBrackets();
			// If server GM-only - show status only to GMs
			status = gsi.getStatus() != ServerStatus.STATUS_GM_ONLY ? gsi.getStatus() :
					client.getAccessLevel() >= 10 ? gsi.getStatus() : ServerStatus.STATUS_DOWN;
			serverId = gsi.getId();
		}
	}

	public ServerList(L2LoginClient client)
	{
		servers = new ArrayList<>(GameServerTable.getInstance().getRegisteredGameServers().size());
		lastServer = client.getLastServer();
		for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			//if (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY
			//		|| client.getAccessLevel() > 0)
			servers.add(new ServerData(client, gsi));
		}

		charsOnServers = client.getCharsOnServ();
		charsToDelete = client.getCharsWaitingDelOnServ();
	}

	@Override
	public void write()
	{
		writeC(0x04);
		writeC(servers.size());
		writeC(lastServer);
		for (ServerData server : servers)
		{
			writeC(server.serverId); // server id

			writeC(server.ip[0] & 0xff);
			writeC(server.ip[1] & 0xff);
			writeC(server.ip[2] & 0xff);
			writeC(server.ip[3] & 0xff);

			writeD(server.port);
			writeC(server.ageLimit); // Age Limit 0, 15, 18
			writeC(server.pvp ? 0x01 : 0x00);
			writeH(1); //writeH(server.currentPlayers);
			if (server.port == 7778)
			{
				writeH(2); //writeH(server.maxPlayers);
			}
			else
			{
				writeH(20); //writeH(server.maxPlayers);
			}
			writeC(server.status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			if (server.port == 7778)
			{
				server.serverType = 0x200;
			}
			writeD(server.serverType); // 1: Normal, 2: Relax, 4: Public Test, 8: No Label, 16: Character Creation Restricted, 32: Event, 64: Free, 512: New, 1024: Classic
			writeC(server.brackets ? 0x01 : 0x00);
		}

		writeH(0x00); // unknown
		if (charsOnServers != null)
		{
			//writeC(charsOnServers.size());
			for (int servId : charsOnServers.keySet())
			{
				writeC(servId);
				writeC(charsOnServers.get(servId));
				/*if (charsToDelete == null || !charsToDelete.containsKey(servId))
                    writeC(0x00);
				else
				{
					writeC(charsToDelete.get(servId).length);
					for (long deleteTime : charsToDelete.get(servId))
					{
						writeD((int)((deleteTime - System.currentTimeMillis()) / 1000));
					}
				}*/
			}
		}
	}
}
