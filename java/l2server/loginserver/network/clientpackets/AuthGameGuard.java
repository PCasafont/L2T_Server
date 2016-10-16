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

import l2server.loginserver.network.L2LoginClient.LoginClientState;
import l2server.loginserver.network.serverpackets.GGAuth;
import l2server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import lombok.Getter;

/**
 * @author -Wooden-
 *         Format: ddddd
 */
public class AuthGameGuard extends L2LoginClientPacket
{
	@Getter private int sessionId;
	@Getter private int data1;
	@Getter private int data2;
	@Getter private int data3;
	@Getter private int data4;

	/**
	 */
	@Override
	protected boolean readImpl()
	{
		if (super.buf.remaining() >= 20)
		{
			sessionId = readD();
			data1 = readD();
			data2 = readD();
			data3 = readD();
			data4 = readD();
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
		if (sessionId == getClient().getSessionId())
		{
			getClient().setState(LoginClientState.AUTHED_GG);
			getClient().sendPacket(new GGAuth(getClient().getSessionId()));
		}
		else
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}
