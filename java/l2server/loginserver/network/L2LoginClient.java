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

package l2server.loginserver.network;

import l2server.Config;
import l2server.log.Log;
import l2server.loginserver.LoginController;
import l2server.loginserver.SessionKey;
import l2server.loginserver.network.serverpackets.L2LoginServerPacket;
import l2server.loginserver.network.serverpackets.LoginFail;
import l2server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2server.loginserver.network.serverpackets.PlayFail;
import l2server.loginserver.network.serverpackets.PlayFail.PlayFailReason;
import l2server.network.MMOClient;
import l2server.network.MMOConnection;
import l2server.network.SendablePacket;
import l2server.util.Rnd;
import l2server.util.crypt.LoginCrypt;
import l2server.util.crypt.ScrambledKeyPair;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a client connected into the LoginServer
 *
 * @author KenM
 */
public final class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{

	public enum LoginClientState
	{
		CONNECTED, AUTHED_GG, AUTHED_LOGIN
	}

	private LoginClientState state;

	// Crypt
	private LoginCrypt loginCrypt;
	private ScrambledKeyPair scrambledPair;
	private byte[] blowfishKey;

	private String account;
	private int accessLevel;
	private int lastServer;
	private SessionKey sessionKey;
	public int sessionId;
	private boolean joinedGS;
	private Map<Integer, Integer> charsOnServers;
	private Map<Integer, long[]> charsToDelete;

	private long connectionStartTime;

	/**
	 * @param con
	 */
	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		this.state = LoginClientState.CONNECTED;
		this.scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
		this.blowfishKey = LoginController.getInstance().getBlowfishKey();
		this.sessionId = Rnd.nextInt();
		this.connectionStartTime = System.currentTimeMillis();
		this.loginCrypt = new LoginCrypt();
		this.loginCrypt.setKey(this.blowfishKey);
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = false;
		try
		{
			ret = this.loginCrypt.decrypt(buf.array(), buf.position(), size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			super.getConnection().close((SendablePacket<L2LoginClient>) null);
			return false;
		}

		if (!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			if (dump[0] != 2 || dump[1] != 0)
			{
				Log.warning("Wrong checksum from client: " + toString());
			}
			//super.getConnection().close((SendablePacket<L2LoginClient>)null);
		}

		return ret;
	}

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = this.loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}

		buf.position(offset + size);
		return true;
	}

	public LoginClientState getState()
	{
		return this.state;
	}

	public void setState(LoginClientState state)
	{
		this.state = state;
	}

	public byte[] getBlowfishKey()
	{
		return this.blowfishKey;
	}

	public byte[] getScrambledModulus()
	{
		return this.scrambledPair.scrambledModulus;
	}

	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) this.scrambledPair.pair.getPrivate();
	}

	public String getAccount()
	{
		return this.account;
	}

	public void setAccount(String account)
	{
		this.account = account;
	}

	public void setAccessLevel(int accessLevel)
	{
		this.accessLevel = accessLevel;
	}

	public int getAccessLevel()
	{
		return this.accessLevel;
	}

	public void setLastServer(int lastServer)
	{
		this.lastServer = lastServer;
	}

	public int getLastServer()
	{
		return this.lastServer;
	}

	public int getSessionId()
	{
		return this.sessionId;
	}

	public boolean hasJoinedGS()
	{
		return this.joinedGS;
	}

	public void setJoinedGS(boolean val)
	{
		this.joinedGS = val;
	}

	public void setSessionKey(SessionKey sessionKey)
	{
		this.sessionKey = sessionKey;
	}

	public SessionKey getSessionKey()
	{
		return this.sessionKey;
	}

	public long getConnectionStartTime()
	{
		return this.connectionStartTime;
	}

	public void sendPacket(L2LoginServerPacket lsp)
	{
		getConnection().sendPacket(lsp);
	}

	public void close(LoginFailReason reason)
	{
		getConnection().close(new LoginFail(reason));
	}

	public void close(PlayFailReason reason)
	{
		getConnection().close(new PlayFail(reason));
	}

	public void close(L2LoginServerPacket lsp)
	{
		getConnection().close(lsp);
	}

	public void setCharsOnServ(int servId, int chars)
	{
		if (this.charsOnServers == null)
		{
			this.charsOnServers = new HashMap<>();
		}
		this.charsOnServers.put(servId, chars);
	}

	public Map<Integer, Integer> getCharsOnServ()
	{
		return this.charsOnServers;
	}

	public void serCharsWaitingDelOnServ(int servId, long[] charsToDel)
	{
		if (this.charsToDelete == null)
		{
			this.charsToDelete = new HashMap<>();
		}
		this.charsToDelete.put(servId, charsToDel);
	}

	public Map<Integer, long[]> getCharsWaitingDelOnServ()
	{
		return this.charsToDelete;
	}

	@Override
	public void onDisconnection()
	{
		if (Config.DEBUG)
		{
			Log.info("DISCONNECTED: " + toString());
		}

		if (!hasJoinedGS())// || (getConnectionStartTime() + LoginController.LOGIN_TIMEOUT) < System.currentTimeMillis())
		{
			LoginController.getInstance().removeAuthedLoginClient(getAccount());
		}
		/*ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
			public void run()
			{
				LoginController.getInstance().removeAuthedLoginClient(getAccount());
			}
		}, hasJoinedGS() ? 30000 : 1000);*/
	}

	@Override
	public String toString()
	{
		InetAddress address = getConnection().getInetAddress();
		if (getState() == LoginClientState.AUTHED_LOGIN)
		{
			return "[" + getAccount() + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
		}
		else
		{
			return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
		}
	}

	@Override
	protected void onForcedDisconnection()
	{
		// Empty
	}
}
