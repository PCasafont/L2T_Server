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
import l2server.gameserver.geoeditorcon.GeoEditorListener;
import l2server.gameserver.gui.ServerGui;
import l2server.gameserver.idfactory.IdFactory;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.L2GamePacketHandler;
import l2server.gameserver.pathfinding.PathFinding;
import l2server.gameserver.util.DeadLockDetector;
import l2server.network.Core;
import l2server.network.CoreConfig;
import l2server.util.IPv4Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

public class Server {
	private static Logger log = LoggerFactory.getLogger(Server.class.getName());

	private final Core<L2GameClient> selectorThread;
	private final L2GamePacketHandler gamePacketHandler;
	private final DeadLockDetector deadDetectThread;
	public static Server gameServer;
	public static ServerGui gui;
	private LoginServerThread loginThread;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB() {
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public Core<L2GameClient> getSelectorThread() {
		return selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler() {
		return gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread() {
		return deadDetectThread;
	}
	
	public Server() throws Exception {
		
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		gui = new ServerGui();
		gui.init();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2) {
			PathFinding.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN) {
			GeoEditorListener.getInstance();
		}
		
		//Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		if (Config.DEADLOCK_DETECTOR) {
			deadDetectThread = new DeadLockDetector();
			deadDetectThread.setDaemon(true);
			deadDetectThread.start();
		} else {
			deadDetectThread = null;
		}
		
		//LameGuard.getInstance();
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the
		// allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		final CoreConfig sc = new CoreConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		gamePacketHandler = new L2GamePacketHandler();
		selectorThread = new Core<>(sc, gamePacketHandler, gamePacketHandler, gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*")) {
			try {
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			} catch (UnknownHostException e1) {
				log.error("WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try {
			selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		} catch (IOException e) {
			log.error("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		selectorThread.start();
		log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		log.info("Server Loaded in " + (serverLoadEnd - serverLoadStart) / 1000 + " seconds");
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		gameServer = this;
	}
	
	static long t = 0;
	
	public static void printSection(String s) {
		//if (t > 0)
		//	log.info("Time spent in last section: " + (t - t) / 1000 + "s");
		t = System.currentTimeMillis();
		
		s = "=[ " + s + " ]";
		while (s.length() < 78) {
			s = "-" + s;
		}
		log.info(s);
	}
}
