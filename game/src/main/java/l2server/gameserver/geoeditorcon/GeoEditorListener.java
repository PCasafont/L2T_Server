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

package l2server.gameserver.geoeditorcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Dezmond
 */
public class GeoEditorListener extends Thread {
	private static Logger log = LoggerFactory.getLogger(GeoEditorListener.class.getName());


	private static GeoEditorListener instance;
	private static final int PORT = 9011;

	private ServerSocket serverSocket;
	private static GeoEditorThread geoEditor;

	public static GeoEditorListener getInstance() {
		synchronized (GeoEditorListener.class) {
			if (instance == null) {
				try {
					instance = new GeoEditorListener();
					instance.start();
					log.info("GeoEditorListener Initialized.");
				} catch (IOException e) {
					log.error("Error creating geoeditor listener! " + e.getMessage(), e);
					System.exit(1);
				}
			}
		}
		return instance;
	}

	private GeoEditorListener() throws IOException {
		serverSocket = new ServerSocket(PORT);
	}

	public GeoEditorThread getThread() {
		return geoEditor;
	}

	public String getStatus() {
		if (geoEditor != null && geoEditor.isWorking()) {
			return "Geoeditor connected.";
		}
		return "Geoeditor not connected.";
	}

	@Override
	public void run() {
		Socket connection = null;
		try {
			while (true) {
				connection = serverSocket.accept();
				if (geoEditor != null && geoEditor.isWorking()) {
					log.warn("Geoeditor already connected!");
					connection.close();
					continue;
				}
				log.info("Received geoeditor connection from: " + connection.getInetAddress().getHostAddress());
				geoEditor = new GeoEditorThread(connection);
				geoEditor.start();
			}
		} catch (Exception e) {
			log.warn("GeoEditorListener: " + e.getMessage(), e);
			try {
				connection.close();
			} catch (Exception ignored) {
			}
		} finally {
			try {
				serverSocket.close();
			} catch (IOException io) {
				log.info("", io);
			}
			log.warn("GeoEditorListener Closed!");
		}
	}
}
