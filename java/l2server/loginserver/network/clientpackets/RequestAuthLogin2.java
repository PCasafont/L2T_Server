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
import l2server.loginserver.GameServerTable.GameServerInfo;
import l2server.loginserver.LoginController;
import l2server.loginserver.LoginController.AuthLoginResult;
import l2server.loginserver.network.L2LoginClient;
import l2server.loginserver.network.L2LoginClient.LoginClientState;
import l2server.loginserver.network.serverpackets.AccountKicked;
import l2server.loginserver.network.serverpackets.AccountKicked.AccountKickedReason;
import l2server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2server.loginserver.network.serverpackets.LoginOk;
import l2server.loginserver.network.serverpackets.ServerList;

/**
 * @author Pere
 */
public class RequestAuthLogin2 extends L2LoginClientPacket
{
	private String _authKey;

	@Override
	public boolean readImpl()
	{
		readD(); // Session ID
		readD(); // ???
		readD(); // ???
		readD(); // ???
		readD(); // ???
		readC(); // ???
		readH(); // ???
		_authKey = readString();
		readC(); // ???
		readH(); // ???
		readD(); // ???
		readString(); // ???
		readH(); // ???
		readD(); // ???
		readD(); // ???
		readD(); // ???
		readD(); // ???
		return true;
	}

	public String readString()
	{
		String s = "";
		char c = (char) readC();
		while (c != 0)
		{
			s += c;
			c = (char) readC();
		}

		return s;
	}

	/**
	 */
	@Override
	public void run()
	{
		L2LoginClient client = getClient();
		LoginController lc = LoginController.getInstance();
		/*try
		{*/
			AuthLoginResult result = AuthLoginResult.INVALID_PASSWORD;
			String user = lc.loginValid(_authKey, client);
			if (user != null)
			{
				result = lc.tryAuthLogin(_authKey, client, user);
			}

			switch (result)
			{
				case AUTH_SUCCESS:
					client.setAccount(user);
					lc.getCharactersOnAccount(user);
					client.setState(LoginClientState.AUTHED_LOGIN);
					client.setSessionKey(lc.assignSessionKeyToClient(user, client));
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
					client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
					break;
				case ACCOUNT_BANNED:
					client.close(new AccountKicked(AccountKickedReason.REASON_PERMANENTLY_BANNED));
					break;
				case ALREADY_ON_LS:
					L2LoginClient oldClient;
					if ((oldClient = lc.getAuthedClient(user)) != null)
					{
						// kick the other client
						oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
						lc.removeAuthedLoginClient(user);
					}
					// kick also current client
					client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					break;
				case ALREADY_ON_GS:
					GameServerInfo gsi;
					if ((gsi = lc.getAccountOnGameServer(user)) != null)
					{
						client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);

						// kick from there
						if (gsi.isAuthed())
						{
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
			Log.info("Banned (" + address + ") for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds, due to " +
					e.getConnects() + " incorrect login attempts.");
		}*/
	}
}
