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

package l2server.gameserver.security;

import l2server.Base64;
import l2server.Config;
import l2server.L2DatabaseFactory;
import l2server.gameserver.LoginServerThread;
import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.serverpackets.Ex2ndPasswordAck;
import l2server.gameserver.network.serverpackets.Ex2ndPasswordCheck;
import l2server.gameserver.network.serverpackets.Ex2ndPasswordVerify;
import l2server.gameserver.util.Util;
import l2server.log.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

/**
 * @author mrTJO
 */
public class SecondaryPasswordAuth
{
	private final L2GameClient activeClient;

	private String password;
	private int wrongAttempts;
	private boolean authed;

	private static final String VAR_PWD = "secauth_pwd";
	private static final String VAR_WTE = "secauth_wte";

	private static final String SELECT_PASSWORD =
			"SELECT var, value FROM account_gsdata WHERE account_name=? AND var LIKE 'secauth_%'";
	private static final String INSERT_PASSWORD = "INSERT INTO account_gsdata VALUES (?, ?, ?)";
	private static final String UPDATE_PASSWORD = "UPDATE account_gsdata SET value=? WHERE account_name=? AND var=?";

	private static final String INSERT_ATTEMPT =
			"INSERT INTO account_gsdata VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?";

	/**
	 *
	 */
	public SecondaryPasswordAuth(L2GameClient activeClient)
	{
		this.activeClient = activeClient;
		this.password = null;
		this.wrongAttempts = 0;
		this.authed = false;
		loadPassword();
	}

	private void loadPassword()
	{
		String var, value = null;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_PASSWORD);
			statement.setString(1, this.activeClient.getAccountName());
			ResultSet rs = statement.executeQuery();
			while (rs.next())
			{
				var = rs.getString("var");
				value = rs.getString("value");

				if (var.equals(VAR_PWD))
				{
					this.password = value;
				}
				else if (var.equals(VAR_WTE))
				{
					this.wrongAttempts = Integer.parseInt(value);
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error while reading password.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		if (this.password != null && this.password.equals("DISABLED"))
		{
			this.authed = true;
		}
	}

	public boolean savePassword(String password)
	{
		if (passwordExist())
		{
			Log.warning("[SecondaryPasswordAuth]" + this.activeClient.getAccountName() + " forced savePassword");
			this.activeClient.closeNow();
			return false;
		}

		if (!validatePassword(password))
		{
			this.activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}

		password = cryptPassword(password);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_PASSWORD);
			statement.setString(1, this.activeClient.getAccountName());
			statement.setString(2, VAR_PWD);
			statement.setString(3, password);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error while writing password.", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		this.password = password;
		return true;
	}

	public boolean insertWrongAttempt(int attempts)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_ATTEMPT);
			statement.setString(1, this.activeClient.getAccountName());
			statement.setString(2, VAR_WTE);
			statement.setString(3, Integer.toString(attempts));
			statement.setString(4, Integer.toString(attempts));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error while writing wrong attempts.", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return true;
	}

	public boolean changePassword(String oldPassword, String newPassword)
	{
		if (!passwordExist())
		{
			Log.warning("[SecondaryPasswordAuth]" + this.activeClient.getAccountName() + " forced changePassword");
			this.activeClient.closeNow();
			return false;
		}

		if (!checkPassword(oldPassword, true))
		{
			return false;
		}

		if (!validatePassword(newPassword))
		{
			this.activeClient.sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.WRONG_PATTERN));
			return false;
		}

		newPassword = cryptPassword(newPassword);

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_PASSWORD);
			statement.setString(1, newPassword);
			statement.setString(2, this.activeClient.getAccountName());
			statement.setString(3, VAR_PWD);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE, "Error while reading password.", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		this.password = newPassword;
		this.authed = false;
		return true;
	}

	public boolean checkPassword(String password, boolean skipAuth)
	{
		password = cryptPassword(password);

		if (!password.equals(this.password))
		{
			this.wrongAttempts++;
			if (this.wrongAttempts < Config.SECOND_AUTH_MAX_ATTEMPTS)
			{
				this.activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_WRONG, this.wrongAttempts));
				insertWrongAttempt(this.wrongAttempts);
				return false;
			}
			else
			{
				LoginServerThread.getInstance().sendTempBan(this.activeClient.getAccountName(),
						this.activeClient.getConnectionAddress().getHostAddress(), Config.SECOND_AUTH_BAN_TIME);
				Log.warning(this.activeClient.getAccountName() + " - (" +
						this.activeClient.getConnectionAddress().getHostAddress() + ") has inputted the wrong password " +
						this.wrongAttempts + " times in row.");
				insertWrongAttempt(0);
				this.activeClient.close(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_BAN,
						Config.SECOND_AUTH_MAX_ATTEMPTS));
				return false;
			}
		}
		if (!skipAuth)
		{
			this.authed = true;
			this.activeClient.sendPacket(new Ex2ndPasswordVerify(Ex2ndPasswordVerify.PASSWORD_OK, this.wrongAttempts));
		}
		insertWrongAttempt(0);
		return true;
	}

	public boolean passwordExist()
	{
		return this.password != null;
	}

	public void openDialog()
	{
		if (passwordExist())
		{
			this.activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_PROMPT));
		}
		else
		{
			this.activeClient.sendPacket(new Ex2ndPasswordCheck(Ex2ndPasswordCheck.PASSWORD_NEW));
		}
	}

	public boolean isAuthed()
	{
		return this.authed;
	}

	private String cryptPassword(String password)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] raw = password.getBytes("UTF-8");
			byte[] hash = md.digest(raw);
			return Base64.encodeBytes(hash);
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.severe("[SecondaryPasswordAuth]Unsupported Algorythm");
		}
		catch (UnsupportedEncodingException e)
		{
			Log.severe("[SecondaryPasswordAuth]Unsupported Encoding");
		}
		return null;
	}

	private boolean validatePassword(String password)
	{
		if (!Util.isDigit(password))
		{
			return false;
		}

		if (password.length() < 6 || password.length() > 8)
		{
			return false;
		}

		/*for (int i = 0; i < password.length()-1; i++)
		{
			char curCh = password.charAt(i);
			char nxtCh = password.charAt(i+1);

			if (curCh+1 == nxtCh)
				return false;
			else if (curCh-1 == nxtCh)
				return false;
			else if (curCh == nxtCh)
				return false;
		}

		for (int i = 0; i < password.length()-2; i++)
		{
			String toChk = password.substring(i+1);
			StringBuffer chkEr = new StringBuffer(password.substring(i, i+2));

			if (toChk.contains(chkEr))
				return false;
			else if (toChk.contains(chkEr.reverse()))
				return false;
		}*/
		this.wrongAttempts = 0;
		return true;
	}
}
