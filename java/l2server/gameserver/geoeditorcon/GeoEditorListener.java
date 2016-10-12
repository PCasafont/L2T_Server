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

import l2server.log.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

/**
 * @author Dezmond
 */
public class GeoEditorListener extends Thread
{
	private static GeoEditorListener _instance;
	private static final int PORT = 9011;

	private ServerSocket _serverSocket;
	private static GeoEditorThread _geoEditor;

	public static GeoEditorListener getInstance()
	{
		synchronized (GeoEditorListener.class)
		{
			if (_instance == null)
			{
				try
				{
					_instance = new GeoEditorListener();
					_instance.start();
					Log.info("GeoEditorListener Initialized.");
				}
				catch (IOException e)
				{
					Log.log(Level.SEVERE, "Error creating geoeditor listener! " + e.getMessage(), e);
					System.exit(1);
				}
			}
		}
		return _instance;
	}

	private GeoEditorListener() throws IOException
	{
		_serverSocket = new ServerSocket(PORT);
	}

	public GeoEditorThread getThread()
	{
		return _geoEditor;
	}

	public String getStatus()
	{
		if (_geoEditor != null && _geoEditor.isWorking())
		{
			return "Geoeditor connected.";
		}
		return "Geoeditor not connected.";
	}

	@Override
	public void run()
	{
		Socket connection = null;
		try
		{
			while (true)
			{
				connection = _serverSocket.accept();
				if (_geoEditor != null && _geoEditor.isWorking())
				{
					Log.warning("Geoeditor already connected!");
					connection.close();
					continue;
				}
				Log.info("Received geoeditor connection from: " + connection.getInetAddress().getHostAddress());
				_geoEditor = new GeoEditorThread(connection);
				_geoEditor.start();
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "GeoEditorListener: " + e.getMessage(), e);
			try
			{
				connection.close();
			}
			catch (Exception ignored)
			{
			}
		}
		finally
		{
			try
			{
				_serverSocket.close();
			}
			catch (IOException io)
			{
				Log.log(Level.INFO, "", io);
			}
			Log.warning("GeoEditorListener Closed!");
		}
	}
}
