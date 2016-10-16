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
import l2server.log.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author -Wooden-
 */
public abstract class FloodProtectedListener extends Thread
{
	private Map<String, ForeignConnection> floodProtection = new HashMap<>();
	private String listenIp;
	private int port;
	private ServerSocket serverSocket;

	public FloodProtectedListener(String listenIp, int port) throws IOException
	{
		this.port = port;
		this.listenIp = listenIp;
		if (this.listenIp.equals("*"))
		{
			serverSocket = new ServerSocket(this.port);
		}
		else
		{
			serverSocket = new ServerSocket(this.port, 50, InetAddress.getByName(this.listenIp));
		}
	}

	@Override
	public void run()
	{
		Socket connection = null;

		while (true)
		{
			try
			{
				connection = serverSocket.accept();
				if (Config.FLOOD_PROTECTION)
				{
					ForeignConnection fConnection = floodProtection.get(connection.getInetAddress().getHostAddress());
					if (fConnection != null)
					{
						fConnection.connectionNumber += 1;
						if (fConnection.connectionNumber > Config.FAST_CONNECTION_LIMIT &&
								System.currentTimeMillis() - fConnection.lastConnection <
										Config.NORMAL_CONNECTION_TIME ||
								System.currentTimeMillis() - fConnection.lastConnection < Config.FAST_CONNECTION_TIME ||
								fConnection.connectionNumber > Config.MAX_CONNECTION_PER_IP)
						{
							fConnection.lastConnection = System.currentTimeMillis();
							connection.close();
							fConnection.connectionNumber -= 1;
							if (!fConnection.isFlooding)
							{
								Log.warning("Potential Flood from " + connection.getInetAddress().getHostAddress());
							}
							fConnection.isFlooding = true;
							continue;
						}
						if (fConnection.isFlooding) //if connection was flooding server but now passed the check
						{
							fConnection.isFlooding = false;
							Log.info(connection.getInetAddress().getHostAddress() +
									" is not considered as flooding anymore.");
						}
						fConnection.lastConnection = System.currentTimeMillis();
					}
					else
					{
						fConnection = new ForeignConnection(System.currentTimeMillis());
						floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);
					}
				}
				addClient(connection);
			}
			catch (Exception e)
			{
				try
				{
					connection.close();
				}
				catch (Exception ignored)
				{
				}
				if (isInterrupted())
				{
					// shutdown?
					try
					{
						serverSocket.close();
					}
					catch (IOException io)
					{
						Log.log(Level.INFO, "", io);
					}
					break;
				}
			}
		}
	}

	protected static class ForeignConnection
	{
		public int connectionNumber;
		public long lastConnection;
		public boolean isFlooding = false;

		/**
		 * @param time
		 */
		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
	}

	public abstract void addClient(Socket s);

	public void removeFloodProtection(String ip)
	{
		if (!Config.FLOOD_PROTECTION)
		{
			return;
		}
		ForeignConnection fConnection = floodProtection.get(ip);
		if (fConnection != null)
		{
			fConnection.connectionNumber -= 1;
			if (fConnection.connectionNumber == 0)
			{
				floodProtection.remove(ip);
			}
		}
		else
		{
			Log.warning("Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
		}
	}

	public void close()
	{
		try
		{
			serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
