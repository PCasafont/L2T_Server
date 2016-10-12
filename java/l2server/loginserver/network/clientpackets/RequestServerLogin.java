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
import l2server.L2DatabaseFactory;
import l2server.log.Log;
import l2server.loginserver.LoginController;
import l2server.loginserver.SessionKey;
import l2server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import l2server.loginserver.network.serverpackets.PlayFail.PlayFailReason;
import l2server.loginserver.network.serverpackets.PlayOk;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

/**
 * Fromat is ddc
 * d: first part of session id
 * d: second part of session id
 * c: server ID
 */
public class RequestServerLogin extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _serverId;

	/**
	 * @return
	 */
	public int getSessionKey1()
	{
		return _skey1;
	}

	/**
	 * @return
	 */
	public int getSessionKey2()
	{
		return _skey2;
	}

	/**
	 * @return
	 */
	public int getServerID()
	{
		return _serverId;
	}

	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 9)
		{
			_skey1 = readD();
			_skey2 = readD();
			_serverId = readC();
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 */
	@Override
	public void run()
	{
		SessionKey sk = getClient().getSessionKey();

		// if we didnt showed the license we cant check these values
		if (!Config.SHOW_LICENCE || sk.checkLoginPair(_skey1, _skey2))
		{
			//System.out.println("Logging Into Server " + _serverId);
			int logIntoDimensionId = 0;
			if (_serverId == 32)
			{
				_serverId = 31;

				logIntoDimensionId = 1;

				/*
				if (getClient().getAccessLevel() <= 0)
				{
					getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
					return;
				}*/
			}

			if (LoginController.getInstance().isLoginPossible(getClient(), _serverId))
			{
				getClient().setJoinedGS(true);
				getClient().sendPacket(new PlayOk(sk));

				if (!Config.DATABASE_LOGIN.contains("tenkai"))
				{
					//if (logIntoDimensionId == 1)
					{
						Connection con = null;
						PreparedStatement statement = null;
						try
						{
							con = L2DatabaseFactory.getInstance().getConnection();

							String stmt = "UPDATE accounts SET lastDimensionId = ? WHERE login = ?";
							statement = con.prepareStatement(stmt);
							statement.setInt(1, logIntoDimensionId);
							statement.setString(2, getClient().getAccount());
							statement.executeUpdate();
							statement.close();
						}
						catch (Exception e)
						{
							Log.log(Level.WARNING, "Could not set LastDimensionId: " + e.getMessage(), e);
						}
						finally
						{
							L2DatabaseFactory.close(con);
						}

						Log.info("Update LastDimensionId = " + logIntoDimensionId + " for " + getClient().getAccount());
					}
				}
			}
			else
			{
				getClient().close(PlayFailReason.REASON_SERVER_OVERLOADED);
			}
		}
		else
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
