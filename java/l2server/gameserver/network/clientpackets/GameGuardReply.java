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

import l2server.gameserver.network.L2GameClient;
import l2server.log.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Format: c dddd
 *
 * @author KenM
 */
public class GameGuardReply extends L2GameClientPacket
{

	private static final byte[] VALID = {
			(byte) 0x88,
			0x40,
			0x1c,
			(byte) 0xa7,
			(byte) 0x83,
			0x42,
			(byte) 0xe9,
			0x15,
			(byte) 0xde,
			(byte) 0xc3,
			0x68,
			(byte) 0xf6,
			0x2d,
			0x23,
			(byte) 0xf1,
			0x3f,
			(byte) 0xee,
			0x68,
			0x5b,
			(byte) 0xc5,
			};

	private byte[] _reply = new byte[8];

	@Override
	protected void readImpl()
	{
		readB(_reply, 0, 4);
		readD();
		readB(_reply, 4, 4);
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] result = md.digest(_reply);
			if (Arrays.equals(result, VALID))
			{
				client.setGameGuardOk(true);
			}
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.log(Level.WARNING, "", e);
		}
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
