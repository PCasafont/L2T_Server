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

import l2server.Base64;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.log.Log;
import l2server.loginserver.GameServerTable.GameServerInfo;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.gameserverpackets.ServerStatus;
import l2server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2server.util.Rnd;
import l2server.util.crypt.ScrambledKeyPair;
import l2server.util.lib.LoginLog;
import lombok.Getter;

import javax.crypto.Cipher;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController
{

	@Getter private static LoginController instance;

	/**
	 * Time before kicking the client if he didnt log in yet
	 */
	public static final int LOGIN_TIMEOUT = 600 * 1000;

	/**
	 * Authed Clients on LoginServer
	 */
	protected ConcurrentHashMap<String, L2LoginClient> loginServerClients = new ConcurrentHashMap<>();

	@Getter private final Map<String, BanInfo> bannedIps = new HashMap<>();

	private final Map<InetAddress, FailedLoginAttempt> hackProtection;

	protected ScrambledKeyPair[] keyPairs;

	private final Thread purge;

	protected byte[][] blowfishKeys;
	private static final int BLOWFISH_KEYS = 20;

	public static void load() throws GeneralSecurityException
	{
		synchronized (LoginController.class)
		{
			if (instance == null)
			{
				instance = new LoginController();
			}
			else
			{
				throw new IllegalStateException("LoginController can only be loaded a single time.");
			}
		}
	}


	private LoginController() throws GeneralSecurityException
	{
		Log.info("Loading LoginController...");

		hackProtection = new HashMap<>();

		keyPairs = new ScrambledKeyPair[10];

		KeyPairGenerator keygen = null;

		keygen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
		keygen.initialize(spec);

		//generate the initial set of keys
		for (int i = 0; i < 10; i++)
		{
			keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
		}
		Log.info("Cached 10 KeyPairs for RSA communication");

		testCipher((RSAPrivateKey) keyPairs[0].pair.getPrivate());

		// Store keys for blowfish communication
		generateBlowFishKeys();

		purge = new PurgeThread();
		purge.setDaemon(true);
		purge.start();
	}

	/**
	 * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
	 * In short it avoids the worst-case execution time on runtime by doing it on loading.
	 *
	 * @param key Any private RSA Key just for testing purposes.
	 * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
	 */
	private void testCipher(RSAPrivateKey key) throws GeneralSecurityException
	{
		// avoid worst-case execution, KenM
		Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
		rsaCipher.init(Cipher.DECRYPT_MODE, key);
	}

	private void generateBlowFishKeys()
	{
		blowfishKeys = new byte[BLOWFISH_KEYS][16];

		for (int i = 0; i < BLOWFISH_KEYS; i++)
		{
			for (int j = 0; j < blowfishKeys[i].length; j++)
			{
				blowfishKeys[i][j] = (byte) (Rnd.nextInt(255) + 1);
			}
		}
		Log.info("Stored " + blowfishKeys.length + " keys for Blowfish communication");
	}

	/**
	 * @return Returns a random key
	 */
	public byte[] getBlowfishKey()
	{
		return blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
	}

	public SessionKey assignSessionKeyToClient(String account, L2LoginClient client)
	{
		SessionKey key;

		key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
		loginServerClients.put(account, client);
		return key;
	}

	public void removeAuthedLoginClient(String account)
	{
		if (account == null)
		{
			return;
		}
		loginServerClients.remove(account);
	}

	public boolean isAccountInLoginServer(String account)
	{
		return loginServerClients.containsKey(account);
	}

	public L2LoginClient getAuthedClient(String account)
	{
		return loginServerClients.get(account);
	}

	public enum AuthLoginResult
	{
		INVALID_PASSWORD, ACCOUNT_BANNED, ALREADY_ON_LS, ALREADY_ON_GS, AUTH_SUCCESS
	}

	public AuthLoginResult tryAuthLogin(String account, String password, L2LoginClient client)
	{
		AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
		// check auth
		if (loginValid(account, password, client))
		{
			// login was successful, verify presence on Gameservers
			ret = AuthLoginResult.ALREADY_ON_GS;
			if (!isAccountInAnyGameServer(account))
			{
				// account isnt on any GS verify LS itself
				ret = AuthLoginResult.ALREADY_ON_LS;

				if (loginServerClients.putIfAbsent(account, client) == null)
				{
					ret = AuthLoginResult.AUTH_SUCCESS;
				}
			}
		}
		else
		{
			if (client.getAccessLevel() < 0)
			{
				ret = AuthLoginResult.ACCOUNT_BANNED;
			}
		}
		return ret;
	}

	public AuthLoginResult tryAuthLogin(String sessionKey, L2LoginClient client, String account)
	{
		AuthLoginResult ret = AuthLoginResult.ALREADY_ON_GS;
		if (!isAccountInAnyGameServer(account))
		{
			// account isnt on any GS verify LS itself
			ret = AuthLoginResult.ALREADY_ON_LS;

			if (loginServerClients.putIfAbsent(account, client) == null)
			{
				ret = AuthLoginResult.AUTH_SUCCESS;
			}
		}
		return ret;
	}

	/**
	 * Adds the address to the ban list of the login server, with the given duration.
	 *
	 * @param address    The Address to be banned.
	 * @param expiration Timestamp in miliseconds when this ban expires
	 * @throws UnknownHostException if the address is invalid.
	 */
	public void addBanForAddress(String address, long expiration) throws UnknownHostException
	{
		InetAddress netAddress = InetAddress.getByName(address);
		if (!bannedIps.containsKey(netAddress.getHostAddress()))
		{
			bannedIps.put(netAddress.getHostAddress(), new BanInfo(netAddress, expiration));
		}
	}

	/**
	 * Adds the address to the ban list of the login server, with the given duration.
	 *
	 * @param address  The Address to be banned.
	 * @param duration is miliseconds
	 */
	public void addBanForAddress(InetAddress address, long duration)
	{
		if (!bannedIps.containsKey(address.getHostAddress()))
		{
			bannedIps.put(address.getHostAddress(), new BanInfo(address, System.currentTimeMillis() + duration));
		}
	}

	public boolean isBannedAddress(InetAddress address)
	{
		String[] parts = address.getHostAddress().split("\\.");
		BanInfo bi = bannedIps.get(address.getHostAddress());
		if (bi == null)
		{
			bi = bannedIps.get(parts[0] + "." + parts[1] + "." + parts[2] + ".0");
		}
		if (bi == null)
		{
			bi = bannedIps.get(parts[0] + "." + parts[1] + ".0.0");
		}
		if (bi == null)
		{
			bi = bannedIps.get(parts[0] + ".0.0.0");
		}
		if (bi != null)
		{
			if (bi.hasExpired())
			{
				bannedIps.remove(address.getHostAddress());
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * Remove the specified address from the ban list
	 *
	 * @param address The address to be removed from the ban list
	 * @return true if the ban was removed, false if there was no ban for this ip
	 */
	public boolean removeBanForAddress(InetAddress address)
	{
		return bannedIps.remove(address.getHostAddress()) != null;
	}

	/**
	 * Remove the specified address from the ban list
	 *
	 * @param address The address to be removed from the ban list
	 * @return true if the ban was removed, false if there was no ban for this ip or the address was invalid.
	 */
	public boolean removeBanForAddress(String address)
	{
		try
		{
			return removeBanForAddress(InetAddress.getByName(address));
		}
		catch (UnknownHostException e)
		{
			return false;
		}
	}

	public SessionKey getKeyForAccount(String account)
	{
		L2LoginClient client = loginServerClients.get(account);
		if (client != null)
		{
			return client.getSessionKey();
		}
		return null;
	}

	public L2LoginClient getClientForKey(SessionKey sessionKey)
	{
		for (L2LoginClient client : loginServerClients.values())
		{
			if (client.getSessionKey().equals(sessionKey))
			{
				return client;
			}
		}

		return null;
	}

	public int getOnlinePlayerCount(int serverId)
	{
		GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		if (gsi != null && gsi.isAuthed())
		{
			return gsi.getCurrentPlayerCount();
		}
		return 0;
	}

	public boolean isAccountInAnyGameServer(String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return true;
			}
		}
		return false;
	}

	public GameServerInfo getAccountOnGameServer(String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			GameServerThread gst = gsi.getGameServerThread();
			if (gst != null && gst.hasAccountOnGameServer(account))
			{
				return gsi;
			}
		}
		return null;
	}

	public int getTotalOnlinePlayerCount()
	{
		int total = 0;
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			if (gsi.isAuthed())
			{
				total += gsi.getCurrentPlayerCount();
			}
		}
		return total;
	}

	public void getCharactersOnAccount(String account)
	{
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			if (gsi.isAuthed())
			{
				gsi.getGameServerThread().requestCharacters(account);
			}
		}
	}

	public int getMaxAllowedOnlinePlayers(int id)
	{
		GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(id);
		if (gsi != null)
		{
			return gsi.getMaxPlayers();
		}
		return 0;
	}

	/**
	 * @return
	 */
	public boolean isLoginPossible(L2LoginClient client, int serverId)
	{
		GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
		int access = client.getAccessLevel();
		if (gsi != null && gsi.isAuthed())
		{
			boolean loginOk = gsi.getCurrentPlayerCount() < gsi.getMaxPlayers() &&
					gsi.getStatus() != ServerStatus.STATUS_GM_ONLY || access >= 10;

			if (loginOk && client.getLastServer() != serverId)
			{
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();

					String stmt = "UPDATE accounts SET lastServer = ? WHERE login = ?";
					statement = con.prepareStatement(stmt);
					statement.setInt(1, serverId);
					statement.setString(2, client.getAccount());
					statement.executeUpdate();
					statement.close();
				}
				catch (Exception e)
				{
					Log.log(Level.WARNING, "Could not set lastServer: " + e.getMessage(), e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
			return loginOk;
		}
		return false;
	}

	public void setAccountAccessLevel(String account, int banLevel)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			String stmt = "UPDATE accounts SET accessLevel=? WHERE login=?";
			statement = con.prepareStatement(stmt);
			statement.setInt(1, banLevel);
			statement.setString(2, account);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not set accessLevel: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setAccountLastTracert(String account, String pcIp, String hop1, String hop2, String hop3, String hop4)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			String stmt = "UPDATE accounts SET pcIp=?, hop1=?, hop2=?, hop3=?, hop4=? WHERE login=?";
			statement = con.prepareStatement(stmt);
			statement.setString(1, pcIp);
			statement.setString(2, hop1);
			statement.setString(3, hop2);
			statement.setString(4, hop3);
			statement.setString(5, hop4);
			statement.setString(6, account);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not set last tracert: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setCharactersOnServer(String account, int charsNum, long[] timeToDel, int serverId)
	{
		L2LoginClient client = loginServerClients.get(account);
		if (client == null)
		{
			return;
		}

		if (charsNum > 0)
		{
			client.setCharsOnServ(serverId, charsNum);
		}

		if (timeToDel != null && timeToDel.length > 0)
		{
			client.serCharsWaitingDelOnServ(serverId, timeToDel);
		}
	}

	public boolean isGM(String user)
	{
		boolean ok = false;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT accessLevel FROM accounts WHERE login=?");
			statement.setString(1, user);
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				int accessLevel = rset.getInt(1);
				if (accessLevel > 0)
				{
					ok = true;
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not check gm state:" + e.getMessage(), e);
			ok = false;
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return ok;
	}

	/**
	 * <p>This method returns one of the cached {@link ScrambledKeyPair ScrambledKeyPairs} for communication with Login Clients.</p>
	 *
	 * @return a scrambled keypair
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		return keyPairs[0];
	}

	/**
	 * user name is not case sensitive any more
	 *
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean loginValid(String user, String password, L2LoginClient client)// throws HackingException
	{
		boolean ok = false;
		InetAddress address = client.getConnection().getInetAddress();

		// player disconnected meanwhile
		if (address == null || user == null)
		{
			return false;
		}

		Connection con = null;
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] raw = (password.toLowerCase() + "XjCSl+n/mpc4" + user.toLowerCase()).getBytes("UTF-8");
			byte[] hash = md.digest(raw);

			byte[] expected = null;
			int access = 0;
			int lastServer = 1;
			String userIP = null;

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT password, accessLevel, lastServer, userIP FROM accounts WHERE login=?");
			statement.setString(1, user);
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				expected = Base64.decode(rset.getString("password"));
				access = rset.getInt("accessLevel");
				lastServer = rset.getInt("lastServer");
				userIP = rset.getString("userIP");
				if (lastServer <= 0)
				{
					lastServer = 1; // minServerId is 1 in Interlude
				}
				if (Config.DEBUG)
				{
					Log.fine("account exists");
				}
			}
			rset.close();
			statement.close();

			// if account doesnt exists
			if (expected == null)
			{
				if (Config.AUTO_CREATE_ACCOUNTS)
				{
					if (user.length() >= 2 && user.length() <= 14)
					{
						statement = con.prepareStatement(
								"INSERT INTO accounts (login,password,lastactive,accessLevel,lastIP) values(?,?,?,?,?)");
						statement.setString(1, user);
						statement.setString(2, Base64.encodeBytes(hash));
						statement.setLong(3, System.currentTimeMillis());
						statement.setInt(4, 0);
						statement.setString(5, address.getHostAddress());
						statement.execute();
						statement.close();

						if (Config.LOG_LOGIN_CONTROLLER)
						{
							LoginLog.add("'" + user + "' " + address.getHostAddress() + " - OK : AccountCreate",
									"loginlog");
						}

						Log.info("Created new account for " + user);
						return true;
					}
					if (Config.LOG_LOGIN_CONTROLLER)
					{
						LoginLog.add("'" + user + "' " + address.getHostAddress() + " - ERR : ErrCreatingACC",
								"loginlog");
					}

					Log.warning("Invalid username creation/use attempt: " + user);
					return false;
				}
				else
				{
					if (Config.LOG_LOGIN_CONTROLLER)
					{
						LoginLog.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountMissing",
								"loginlog");
					}

					Log.warning("Account missing for user " + user);
					FailedLoginAttempt failedAttempt = hackProtection.get(address);
					int failedCount;
					if (failedAttempt == null)
					{
						hackProtection.put(address, new FailedLoginAttempt(address, password));
						failedCount = 1;
					}
					else
					{
						failedAttempt.increaseCounter();
						failedCount = failedAttempt.getCount();
					}

					if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
					{
						Log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN +
								" seconds due to " + failedCount + " invalid user name(" + user + ") attempts");
						addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
					}
					return false;
				}
			}
			else
			{
				// is this account banned?
				if (access < 0)
				{
					if (Config.LOG_LOGIN_CONTROLLER)
					{
						LoginLog.add("'" + user + "' " + address.getHostAddress() + " - ERR : AccountBanned",
								"loginlog");
					}

					client.setAccessLevel(access);
					return false;
				}
				// Check IP
				if (userIP != null)
				{
					if (!isValidIPAddress(userIP))
					{
						// Address is not valid so it's a domain name, get IP
						try
						{
							InetAddress addr = InetAddress.getByName(userIP);
							userIP = addr.getHostAddress();
						}
						catch (Exception e)
						{
							return false;
						}
					}
					if (!address.getHostAddress().equalsIgnoreCase(userIP))
					{
						if (Config.LOG_LOGIN_CONTROLLER)
						{
							LoginLog.add("'" + user + "' " + address.getHostAddress() + "/" + userIP +
									" - ERR : INCORRECT IP", "loginlog");
						}

						return false;
					}
				}
				// check password hash
				ok = true;
				for (int i = 0; i < expected.length; i++)
				{
					if (hash[i] != expected[i])
					{
						ok = false;
						break;
					}
				}
			}

			if (ok)
			{
				client.setAccessLevel(access);
				client.setLastServer(lastServer);
				PreparedStatement statement2 =
						con.prepareStatement("SELECT lastIP, lastIP2 FROM accounts WHERE login=?");
				statement2.setString(1, user);
				rset = statement2.executeQuery();
				String lastIP = null;
				String lastIP2 = null;
				if (rset.next())
				{
					lastIP = rset.getString("lastIP");
					lastIP2 = rset.getString("lastIP2");
				}
				rset.close();
				statement2.close();
				if (lastIP == null || !lastIP.equals(address.getHostAddress()))
				{
					PreparedStatement statement3 = con.prepareStatement(
							"UPDATE accounts SET lastactive=?, lastIP=?, lastIP2=?, lastIP3=? WHERE login=?");
					statement3.setLong(1, System.currentTimeMillis());
					statement3.setString(2, address.getHostAddress());
					statement3.setString(3, lastIP);
					statement3.setString(4, lastIP2);
					statement3.setString(5, user);
					statement3.execute();
					statement3.close();
				}
				else
				{
					PreparedStatement statement3 =
							con.prepareStatement("UPDATE accounts SET lastactive=? WHERE login=?");
					statement3.setLong(1, System.currentTimeMillis());
					statement3.setString(2, user);
					statement3.execute();
					statement3.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not check password(" + user + "):" + e.getMessage(), e);
			ok = false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (!ok)
		{
			if (Config.LOG_LOGIN_CONTROLLER)
			{
				LoginLog.add("'" + user + "' " + address.getHostAddress() + " - ERR : LoginFailed", "loginlog");
			}

			FailedLoginAttempt failedAttempt = hackProtection.get(address);
			int failedCount;
			if (failedAttempt == null)
			{
				hackProtection.put(address, new FailedLoginAttempt(address, password));
				failedCount = 1;
			}
			else
			{
				failedAttempt.increaseCounter(password);
				failedCount = failedAttempt.getCount();
			}

			if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
			{
				Log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN +
						" seconds due to " + failedCount + " invalid user/pass attempts");
				addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			}
		}
		else
		{
			hackProtection.remove(address);
			if (Config.LOG_LOGIN_CONTROLLER)
			{
				LoginLog.add("'" + user + "' " + address.getHostAddress() + " - OK : LoginOk", "loginlog");
			}
		}

		return ok;
	}

	/**
	 * user name is not case sensitive any more
	 *
	 * @return
	 */
	public String loginValid(String sessionKey, L2LoginClient client)// throws HackingException
	{
		boolean ok = false;
		InetAddress address = client.getConnection().getInetAddress();

		// player disconnected meanwhile
		if (address == null || sessionKey == null)
		{
			return null;
		}

		String login = null;
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT login, expiryTime FROM auth_sessions WHERE `key`=?");
			statement.setString(1, sessionKey);
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				login = rset.getString("login");
				long expiryTime = rset.getLong("expiryTime");
				if (expiryTime > System.currentTimeMillis())
				{
					ok = true;
				}
			}
			rset.close();
			statement.close();

			if (ok)
			{
				int access = 0;
				int lastServer = 1;
				statement = con.prepareStatement("SELECT accessLevel, lastServer, userIP FROM accounts WHERE login=?");
				statement.setString(1, login);
				rset = statement.executeQuery();
				if (rset.next())
				{
					access = rset.getInt("accessLevel");
					lastServer = rset.getInt("lastServer");
					if (lastServer <= 0)
					{
						lastServer = 1; // minServerId is 1 in Interlude
					}
				}
				rset.close();
				statement.close();

				client.setAccessLevel(access);
				client.setLastServer(lastServer);
				PreparedStatement statement2 =
						con.prepareStatement("SELECT lastIP, lastIP2 FROM accounts WHERE login=?");
				statement2.setString(1, login);
				rset = statement2.executeQuery();
				String lastIP = null;
				String lastIP2 = null;
				if (rset.next())
				{
					lastIP = rset.getString("lastIP");
					lastIP2 = rset.getString("lastIP2");
				}
				rset.close();
				statement2.close();
				if (lastIP == null || !lastIP.equals(address.getHostAddress()))
				{
					PreparedStatement statement3 = con.prepareStatement(
							"UPDATE accounts SET lastactive=?, lastIP=?, lastIP2=?, lastIP3=? WHERE login=?");
					statement3.setLong(1, System.currentTimeMillis());
					statement3.setString(2, address.getHostAddress());
					statement3.setString(3, lastIP);
					statement3.setString(4, lastIP2);
					statement3.setString(5, login);
					statement3.execute();
					statement3.close();
				}
				else
				{
					PreparedStatement statement3 =
							con.prepareStatement("UPDATE accounts SET lastactive=? WHERE login=?");
					statement3.setLong(1, System.currentTimeMillis());
					statement3.setString(2, login);
					statement3.execute();
					statement3.close();
				}
			}
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not check password(" + sessionKey + "):" + e.getMessage(), e);
			ok = false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (!ok)
		{
			login = null;
			if (Config.LOG_LOGIN_CONTROLLER)
			{
				LoginLog.add("'" + sessionKey + "' " + address.getHostAddress() + " - ERR : LoginFailed", "loginlog");
			}

			FailedLoginAttempt failedAttempt = hackProtection.get(address);
			int failedCount;
			if (failedAttempt == null)
			{
				hackProtection.put(address, new FailedLoginAttempt(address, sessionKey));
				failedCount = 1;
			}
			else
			{
				failedAttempt.increaseCounter(sessionKey);
				failedCount = failedAttempt.getCount();
			}

			if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN)
			{
				Log.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN +
						" seconds due to " + failedCount + " invalid user/pass attempts");
				addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			}
		}
		else
		{
			hackProtection.remove(address);
			if (Config.LOG_LOGIN_CONTROLLER)
			{
				LoginLog.add("'" + sessionKey + "' " + address.getHostAddress() + " - OK : LoginOk", "loginlog");
			}
		}

		return login;
	}

	public boolean loginBanned(String user)
	{
		boolean ok = false;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT accessLevel FROM accounts WHERE login=?");
			statement.setString(1, user);
			ResultSet rset = statement.executeQuery();
			if (rset.next())
			{
				int accessLevel = rset.getInt(1);
				if (accessLevel < 0)
				{
					ok = true;
				}
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			// digest algo not found ??
			// out of bounds should not be possible
			Log.log(Level.WARNING, "Could not check ban state:" + e.getMessage(), e);
			ok = false;
		}
		finally
		{
			try
			{
				L2DatabaseFactory.close(con);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return ok;
	}

	public boolean isValidIPAddress(String ipAddress)
	{
		String[] parts = ipAddress.split("\\.");
		if (parts.length != 4)
		{
			return false;
		}

		for (String s : parts)
		{
			int i = Integer.parseInt(s);
			if (i < 0 || i > 255)
			{
				return false;
			}
		}
		return true;
	}

	class FailedLoginAttempt
	{
		//private InetAddress ipAddress;
		@Getter private int count;
		private long lastAttempTime;
		private String lastPassword;

		public FailedLoginAttempt(InetAddress address, String lastPassword)
		{
			//_ipAddress = address;
			count = 1;
			lastAttempTime = System.currentTimeMillis();
			this.lastPassword = lastPassword;
		}

		public void increaseCounter(String password)
		{
			if (!lastPassword.equals(password))
			{
				// check if theres a long time since last wrong try
				if (System.currentTimeMillis() - lastAttempTime < 300 * 1000)
				{
					count++;
				}
				else
				{
					// restart the status
					count = 1;
				}
				lastPassword = password;
				lastAttempTime = System.currentTimeMillis();
			}
			else
			//trying the same password is not brute force
			{
				lastAttempTime = System.currentTimeMillis();
			}
		}


		public void increaseCounter()
		{
			count++;
		}
	}

	class BanInfo
	{
		private final InetAddress ipAddress;
		// Expiration
		private final long expiration;

		public BanInfo(InetAddress ipAddress, long expiration)
		{
			this.ipAddress = ipAddress;
			this.expiration = expiration;
		}

		public InetAddress getAddress()
		{
			return ipAddress;
		}

		public boolean hasExpired()
		{
			return System.currentTimeMillis() > expiration && expiration > 0;
		}
	}

	class PurgeThread extends Thread
	{
		public PurgeThread()
		{
			setName("PurgeThread");
		}

		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				for (L2LoginClient client : loginServerClients.values())
				{
					if (client == null)
					{
						continue;
					}
					if (client.getConnectionStartTime() + LOGIN_TIMEOUT < System.currentTimeMillis())
					{
						client.close(LoginFailReason.REASON_ACCESS_FAILED);
					}
				}

				try
				{
					Thread.sleep(LOGIN_TIMEOUT / 2);
				}
				catch (InterruptedException e)
				{
					return;
				}
			}
		}
	}
}
