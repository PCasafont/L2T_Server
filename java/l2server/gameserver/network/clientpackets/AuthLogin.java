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

package l2server.gameserver.network.clientpackets;

import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.LoginServerThread;
import l2server.gameserver.LoginServerThread.SessionKey;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.ExLoginVitalityEffectInfo;
import l2server.gameserver.network.serverpackets.L2GameServerPacket;
import l2server.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @version $Revision: 1.9.2.3.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class AuthLogin extends L2GameClientPacket
{
	// loginName + keys must match what the loginserver used.
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;

	/**
	 */
	@Override
	protected void readImpl()
	{
		_loginName = readS().toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		if (_loginName.length() == 0 || !client.isProtocolOk())
		{
			client.close((L2GameServerPacket) null);
			return;
		}
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		if (Config.DEBUG)
		{
			Log.info("user:" + _loginName);
			Log.info("key:" + key);
		}

		// avoid potential exploits
		if (client.getAccountName() == null)
		{
			if (!_loginName.equalsIgnoreCase("IdEmpty"))
			{
				client.setAccountName(_loginName);
				LoginServerThread.getInstance().addGameServerLogin(_loginName, client);
			}
			LoginServerThread.getInstance().addWaitingClientAndSendRequest(_loginName, client, key);
		}
		//sendVitalityInfo(client);
	}

	@SuppressWarnings("unused")
	private void sendVitalityInfo(L2GameClient client)
	{
		Connection con = null;
		int vitalityPoints = Config.STARTING_VITALITY_POINTS;
		int vitalityItemsUsed = 0;
		/*
         *
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT value FROM account_gsdata WHERE account_name=? AND var=?");
				statement.setString(1, getAccountName());
				statement.setString(2, "vit_items_used");
				ResultSet rs = statement.executeQuery();
				if (rs.next())
				{
					_vitalityItemsUsed = Integer.parseInt(rs.getString("value"));
				}
				else
				{
					statement.close();
					statement = con.prepareStatement("INSERT INTO account_gsdata(account_name,var,value) VALUES(?,?,?)");
					statement.setString(1, getAccountName());
					statement.setString(2, "vit_items_used");
					statement.setString(3, String.valueOf(0));
					statement.execute();
				}
				rs.close();
				statement.close();
			}
			catch (Exception e)
			{
				Logozo.log(Level.WARNING, "Could not load player vitality items used count: " + e.getMessage(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}

		 */
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement =
					con.prepareStatement("SELECT value FROM account_gsdata WHERE account_name=? AND var=?");
			statement.setString(1, client.getAccountName());
			statement.setString(2, "vitality");
			ResultSet rset = statement.executeQuery();

			if (rset.next())
			{
				vitalityPoints = rset.getInt("value");
			}
			else
			{
				statement.close();
				statement = con.prepareStatement("INSERT INTO account_gsdata(account_name,var,value) VALUES(?,?,?)");
				statement.setString(1, client.getAccountName());
				statement.setString(2, "vitality");
				statement.setInt(3, Config.STARTING_VITALITY_POINTS);
				statement.execute();
			}

			rset.close();
			statement.close();

			statement = con.prepareStatement("SELECT value FROM account_gsdata WHERE account_name=? AND var=?");
			statement.setString(1, client.getAccountName());
			statement.setString(2, "vit_items_used");
			rset = statement.executeQuery();
			if (rset.next())
			{
				vitalityItemsUsed = rset.getInt("value");
			}
			else
			{
				statement.close();
				statement = con.prepareStatement("INSERT INTO account_gsdata(account_name,var,value) VALUES(?,?,?)");
				statement.setString(1, client.getAccountName());
				statement.setString(2, "vit_items_used");
				statement.setInt(3, 0);
				statement.execute();
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.WARNING, "Could not restore account vitality points: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		client.sendPacket(new ExLoginVitalityEffectInfo(vitalityPoints, vitalityItemsUsed));
	}
}
