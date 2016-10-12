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
import l2server.L2DatabaseFactory;
import l2server.ServerMode;
import l2server.log.Log;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.L2LoginPacketHandler;
import l2server.network.Core;
import l2server.network.CoreConfig;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * @author KenM
 */
public class L2LoginServer
{
	public static final int PROTOCOL_REV = 0x0106;

	private static L2LoginServer _instance;
	private GameServerListener _gameServerListener;
	private Core<L2LoginClient> _selectorThread;

	public static void main(String[] args)
	{
		_instance = new L2LoginServer();
	}

	public static L2LoginServer getInstance()
	{
		return _instance;
	}

	public L2LoginServer()
	{
		ServerMode.serverMode = ServerMode.MODE_LOGINSERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file

        /* Main */
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();

		// Create input stream for log file -- or store file data into memory
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(LOG_NAME));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		// Load Config
		Config.load();

		// Prepare Database
		L2DatabaseFactory.getInstance();

		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			Log.log(Level.SEVERE, "FATAL: Failed initializing LoginController. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		try
		{
			GameServerTable.load();
		}
		catch (GeneralSecurityException | SQLException e)
		{
			Log.log(Level.SEVERE, "FATAL: Failed to load GameServerTable. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		loadBanFile();

		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (UnknownHostException e)
			{
				Log.log(Level.WARNING,
						"WARNING: The LoginServer bind address is invalid, using all avaliable IPs. Reason: " +
								e.getMessage(), e);
			}
		}

		final CoreConfig sc = new CoreConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;

		final L2LoginPacketHandler lph = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new Core<>(sc, sh, lph, sh, sh);
		}
		catch (IOException e)
		{
			Log.log(Level.SEVERE, "FATAL: Failed to open Selector. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			Log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" +
					Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			Log.log(Level.SEVERE, "FATAL: Failed to start the Game Server Listener. Reason: " + e.getMessage(), e);
			System.exit(1);
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		}
		catch (IOException e)
		{
			Log.log(Level.SEVERE, "FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();

		Log.info("Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" +
				Config.PORT_LOGIN);
	}

	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}

	private void loadBanFile()
	{
		File bannedFile = new File("./banned_ip.cfg");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(bannedFile);
			}
			catch (FileNotFoundException e)
			{
				Log.log(Level.WARNING,
						"Failed to load banned IPs file (" + bannedFile.getName() + ") for reading. Reason: " +
								e.getMessage(), e);
				return;
			}

			LineNumberReader reader = null;
			String line;
			String[] parts;
			try
			{
				reader = new LineNumberReader(new InputStreamReader(fis));

				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					// check if this line isnt a comment line
					if (line.length() > 0 && line.charAt(0) != '#')
					{
						// split comments if any
						parts = line.split("#", 2);

						// discard comments in the line, if any
						line = parts[0];

						parts = line.split(" ");

						String address = parts[0];

						long duration = 0;

						if (parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch (NumberFormatException e)
							{
								Log.warning("Skipped: Incorrect ban duration (" + parts[1] + ") on (" +
										bannedFile.getName() + "). Line: " + reader.getLineNumber());
								continue;
							}
						}

						try
						{
							LoginController.getInstance().addBanForAddress(address, duration);
						}
						catch (UnknownHostException e)
						{
							Log.warning("Skipped: Invalid address (" + parts[0] + ") on (" + bannedFile.getName() +
									"). Line: " + reader.getLineNumber());
						}
					}
				}
			}
			catch (IOException e)
			{
				Log.log(Level.WARNING,
						"Error while reading the bans file (" + bannedFile.getName() + "). Details: " + e.getMessage(),
						e);
			}
			finally
			{
				try
				{
					reader.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				try
				{
					fis.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			Log.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			Log.warning("IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
	}

	public void shutdown(boolean restart)
	{
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}
