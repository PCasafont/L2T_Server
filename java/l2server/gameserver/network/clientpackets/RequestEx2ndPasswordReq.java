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
import l2server.gameserver.network.serverpackets.Ex2ndPasswordAck;
import l2server.gameserver.security.SecondaryPasswordAuth;

/**
 * (ch)cS{S}
 * c: change pass?
 * S: current password
 * S: new password
 *
 * @author mrTJO
 */
public class RequestEx2ndPasswordReq extends L2GameClientPacket
{
	//
	int changePass;
	String password, newPassword;

	@Override
	protected void readImpl()
	{
		this.changePass = readC();
		this.password = readS();
		if (this.changePass == 2)
		{
			this.newPassword = readS();
		}
	}

	@Override
	protected void runImpl()
	{
		if (!Config.SECOND_AUTH_ENABLED)
		{
			return;
		}

		SecondaryPasswordAuth spa = getClient().getSecondaryAuth();
		boolean exVal = false;

		if (this.changePass == 0 && !spa.passwordExist())
		{
			exVal = spa.savePassword(this.password);
		}
		else if (this.changePass == 2 && spa.passwordExist())
		{
			exVal = spa.changePassword(this.password, this.newPassword);
		}

		if (exVal)
		{
			getClient().sendPacket(new Ex2ndPasswordAck(Ex2ndPasswordAck.SUCCESS));
		}
	}
}
