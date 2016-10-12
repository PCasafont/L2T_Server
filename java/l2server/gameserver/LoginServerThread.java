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

package l2server.gameserver;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.model.L2World;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.gameserverpackets.*;
import l2server.gameserver.network.loginserverpackets.*;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;
import l2server.gameserver.network.serverpackets.LoginFail;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.log.Log;
import l2server.util.Util;
import l2server.util.crypt.NewCrypt;
import l2server.util.network.BaseSendablePacket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoginServerThread extends Thread
{
	protected static final Logger _logAccounting = Logger.getLogger("accounting");

	/**
	 * {@see l2server.loginserver.LoginServer#PROTOCOL_REV }
	 */
	private static final int REVISION = 0x0106;
	private RSAPublicKey _publicKey;
	private String _hostname;
	private int _port;
	private int _gamePort;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;

	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #_blowfishKey}
	 */
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private boolean _acceptAlternate;
	private int _requestID;
	private int _serverID;
	private boolean _reserveHost;
	private int _maxPlayer;
	private final List<WaitingClient> _waitingClients;
	private Map<String, L2GameClient> _accountsInGameServer;
	private int _status;
	private String _serverName;
	private String[] _subnets;
	private String[] _hosts;

	private LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = Util.generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_subnets = Config.GAME_SERVER_SUBNETS;
		_hosts = Config.GAME_SERVER_HOSTS;
		_waitingClients = new ArrayList<>();
		_accountsInGameServer = new ConcurrentHashMap<>();
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}

	public static LoginServerThread getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				Log.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());

				//init Blowfish
				_blowfishKey = Util.generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted())
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = lengthHi * 256 + lengthLo;

					if (lengthHi < 0)
					{
						Log.finer("LoginServerThread: Login terminated the connection.");
						break;
					}

					byte[] incoming = new byte[length - 2];

					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}

					if (receivedBytes != length - 2)
					{
						Log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}

					// decrypt if we have a key
					byte[] decrypt = _blowfish.decrypt(incoming);
					checksumOk = NewCrypt.verifyChecksum(decrypt);

					if (!checksumOk)
					{
						Log.warning("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}

					if (Config.DEBUG)
					{
						Log.warning("[C]\n" + Util.printData(decrypt));
					}

					int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 0x00:
							InitLS init = new InitLS(decrypt);
							if (Config.DEBUG)
							{
								Log.info("Init received");
							}
							if (init.getRevision() != REVISION)
							{
								//TODO: revision mismatch
								Log.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try
							{
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
								if (Config.DEBUG)
								{
									Log.info("RSA key set up");
								}
							}

							catch (GeneralSecurityException e)
							{
								Log.warning("Troubles while init the public key send by login");
								break;
							}
							//send the blowfish key through the rsa encryption
							BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
							sendPacket(bfk);
							if (Config.DEBUG)
							{
								Log.info("Sent new blowfish key");
							}
							//now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							if (Config.DEBUG)
							{
								Log.info("Changed blowfish key");
							}
							AuthRequest ar =
									new AuthRequest(_requestID, _acceptAlternate, _hexID, _gamePort, _reserveHost,
											_maxPlayer, _subnets, _hosts);
							sendPacket(ar);
							if (Config.DEBUG)
							{
								Log.info("Sent AuthRequest to login");
							}
							break;
						case 0x01:
							LoginServerFail lsf = new LoginServerFail(decrypt);
							Log.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						case 0x02:
							AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							Log.info("Registered on login as Server " + _serverID + " : " + _serverName);
							ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							st.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
							if (Config.SERVER_GMONLY)
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							if (Config.SERVER_LIST_AGE == 15)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_15);
							}
							else if (Config.SERVER_LIST_AGE == 18)
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_18);
							}
							else
							{
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_ALL);
							}
							sendPacket(st);
							if (L2World.getInstance().getAllPlayersCount() > 0)
							{
								ArrayList<String> playerList = new ArrayList<>();
								Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers().values();
								//synchronized (L2World.getInstance().getAllPlayers())
								{
									for (L2PcInstance player : pls)
									{
										playerList.add(player.getAccountName());
									}
								}
								PlayerInGame pig = new PlayerInGame(playerList);
								sendPacket(pig);
							}
							break;
						case 0x03:
							PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							String account = par.getAccount();
							String playKey1 = null;
							if (account.contains(";"))
							{
								account = par.getAccount().split(";")[0];
								playKey1 = par.getAccount().split(";")[1];
							}
							WaitingClient wcToRemove = null;
							synchronized (_waitingClients)
							{
								for (WaitingClient wc : _waitingClients)
								{
									if (playKey1 == null && wc.account.equals(account) ||
											wc.session.playOkID1 == Integer.parseInt(playKey1))
									{
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									if (Config.DEBUG)
									{
										Log.info("Login accepted player " + wcToRemove.account + " waited(" +
												(TimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
									}
									PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setAccountName(account);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									wcToRemove.gameClient.sendPacket(new LoginFail(LoginFail.SUCCESS));
									CharSelectionInfo cl = new CharSelectionInfo(account,
											wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
									_accountsInGameServer.put(account, wcToRemove.gameClient);
								}
								else
								{
									Log.warning(
											"Session key is not correct. Closing connection for account " + account +
													".");
									//wcToRemove.gameClient.getConnection().sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									wcToRemove.gameClient.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									_accountsInGameServer.remove(account);
								}
								_waitingClients.remove(wcToRemove);
							}
							break;
						case 0x04:
							KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							break;
						case 0x05:
							RequestCharacters rc = new RequestCharacters(decrypt);
							getCharsOnServer(rc.getAccount());
							break;
					}
				}
			}
			catch (UnknownHostException e)
			{
				if (Config.DEBUG)
				{
					Log.log(Level.WARNING, "", e);
				}
			}
			catch (SocketException e)
			{
				Log.warning("LoginServer not avaible, trying to reconnect...");
			}
			catch (IOException e)
			{
				Log.log(Level.WARNING, "Disconnected from Login, Trying to reconnect: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					_loginSocket.close();
					if (isInterrupted())
					{
						return;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			try
			{
				Thread.sleep(5000); // 5 seconds tempo.
			}
			catch (InterruptedException e)
			{
				return; // never swallow an interrupt!
			}
		}
	}

	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key)
	{
		if (Config.DEBUG)
		{
			Log.info(String.valueOf(key));
		}
		WaitingClient wc = new WaitingClient(acc, client, key);
		synchronized (_waitingClients)
		{
			_waitingClients.add(wc);
		}
		PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		try
		{
			sendPacket(par);
		}
		catch (IOException e)
		{
			Log.warning("Error while sending player auth request");
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	public void removeWaitingClient(L2GameClient client)
	{
		WaitingClient toRemove = null;
		synchronized (_waitingClients)
		{
			for (WaitingClient c : _waitingClients)
			{
				if (c.gameClient == client)
				{
					toRemove = c;
				}
			}
			if (toRemove != null)
			{
				_waitingClients.remove(toRemove);
			}
		}
	}

	public void sendLogout(String account)
	{
		if (account == null)
		{
			return;
		}

		PlayerLogout pl = new PlayerLogout(account);
		try
		{
			sendPacket(pl);
		}
		catch (IOException e)
		{
			Log.warning("Error while sending logout packet to login");
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
		finally
		{
			_accountsInGameServer.remove(account);
		}
	}

	public void addGameServerLogin(String account, L2GameClient client)
	{
		_accountsInGameServer.put(account, client);
	}

	public void sendAccessLevel(String account, int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	public void sendClientTracert(String account, String[] adress)
	{
		PlayerTracert ptc = new PlayerTracert(account, adress[0], adress[1], adress[2], adress[3], adress[4]);
		try
		{
			sendPacket(ptc);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	public void sendTempBan(String account, String ip, long time)
	{
		TempBan tbn = new TempBan(account, ip, time);
		try
		{
			sendPacket(tbn);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	private String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}

	public void doKickPlayer(String account)
	{
		L2GameClient client = _accountsInGameServer.get(account);
		if (client != null)
		{
			LogRecord record = new LogRecord(Level.WARNING, "Kicked by login");
			record.setParameters(new Object[]{client});
			_logAccounting.log(record);
			client.setAditionalClosePacket(SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT));
			client.closeNow();
		}
	}

	private void getCharsOnServer(String account)
	{
		Connection con = null;
		int chars = 0;
		List<Long> charToDel = new ArrayList<>();
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				chars++;
				long delTime = rset.getLong("deletetime");
				if (delTime != 0)
				{
					charToDel.add(delTime);
				}
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			Log.log(Level.WARNING, "Exception: getCharsOnServer: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		ReplyCharacters rec = new ReplyCharacters(account, chars, charToDel);
		try
		{
			sendPacket(rec);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(BaseSendablePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (Config.DEBUG)
		{
			Log.finest("[S]\n" + Util.printData(data));
		}
		data = _blowfish.crypt(data);

		int len = data.length + 2;
		synchronized (_out) //avoids tow threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}

	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}

	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}

	/**
	 */
	public void sendServerStatus(int id, int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	/**
	 * Send Server Type Config to LS
	 */
	public void sendServerType()
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				Log.log(Level.WARNING, "", e);
			}
		}
	}

	/**
	 * @return
	 */
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}

	/**
	 * @return
	 */
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}

	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return _serverName;
	}

	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}

	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;

		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}

		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}

	private static class WaitingClient
	{
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;

		public WaitingClient(String acc, L2GameClient client, SessionKey key)
		{
			account = acc;
			timestamp = TimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final LoginServerThread _instance = new LoginServerThread();
	}
}
