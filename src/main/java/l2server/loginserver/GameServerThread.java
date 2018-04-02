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

package l2server.loginserver;

import l2server.Config;
import l2server.loginserver.GameServerTable.GameServerInfo;
import l2server.loginserver.network.L2JGameServerPacketHandler;
import l2server.loginserver.network.L2JGameServerPacketHandler.GameServerState;
import l2server.loginserver.network.loginserverpackets.InitLS;
import l2server.loginserver.network.loginserverpackets.KickPlayer;
import l2server.loginserver.network.loginserverpackets.LoginServerFail;
import l2server.loginserver.network.loginserverpackets.RequestCharacters;
import l2server.util.Rnd;
import l2server.util.Util;
import l2server.util.crypt.NewCrypt;
import l2server.util.network.BaseSendablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.Set;

/**
 * @author -Wooden-
 * @author KenM
 */

public class GameServerThread extends Thread {
	private static Logger log = LoggerFactory.getLogger(GameServerThread.class.getName());


	private final Socket connection;
	private InputStream in;
	private OutputStream out;
	private final RSAPublicKey publicKey;
	private final RSAPrivateKey privateKey;
	private NewCrypt blowfish;
	private GameServerState loginConnectionState = GameServerState.CONNECTED;
	
	private final String connectionIp;
	
	private GameServerInfo gsi;
	
	/**
	 * Authed Clients on a GameServer
	 */
	private final Set<String> accountsOnGameServer = new HashSet<>();
	
	private String connectionIPAddress;
	
	@Override
	public void run() {
		connectionIPAddress = connection.getInetAddress().getHostAddress();
		if (GameServerThread.isBannedGameserverIP(connectionIPAddress)) {
			log.info("GameServerRegistration: IP Address " + connectionIPAddress + " is on Banned IP list.");
			forceClose(LoginServerFail.REASON_IP_BANNED);
			// ensure no further processing for this connection
			return;
		}
		
		InitLS startPacket = new InitLS(publicKey.getModulus().toByteArray());
		try {
			sendPacket(startPacket);
			
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			for (; ; ) {
				lengthLo = in.read();
				lengthHi = in.read();
				length = lengthHi * 256 + lengthLo;
				
				if (lengthHi < 0 || connection.isClosed()) {
					log.trace("LoginServerThread: Login terminated the connection.");
					break;
				}
				
				byte[] data = new byte[length - 2];
				
				int receivedBytes = 0;
				int newBytes = 0;
				int left = length - 2;
				while (newBytes != -1 && receivedBytes < length - 2) {
					newBytes = in.read(data, receivedBytes, left);
					receivedBytes = receivedBytes + newBytes;
					left -= newBytes;
				}
				
				if (receivedBytes != length - 2) {
					log.warn("Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}
				
				// decrypt if we have a key
				data = blowfish.decrypt(data);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk) {
					log.warn("Incorrect packet checksum, closing connection (LS)");
					return;
				}
				
				if (Config.DEBUG) {
					log.warn("[C]\n" + Util.printData(data));
				}
				
				L2JGameServerPacketHandler.handlePacket(data, this);
			}
		} catch (IOException e) {
			String serverName = getServerId() != -1 ? "[" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) :
					"(" + connectionIPAddress + ")";
			String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			log.info(msg);
		} finally {
			if (isAuthed()) {
				gsi.setDown();
				log.info("Server [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) +
						" is now set as disconnected");
			}
			L2LoginServer.getInstance().getGameServerListener().removeGameServer(this);
			L2LoginServer.getInstance().getGameServerListener().removeFloodProtection(connectionIp);
		}
	}
	
	public boolean hasAccountOnGameServer(String account) {
		return accountsOnGameServer.contains(account);
	}
	
	public int getPlayerCount() {
		double multiplier = 2.0 - ((float) (System.currentTimeMillis() / 1000) - 1401565000) * 0.0000001;
		/*if (multiplier > 2.5f)
            multiplier = 2.5f - (multiplier - 2.5f);
		if (multiplier < 1)
			multiplier = 1;*/
		
		if (gsi.getId() == 28) {
			multiplier = 2;
		}
		
		return (int) Math.round(accountsOnGameServer.size() * multiplier + Rnd.get(1));
		//return accountsOnGameServer.size() * multiplier;
	}
	
	/**
	 * Attachs a GameServerInfo to this Thread
	 * <li>Updates the GameServerInfo values based on GameServerAuth packet</li>
	 * <li><b>Sets the GameServerInfo as Authed</b></li>
	 *
	 * @param gsi The GameServerInfo to be attached.
	 */
	public void attachGameServerInfo(GameServerInfo gsi, int port, String[] hosts, int maxPlayers) {
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(port);
		setGameHosts(hosts);
		gsi.setMaxPlayers(maxPlayers);
		gsi.setAuthed(true);
	}
	
	public void forceClose(int reason) {
		sendPacket(new LoginServerFail(reason));
		
		try {
			connection.close();
		} catch (IOException e) {
			log.trace("GameServerThread: Failed disconnecting banned server, server already disconnected.");
		}
	}
	
	public static boolean isBannedGameserverIP(String ipAddress) {
		return false;
	}
	
	public GameServerThread(Socket con) {
		connection = con;
		connectionIp = con.getInetAddress().getHostAddress();
		try {
			in = connection.getInputStream();
			out = new BufferedOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		KeyPair pair = GameServerTable.getInstance().getKeyPair();
		privateKey = (RSAPrivateKey) pair.getPrivate();
		publicKey = (RSAPublicKey) pair.getPublic();
		blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		setName(getClass().getSimpleName() + "-" + getId() + "@" + connectionIp);
		start();
	}
	
	/**
	 * @throws IOException
	 */
	public void sendPacket(BaseSendablePacket sl) {
		try {
			byte[] data = sl.getContent();
			NewCrypt.appendChecksum(data);
			if (Config.DEBUG) {
				log.trace("[S] " + sl.getClass().getSimpleName() + ":\n" + Util.printData(data));
			}
			data = blowfish.crypt(data);
			
			int len = data.length + 2;
			synchronized (out) {
				out.write(len & 0xff);
				out.write(len >> 8 & 0xff);
				out.write(data);
				out.flush();
			}
		} catch (IOException e) {
			log.error("IOException while sending packet " + sl.getClass().getSimpleName());
		}
	}
	
	public void kickPlayer(String account) {
		sendPacket(new KickPlayer(account));
		// Tenkai temp fix
		accountsOnGameServer.remove(account);
	}
	
	public void requestCharacters(String account) {
		sendPacket(new RequestCharacters(account));
	}
	
	public void setGameHosts(String[] hosts) {
		log.info("Updated Gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " IP's:");
		
		gsi.clearServerAddresses();
		for (int i = 0; i < hosts.length; i += 2) {
			try {
				gsi.addServerAddress(hosts[i], hosts[i + 1]);
			} catch (Exception e) {
				log.warn("Couldn't resolve hostname \"" + e + "\"");
			}
		}
		
		for (String s : gsi.getServerAddresses()) {
			log.info(s);
		}
	}
	
	/**
	 * @return Returns the isAuthed.
	 */
	public boolean isAuthed() {
		if (getGameServerInfo() == null) {
			return false;
		}
		return getGameServerInfo().isAuthed();
	}
	
	public void setGameServerInfo(GameServerInfo gsi) {
		this.gsi = gsi;
	}
	
	public GameServerInfo getGameServerInfo() {
		return gsi;
	}
	
	/**
	 * @return Returns the connectionIpAddress.
	 */
	public String getConnectionIpAddress() {
		return connectionIPAddress;
	}
	
	public int getServerId() {
		if (getGameServerInfo() != null) {
			return getGameServerInfo().getId();
		}
		return -1;
	}
	
	public RSAPrivateKey getPrivateKey() {
		return privateKey;
	}
	
	public void SetBlowFish(NewCrypt blowfish) {
		this.blowfish = blowfish;
	}
	
	public void addAccountOnGameServer(String account) {
		accountsOnGameServer.add(account);
	}
	
	public void removeAccountOnGameServer(String account) {
		accountsOnGameServer.remove(account);
	}
	
	public GameServerState getLoginConnectionState() {
		return loginConnectionState;
	}
	
	public void setLoginConnectionState(GameServerState state) {
		loginConnectionState = state;
	}
}
