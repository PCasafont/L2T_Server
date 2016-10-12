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
	private List<ServerData> _servers;
	private int _lastServer;
	private Map<Integer, Integer> _charsOnServers;
	@SuppressWarnings("unused")
	private Map<Integer, long[]> _charsToDelete;

	class ServerData
	{
		protected byte[] _ip;
		protected int _port;
		protected int _ageLimit;
		protected boolean _pvp;
		protected int _currentPlayers;
		protected int _maxPlayers;
		protected boolean _brackets;
		protected boolean _clock;
		protected int _status;
		protected int _serverId;
		protected int _serverType;

		ServerData(L2LoginClient client, GameServerInfo gsi)
		{
			try
			{
				_ip = InetAddress.getByName(gsi.getServerAddress(client.getConnection().getInetAddress())).getAddress();
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				_ip = new byte[4];
				_ip[0] = 127;
				_ip[1] = 0;
				_ip[2] = 0;
				_ip[3] = 1;
			}

			_port = gsi.getPort();
			_pvp = gsi.isPvp();
			_serverType = gsi.getServerType();
			_currentPlayers = gsi.getCurrentPlayerCount();
			_maxPlayers = gsi.getMaxPlayers();
			_ageLimit = 0;
			_brackets = gsi.isShowingBrackets();
			// If server GM-only - show status only to GMs
			_status = gsi.getStatus() != ServerStatus.STATUS_GM_ONLY ? gsi.getStatus() :
					client.getAccessLevel() >= 10 ? gsi.getStatus() : ServerStatus.STATUS_DOWN;
			_serverId = gsi.getId();
		}
	}

	public ServerList(L2LoginClient client)
	{
		_servers = new ArrayList<>(GameServerTable.getInstance().getRegisteredGameServers().size());
		_lastServer = client.getLastServer();
		for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			//if (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY
			//		|| client.getAccessLevel() > 0)
			_servers.add(new ServerData(client, gsi));
		}

		_charsOnServers = client.getCharsOnServ();
		_charsToDelete = client.getCharsWaitingDelOnServ();
	}

	@Override
	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_lastServer);
		for (ServerData server : _servers)
		{
			writeC(server._serverId); // server id

			writeC(server._ip[0] & 0xff);
			writeC(server._ip[1] & 0xff);
			writeC(server._ip[2] & 0xff);
			writeC(server._ip[3] & 0xff);

			writeD(server._port);
			writeC(server._ageLimit); // Age Limit 0, 15, 18
			writeC(server._pvp ? 0x01 : 0x00);
			writeH(1); //writeH(server._currentPlayers);
			if (server._port == 7778)
			{
				writeH(2); //writeH(server._maxPlayers);
			}
			else
			{
				writeH(20); //writeH(server._maxPlayers);
			}
			writeC(server._status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
			if (server._port == 7778)
			{
				server._serverType = 0x200;
			}
			writeD(server._serverType); // 1: Normal, 2: Relax, 4: Public Test, 8: No Label, 16: Character Creation Restricted, 32: Event, 64: Free, 512: New, 1024: Classic
			writeC(server._brackets ? 0x01 : 0x00);
		}

		writeH(0x00); // unknown
		if (_charsOnServers != null)
		{
			//writeC(_charsOnServers.size());
			for (int servId : _charsOnServers.keySet())
			{
				writeC(servId);
				writeC(_charsOnServers.get(servId));
				/*if (_charsToDelete == null || !_charsToDelete.containsKey(servId))
                    writeC(0x00);
				else
				{
					writeC(_charsToDelete.get(servId).length);
					for (long deleteTime : _charsToDelete.get(servId))
					{
						writeD((int)((deleteTime - System.currentTimeMillis()) / 1000));
					}
				}*/
			}
		}
	}
}
