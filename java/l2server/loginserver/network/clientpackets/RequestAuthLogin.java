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

package l2server.loginserver.network.clientpackets;

import l2server.Config;
import l2server.log.Log;
import l2server.loginserver.GameServerTable;
import l2server.loginserver.LoginController;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.serverpackets.AccountKicked;
import l2server.loginserver.network.serverpackets.LoginFail;
import l2server.loginserver.network.serverpackets.LoginOk;
import l2server.loginserver.network.serverpackets.ServerList;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.util.logging.Level;

/**
 * Format: x
 * 0 (a leading null)
 * x: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	private byte[] raw = new byte[256];

	private String user;
	private String password;
	private int ncotp;

	/**
	 * @return
	 */
	public String getPassword()
	{
		return this.password;
	}

	/**
	 * @return
	 */
	public String getUser()
	{
		return this.user;
	}

	public int getOneTimePassword()
	{
		return this.ncotp;
	}

	@Override
	public boolean readImpl()
	{
		if (super.buf.remaining() >= 256)
		{
			readB(this.raw);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void run()
	{
		byte[] decrypted = null;
		byte[] decrypted2 = null;
		L2LoginClient client = getClient();
		try
		{
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(this.raw, 0x00, 0x80);
			decrypted2 = rsaCipher.doFinal(this.raw, 0x80, 0x80);
		}
		catch (GeneralSecurityException e)
		{
			Log.log(Level.INFO, "", e);
			return;
		}

		this.user = new String(decrypted, 0x4E, 14).trim();
		this.user = this.user.toLowerCase();
		this.password = new String(decrypted2, 0x5C, 16).trim();
		this.ncotp = decrypted[0x7c];
		this.ncotp |= decrypted[0x7d] << 8;
		this.ncotp |= decrypted[0x7e] << 16;
		this.ncotp |= decrypted[0x7f] << 24;

		LoginController lc = LoginController.getInstance();
		/*try
		{*/
			LoginController.AuthLoginResult result = lc.tryAuthLogin(this.user, this.password, client);
			switch (result)
			{
				case AUTH_SUCCESS:
					client.setAccount(this.user);
					lc.getCharactersOnAccount(this.user);
					client.setState(L2LoginClient.LoginClientState.AUTHED_LOGIN);
					client.setSessionKey(lc.assignSessionKeyToClient(this.user, client));
					if (Config.SHOW_LICENCE)
					{
						client.sendPacket(new LoginOk(getClient().getSessionKey()));
					}
					else
					{
						int time = 0;
						while (getClient().getCharsOnServ() == null && time < 10)
						{
							try
							{
								Thread.sleep(100);
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							time++;
						}
						getClient().sendPacket(new ServerList(getClient()));
					}
					break;
				case INVALID_PASSWORD:
					client.close(LoginFail.LoginFailReason.REASON_USER_OR_PASS_WRONG);
					break;
				case ACCOUNT_BANNED:
					client.close(new AccountKicked(AccountKicked.AccountKickedReason.REASON_PERMANENTLY_BANNED));
					break;
				case ALREADY_ON_LS:
					L2LoginClient oldClient;
					if ((oldClient = lc.getAuthedClient(this.user)) != null)
					{
						// kick the other client
						oldClient.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
						lc.removeAuthedLoginClient(this.user);
					}
					// kick also current client
					client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
					break;
				case ALREADY_ON_GS:
					GameServerTable.GameServerInfo gsi;
					if ((gsi = lc.getAccountOnGameServer(this.user)) != null)
					{
						client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);

						// kick from there
						if (gsi.isAuthed())
						{
							gsi.getGameServerThread().kickPlayer(this.user);
						}
					}
					break;
			}
		/*}
		catch (HackingException e)
		{
			InetAddress address = getClient().getConnection().getInetAddress();
			lc.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			Log.info("Banned (" + address + ") for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds, due to " +
					e.getConnects() + " incorrect login attempts.");
		}*/
	}
}
