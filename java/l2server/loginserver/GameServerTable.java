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
import l2server.log.Log;
import l2server.loginserver.network.gameserverpackets.ServerStatus;
import l2server.util.IPSubnet;
import l2server.util.Rnd;
import l2server.util.xml.XmlDocument;
import l2server.util.xml.XmlNode;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @author KenM
 */
public class GameServerTable
{

	private static GameServerTable _instance;

	// Server Names Config
	private static Map<Integer, String> _serverNames = new HashMap<>();

	// Game Server Table
	private Map<Integer, GameServerInfo> _gameServerTable = new ConcurrentHashMap<>();

	// RSA Config
	private static final int KEYS_SIZE = 10;
	private KeyPair[] _keyPairs;

	public static void load() throws SQLException, GeneralSecurityException
	{
		synchronized (GameServerTable.class)
		{
			if (_instance == null)
			{
				_instance = new GameServerTable();
			}
			else
			{
				throw new IllegalStateException("Load can only be invoked a single time.");
			}
		}
	}

	public static GameServerTable getInstance()
	{
		return _instance;
	}

	public GameServerTable() throws SQLException, NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		loadServerNames();
		Log.info("Loaded " + _serverNames.size() + " server names");

		loadRegisteredGameServers();
		Log.info("Loaded " + _gameServerTable.size() + " registered Game Servers");

		loadRSAKeys();
		Log.info("Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}

	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		keyGen.initialize(spec);

		_keyPairs = new KeyPair[KEYS_SIZE];
		for (int i = 0; i < KEYS_SIZE; i++)
		{
			_keyPairs[i] = keyGen.genKeyPair();
		}
	}

	private void loadServerNames()
	{
		try
		{
			XmlDocument doc = new XmlDocument(new File(Config.DATA_FOLDER + "servername.xml"));
			for (XmlNode node : doc.getFirstChild().getChildren())
			{
				if (node.getName().equals("server"))
				{
					int id = node.getInt("id");
					String name = node.getString("name");
					_serverNames.put(id, name);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void loadRegisteredGameServers() throws SQLException
	{
		Connection con = null;
		PreparedStatement statement = null;

		int id;
		con = L2DatabaseFactory.getInstance().getConnection();
		statement = con.prepareStatement("SELECT * FROM gameservers");
		ResultSet rset = statement.executeQuery();
		GameServerInfo gsi;
		while (rset.next())
		{
			id = rset.getInt("server_id");
			gsi = new GameServerInfo(id, stringToHex(rset.getString("hexid")));
			_gameServerTable.put(id, gsi);
		}
		rset.close();
		statement.close();
		L2DatabaseFactory.close(con);
	}

	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}

	public GameServerInfo getRegisteredGameServerById(int id)
	{
		return _gameServerTable.get(id);
	}

	public boolean hasRegisteredGameServerOnId(int id)
	{
		return _gameServerTable.containsKey(id);
	}

	public boolean registerWithFirstAvaliableId(GameServerInfo gsi)
	{
		// avoid two servers registering with the same "free" id
		synchronized (_gameServerTable)
		{
			for (Entry<Integer, String> entry : _serverNames.entrySet())
			{
				if (!_gameServerTable.containsKey(entry.getKey()))
				{
					_gameServerTable.put(entry.getKey(), gsi);
					gsi.setId(entry.getKey());
					return true;
				}
			}
		}
		return false;
	}

	public boolean register(int id, GameServerInfo gsi)
	{
		// avoid two servers registering with the same id
		synchronized (_gameServerTable)
		{
			if (!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}

	public void registerServerOnDB(GameServerInfo gsi)
	{
		this.registerServerOnDB(gsi.getHexId(), gsi.getId(), gsi.getExternalHost());
	}

	public void registerServerOnDB(byte[] hexId, int id, String externalHost)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO gameservers (hexid,server_id,host) values (?,?,?)");
			statement.setString(1, hexToString(hexId));
			statement.setInt(2, id);
			statement.setString(3, externalHost);
			statement.executeUpdate();
			statement.close();

			register(id, new GameServerInfo(id, hexId));
		}
		catch (SQLException e)
		{
			Log.log(Level.SEVERE, "SQL error while saving gameserver.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public String getServerNameById(int id)
	{
		return getServerNames().get(id);
	}

	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}

	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}

	private byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}

	private String hexToString(byte[] hex)
	{
		if (hex == null)
		{
			return "null";
		}
		return new BigInteger(hex).toString(16);
	}

	public static class GameServerInfo
	{
		// auth
		private int _id;
		private byte[] _hexId;
		private boolean _isAuthed;

		// status
		private GameServerThread _gst;
		private int _status;

		// network
		private ArrayList<GameServerAddress> _addrs = new ArrayList<>(5);
		private int _port;

		// config
		private boolean _isPvp = true;
		private int _serverType;
		private int _ageLimit;
		private boolean _isShowingBrackets;
		private int _maxPlayers;

		public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
		{
			_id = id;
			_hexId = hexId;
			_gst = gst;
			_status = ServerStatus.STATUS_DOWN;
		}

		public GameServerInfo(int id, byte[] hexId)
		{
			this(id, hexId, null);
		}

		public void setId(int id)
		{
			_id = id;
		}

		public int getId()
		{
			return _id;
		}

		public byte[] getHexId()
		{
			return _hexId;
		}

		public void setAuthed(boolean isAuthed)
		{
			_isAuthed = isAuthed;
		}

		public boolean isAuthed()
		{
			return _isAuthed;
		}

		public void setGameServerThread(GameServerThread gst)
		{
			_gst = gst;
		}

		public GameServerThread getGameServerThread()
		{
			return _gst;
		}

		public void setStatus(int status)
		{
			_status = status;
		}

		public int getStatus()
		{
			return _status;
		}

		public int getCurrentPlayerCount()
		{
			if (_gst == null)
			{
				return 0;
			}
			return _gst.getPlayerCount();
		}

		public String getExternalHost()
		{
			try
			{
				return getServerAddress(InetAddress.getByName("0.0.0.0"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		public int getPort()
		{
			return _port;
		}

		public void setPort(int port)
		{
			_port = port;
		}

		public void setMaxPlayers(int maxPlayers)
		{
			_maxPlayers = maxPlayers;
		}

		public int getMaxPlayers()
		{
			return _maxPlayers;
		}

		public boolean isPvp()
		{
			return _isPvp;
		}

		public void setAgeLimit(int val)
		{
			_ageLimit = val;
		}

		public int getAgeLimit()
		{
			return _ageLimit;
		}

		public void setServerType(int val)
		{
			_serverType = val;
		}

		public int getServerType()
		{
			return _serverType;
		}

		public void setShowingBrackets(boolean val)
		{
			_isShowingBrackets = val;
		}

		public boolean isShowingBrackets()
		{
			return _isShowingBrackets;
		}

		public void setDown()
		{
			setAuthed(false);
			setPort(0);
			setGameServerThread(null);
			setStatus(ServerStatus.STATUS_DOWN);
		}

		public void addServerAddress(String subnet, String addr) throws UnknownHostException
		{
			_addrs.add(new GameServerAddress(subnet, addr));
		}

		public String getServerAddress(InetAddress addr)
		{
			for (GameServerAddress a : _addrs)
			{
				if (a.equals(addr))
				{
					return a.getServerAddress();
				}
			}
			return null; // should not happens
		}

		public String[] getServerAddresses()
		{
			String[] result = new String[_addrs.size()];
			for (int i = 0; i < result.length; i++)
			{
				result[i] = _addrs.get(i).toString();
			}

			return result;
		}

		public void clearServerAddresses()
		{
			_addrs.clear();
		}

		private class GameServerAddress extends IPSubnet
		{
			private String _serverAddress;

			public GameServerAddress(String subnet, String address) throws UnknownHostException
			{
				super(subnet);
				_serverAddress = address;
			}

			public String getServerAddress()
			{
				return _serverAddress;
			}

			@Override
			public String toString()
			{
				return _serverAddress + super.toString();
			}
		}
	}
}
