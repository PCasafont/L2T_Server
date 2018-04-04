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
import l2server.DatabasePool;
import l2server.ServerMode;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.L2LoginPacketHandler;
import l2server.network.Core;
import l2server.network.CoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

/**
 * @author KenM
 */
public class L2LoginServer {
	private static Logger log = LoggerFactory.getLogger(L2LoginServer.class.getName());


	public static final int PROTOCOL_REV = 0x0106;
	
	private static L2LoginServer instance;
	private GameServerListener gameServerListener;
	private Core<L2LoginClient> selectorThread;
	
	public static L2LoginServer getInstance() {
		return instance;
	}
	
	public L2LoginServer() {
		ServerMode.serverMode = ServerMode.MODE_LOGINSERVER;
		
		// Load Config
		Config.load();
		
		// Prepare Database
		DatabasePool.getInstance();
		
		try {
			LoginController.load();
		} catch (GeneralSecurityException e) {
			log.error("FATAL: Failed initializing LoginController. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		try {
			GameServerTable.load();
		} catch (GeneralSecurityException | SQLException e) {
			log.error("FATAL: Failed to load GameServerTable. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		loadBanFile();
		
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*")) {
			try {
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			} catch (UnknownHostException e) {
				log.warn("WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e.getMessage(), e);
			}
		}
		
		final CoreConfig sc = new CoreConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final L2LoginPacketHandler lph = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try {
			selectorThread = new Core<>(sc, sh, lph, sh, sh);
		} catch (IOException e) {
			log.error("FATAL: Failed to open Selector. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		try {
			gameServerListener = new GameServerListener();
			gameServerListener.start();
			log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		} catch (IOException e) {
			log.error("FATAL: Failed to start the Game Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		try {
			selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		} catch (IOException e) {
			log.error("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		selectorThread.start();
		
		log.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);
		
		instance = this;
	}
	
	public GameServerListener getGameServerListener() {
		return gameServerListener;
	}
	
	private void loadBanFile() {
		File bannedFile = new File("./banned_ip.cfg");
		if (bannedFile.exists() && bannedFile.isFile()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(bannedFile);
			} catch (FileNotFoundException e) {
				log.warn("Failed to load banned IPs file (" + bannedFile.getName() + ") for reading. Reason: " + e.getMessage(), e);
				return;
			}
			
			LineNumberReader reader = null;
			String line;
			String[] parts;
			try {
				reader = new LineNumberReader(new InputStreamReader(fis));
				
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					// check if this line isnt a comment line
					if (line.length() > 0 && line.charAt(0) != '#') {
						// split comments if any
						parts = line.split("#", 2);
						
						// discard comments in the line, if any
						line = parts[0];
						
						parts = line.split(" ");
						
						String address = parts[0];
						
						long duration = 0;
						
						if (parts.length > 1) {
							try {
								duration = Long.parseLong(parts[1]);
							} catch (NumberFormatException e) {
								log.warn("Skipped: Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " +
										reader.getLineNumber());
								continue;
							}
						}
						
						try {
							LoginController.getInstance().addBanForAddress(address, duration);
						} catch (UnknownHostException e) {
							log.warn(
									"Skipped: Invalid address (" + parts[0] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
						}
					}
				}
			} catch (IOException e) {
				log.warn("Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage(), e);
			} finally {
				try {
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			log.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		} else {
			log.warn("IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
	}
	
	public void shutdown(boolean restart) {
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
