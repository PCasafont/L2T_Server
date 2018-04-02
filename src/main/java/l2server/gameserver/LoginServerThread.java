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
import l2server.gameserver.model.World;
import l2server.gameserver.model.actor.instance.Player;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.SystemMessageId;
import l2server.gameserver.network.gameserverpackets.*;
import l2server.gameserver.network.loginserverpackets.*;
import l2server.gameserver.network.serverpackets.CharSelectionInfo;
import l2server.gameserver.network.serverpackets.LoginFail;
import l2server.gameserver.network.serverpackets.SystemMessage;
import l2server.util.Util;
import l2server.util.crypt.NewCrypt;
import l2server.util.loader.annotations.Load;
import l2server.util.network.BaseSendablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class LoginServerThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(LoginServerThread.class.getName());
	
	/**
	 * {@see l2server.loginserver.LoginServer#PROTOCOL_REV }
	 */
	private static final int REVISION = 0x0106;
	private RSAPublicKey publicKey;
	private String hostname;
	private int port;
	private int gamePort;
	private Socket loginSocket;
	private InputStream in;
	private OutputStream out;
	
	/**
	 * The BlowFish engine used to encrypt packets<br>
	 * It is first initialized with a unified key:<br>
	 * "_;v.]05-31!|+-%xT!^[$\00"<br>
	 * <br>
	 * and then after handshake, with a new key sent by<br>
	 * loginserver during the handshake. This new key is stored<br>
	 * in {@link #blowfishKey}
	 */
	private NewCrypt blowfish;
	private byte[] blowfishKey;
	private byte[] hexID;
	private boolean acceptAlternate;
	private int requestID;
	private int serverID;
	private boolean reserveHost;
	private int maxPlayer;
	private final List<WaitingClient> waitingClients;
	private Map<String, L2GameClient> accountsInGameServer;
	private int status;
	private String serverName;
	private String[] subnets;
	private String[] hosts;
	
	private LoginServerThread() {
		super("LoginServerThread");
		port = Config.GAME_SERVER_LOGIN_PORT;
		gamePort = Config.PORT_GAME;
		hostname = Config.GAME_SERVER_LOGIN_HOST;
		hexID = Config.HEX_ID;
		if (hexID == null) {
			requestID = Config.REQUEST_ID;
			hexID = Util.generateHex(16);
		} else {
			requestID = Config.SERVER_ID;
		}
		acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		subnets = Config.GAME_SERVER_SUBNETS;
		hosts = Config.GAME_SERVER_HOSTS;
		waitingClients = new ArrayList<>();
		accountsInGameServer = new ConcurrentHashMap<>();
		maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	public static LoginServerThread getInstance() {
		return SingletonHolder.instance;
	}
	
	@Load
	public synchronized void initialize() {
		start();
	}
	
	@Override
	public void run() {
		while (!isInterrupted()) {
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try {
				// Connection
				log.info("Connecting to login on " + hostname + ":" + port);
				loginSocket = new Socket(hostname, port);
				in = loginSocket.getInputStream();
				out = new BufferedOutputStream(loginSocket.getOutputStream());
				
				//init Blowfish
				blowfishKey = Util.generateHex(40);
				blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted()) {
					lengthLo = in.read();
					lengthHi = in.read();
					length = lengthHi * 256 + lengthLo;
					
					if (lengthHi < 0) {
						log.trace("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					byte[] incoming = new byte[length - 2];
					
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					while (newBytes != -1 && receivedBytes < length - 2) {
						newBytes = in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != length - 2) {
						log.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					// decrypt if we have a key
					byte[] decrypt = blowfish.decrypt(incoming);
					checksumOk = NewCrypt.verifyChecksum(decrypt);
					
					if (!checksumOk) {
						log.warn("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					if (Config.DEBUG) {
						log.warn("[C]\n" + Util.printData(decrypt));
					}
					
					int packetType = decrypt[0] & 0xff;
					switch (packetType) {
						case 0x00:
							InitLS init = new InitLS(decrypt);
							if (Config.DEBUG) {
								log.info("Init received");
							}
							if (init.getRevision() != REVISION) {
								//TODO: revision mismatch
								log.warn("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							try {
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
								if (Config.DEBUG) {
									log.info("RSA key set up");
								}
							} catch (GeneralSecurityException e) {
								log.warn("Troubles while init the public key send by login");
								break;
							}
							//send the blowfish key through the rsa encryption
							BlowFishKey bfk = new BlowFishKey(blowfishKey, publicKey);
							sendPacket(bfk);
							if (Config.DEBUG) {
								log.info("Sent new blowfish key");
							}
							//now, only accept paket with the new encryption
							blowfish = new NewCrypt(blowfishKey);
							if (Config.DEBUG) {
								log.info("Changed blowfish key");
							}
							AuthRequest ar = new AuthRequest(requestID, acceptAlternate, hexID, gamePort, reserveHost, maxPlayer, subnets, hosts);
							sendPacket(ar);
							if (Config.DEBUG) {
								log.info("Sent AuthRequest to login");
							}
							break;
						case 0x01:
							LoginServerFail lsf = new LoginServerFail(decrypt);
							log.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						case 0x02:
							AuthResponse aresp = new AuthResponse(decrypt);
							serverID = aresp.getServerId();
							serverName = aresp.getServerName();
							Config.saveHexid(serverID, hexToString(hexID));
							log.info("Registered on login as Server " + serverID + " : " + serverName);
							ServerStatus st = new ServerStatus();
							if (Config.SERVER_LIST_BRACKET) {
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							} else {
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							}
							st.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
							if (Config.SERVER_GMONLY) {
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							} else {
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							}
							if (Config.SERVER_LIST_AGE == 15) {
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_15);
							} else if (Config.SERVER_LIST_AGE == 18) {
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_18);
							} else {
								st.addAttribute(ServerStatus.SERVER_AGE, ServerStatus.SERVER_AGE_ALL);
							}
							sendPacket(st);
							if (World.getInstance().getAllPlayersCount() > 0) {
								ArrayList<String> playerList = new ArrayList<>();
								Collection<Player> pls = World.getInstance().getAllPlayers().values();
								//synchronized (World.getInstance().getAllPlayers())
								{
									for (Player player : pls) {
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
							if (account.contains(";")) {
								account = par.getAccount().split(";")[0];
								playKey1 = par.getAccount().split(";")[1];
							}
							WaitingClient wcToRemove = null;
							synchronized (waitingClients) {
								for (WaitingClient wc : waitingClients) {
									if (playKey1 == null && wc.account.equals(account) || wc.session.playOkID1 == Integer.parseInt(playKey1)) {
										wcToRemove = wc;
									}
								}
							}
							if (wcToRemove != null) {
								if (par.isAuthed()) {
									if (Config.DEBUG) {
										log.info("Login accepted player " + wcToRemove.account + " waited(" +
												(TimeController.getGameTicks() - wcToRemove.timestamp) + "ms)");
									}
									PlayerInGame pig = new PlayerInGame(par.getAccount());
									sendPacket(pig);
									wcToRemove.gameClient.setAccountName(account);
									wcToRemove.gameClient.setState(GameClientState.AUTHED);
									wcToRemove.gameClient.setSessionId(wcToRemove.session);
									wcToRemove.gameClient.sendPacket(new LoginFail(LoginFail.SUCCESS));
									CharSelectionInfo cl = new CharSelectionInfo(account, wcToRemove.gameClient.getSessionId().playOkID1);
									wcToRemove.gameClient.getConnection().sendPacket(cl);
									wcToRemove.gameClient.setCharSelection(cl.getCharInfo());
									accountsInGameServer.put(account, wcToRemove.gameClient);
								} else {
									log.warn("Session key is not correct. Closing connection for account " + account + ".");
									//wcToRemove.gameClient.getConnection().sendPacket(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									wcToRemove.gameClient.close(new LoginFail(LoginFail.SYSTEM_ERROR_LOGIN_LATER));
									accountsInGameServer.remove(account);
								}
								waitingClients.remove(wcToRemove);
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
			} catch (UnknownHostException e) {
				if (Config.DEBUG) {
					log.warn("", e);
				}
			} catch (SocketException e) {
				log.warn("LoginServer not avaible, trying to reconnect...");
			} catch (IOException e) {
				log.warn("Disconnected from Login, Trying to reconnect: " + e.getMessage(), e);
			} finally {
				try {
					if (loginSocket != null) {
						loginSocket.close();
					}
					if (isInterrupted()) {
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(5000); // 5 seconds tempo.
			} catch (InterruptedException e) {
				return; // never swallow an interrupt!
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(String acc, L2GameClient client, SessionKey key) {
		if (Config.DEBUG) {
			log.info(String.valueOf(key));
		}
		WaitingClient wc = new WaitingClient(acc, client, key);
		synchronized (waitingClients) {
			waitingClients.add(wc);
		}
		PlayerAuthRequest par = new PlayerAuthRequest(acc, key);
		try {
			sendPacket(par);
		} catch (IOException e) {
			log.warn("Error while sending player auth request");
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	public void removeWaitingClient(L2GameClient client) {
		WaitingClient toRemove = null;
		synchronized (waitingClients) {
			for (WaitingClient c : waitingClients) {
				if (c.gameClient == client) {
					toRemove = c;
				}
			}
			if (toRemove != null) {
				waitingClients.remove(toRemove);
			}
		}
	}
	
	public void sendLogout(String account) {
		if (account == null) {
			return;
		}
		
		PlayerLogout pl = new PlayerLogout(account);
		try {
			sendPacket(pl);
		} catch (IOException e) {
			log.warn("Error while sending logout packet to login");
			if (Config.DEBUG) {
				log.warn("", e);
			}
		} finally {
			accountsInGameServer.remove(account);
		}
	}
	
	public void addGameServerLogin(String account, L2GameClient client) {
		accountsInGameServer.put(account, client);
	}
	
	public void sendAccessLevel(String account, int level) {
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try {
			sendPacket(cal);
		} catch (IOException e) {
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	public void sendClientTracert(String account, String[] adress) {
		PlayerTracert ptc = new PlayerTracert(account, adress[0], adress[1], adress[2], adress[3], adress[4]);
		try {
			sendPacket(ptc);
		} catch (IOException e) {
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	public void sendTempBan(String account, String ip, long time) {
		TempBan tbn = new TempBan(account, ip, time);
		try {
			sendPacket(tbn);
		} catch (IOException e) {
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	private String hexToString(byte[] hex) {
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(String account) {
		L2GameClient client = accountsInGameServer.get(account);
		if (client != null) {
			log.warn("Kicked by login: {}", client);
			client.setAditionalClosePacket(SystemMessage.getSystemMessage(SystemMessageId.ANOTHER_LOGIN_WITH_ACCOUNT));
			client.closeNow();
		}
	}
	
	private void getCharsOnServer(String account) {
		Connection con = null;
		int chars = 0;
		List<Long> charToDel = new ArrayList<>();
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			while (rset.next()) {
				chars++;
				long delTime = rset.getLong("deletetime");
				if (delTime != 0) {
					charToDel.add(delTime);
				}
			}
			rset.close();
			statement.close();
		} catch (SQLException e) {
			log.warn("Exception: getCharsOnServer: " + e.getMessage(), e);
		} finally {
			L2DatabaseFactory.close(con);
		}
		
		ReplyCharacters rec = new ReplyCharacters(account, chars, charToDel);
		try {
			sendPacket(rec);
		} catch (IOException e) {
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	/**
	 * @throws IOException
	 */
	private void sendPacket(BaseSendablePacket sl) throws IOException {
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (Config.DEBUG) {
			log.trace("[S]\n" + Util.printData(data));
		}
		data = blowfish.crypt(data);
		
		int len = data.length + 2;
		synchronized (out) //avoids tow threads writing in the mean time
		{
			out.write(len & 0xff);
			out.write(len >> 8 & 0xff);
			out.write(data);
			out.flush();
		}
	}
	
	/**
	 * @param maxPlayer The maxPlayer to set.
	 */
	public void setMaxPlayer(int maxPlayer) {
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		this.maxPlayer = maxPlayer;
	}
	
	/**
	 * @return Returns the maxPlayer.
	 */
	public int getMaxPlayer() {
		return maxPlayer;
	}
	
	public void sendServerStatus(int id, int value) {
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try {
			sendPacket(ss);
		} catch (IOException e) {
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	/**
	 * Send Server Type Config to LS
	 */
	public void sendServerType() {
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(ServerStatus.SERVER_TYPE, Config.SERVER_LIST_TYPE);
		try {
			sendPacket(ss);
		} catch (IOException e) {
			if (Config.DEBUG) {
				log.warn("", e);
			}
		}
	}
	
	public String getStatusString() {
		return ServerStatus.STATUS_STRING[status];
	}
	
	public boolean isBracketShown() {
		return Config.SERVER_LIST_BRACKET;
	}
	
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName() {
		return serverName;
	}
	
	public void setServerStatus(int status) {
		switch (status) {
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				this.status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				this.status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				this.status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				this.status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				this.status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				this.status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}
	
	public static class SessionKey {
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2) {
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString() {
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	private static class WaitingClient {
		public int timestamp;
		public String account;
		public L2GameClient gameClient;
		public SessionKey session;
		
		public WaitingClient(String acc, L2GameClient client, SessionKey key) {
			account = acc;
			timestamp = TimeController.getGameTicks();
			gameClient = client;
			session = key;
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {
		protected static final LoginServerThread instance = new LoginServerThread();
	}
}
