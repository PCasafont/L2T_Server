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
import l2server.loginserver.GameServerTable;
import l2server.loginserver.LoginController;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.serverpackets.AccountKicked;
import l2server.loginserver.network.serverpackets.LoginFail;
import l2server.loginserver.network.serverpackets.LoginOk;
import l2server.loginserver.network.serverpackets.ServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

/**
 * Format: x
 * 0 (a leading null)
 * x: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin extends L2LoginClientPacket {
	private static Logger log = LoggerFactory.getLogger(RequestAuthLogin.class.getName());


	private byte[] raw = new byte[256];
	
	private String user;
	private String password;
	private int ncotp;
	
	public String getPassword() {
		return password;
	}
	
	public String getUser() {
		return user;
	}
	
	public int getOneTimePassword() {
		return ncotp;
	}
	
	@Override
	public boolean readImpl() {
		if (super.buf.remaining() >= 256) {
			readB(raw);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void run() {
		byte[] decrypted = null;
		byte[] decrypted2 = null;
		L2LoginClient client = getClient();
		try {
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, client.getRSAPrivateKey());
			decrypted = rsaCipher.doFinal(raw, 0x00, 0x80);
			decrypted2 = rsaCipher.doFinal(raw, 0x80, 0x80);
		} catch (GeneralSecurityException e) {
			log.info("", e);
			return;
		}
		
		user = new String(decrypted, 0x4E, 14).trim();
		user = user.toLowerCase();
		password = new String(decrypted2, 0x5C, 16).trim();
		ncotp = decrypted[0x7c];
		ncotp |= decrypted[0x7d] << 8;
		ncotp |= decrypted[0x7e] << 16;
		ncotp |= decrypted[0x7f] << 24;
		
		LoginController lc = LoginController.getInstance();
		/*try
		{*/
		LoginController.AuthLoginResult result = lc.tryAuthLogin(user, password, client);
		switch (result) {
			case AUTH_SUCCESS:
				client.setAccount(user);
				lc.getCharactersOnAccount(user);
				client.setState(L2LoginClient.LoginClientState.AUTHED_LOGIN);
				client.setSessionKey(lc.assignSessionKeyToClient(user, client));
				if (Config.SHOW_LICENCE) {
					client.sendPacket(new LoginOk(getClient().getSessionKey()));
				} else {
					int time = 0;
					while (getClient().getCharsOnServ() == null && time < 10) {
						try {
							Thread.sleep(100);
						} catch (Exception e) {
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
				if ((oldClient = lc.getAuthedClient(user)) != null) {
					// kick the other client
					oldClient.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeAuthedLoginClient(user);
				}
				// kick also current client
				client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			case ALREADY_ON_GS:
				GameServerTable.GameServerInfo gsi;
				if ((gsi = lc.getAccountOnGameServer(user)) != null) {
					client.close(LoginFail.LoginFailReason.REASON_ACCOUNT_IN_USE);
					
					// kick from there
					if (gsi.isAuthed()) {
						gsi.getGameServerThread().kickPlayer(user);
					}
				}
				break;
		}
		/*}
		catch (HackingException e)
		{
			InetAddress address = getClient().getConnection().getInetAddress();
			lc.addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
			log.info("Banned (" + address + ") for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds, due to " +
					e.getConnects() + " incorrect login attempts.");
		}*/
	}
}
