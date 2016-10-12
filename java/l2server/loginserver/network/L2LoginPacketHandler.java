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

import l2server.log.Log;
import l2server.loginserver.network.L2LoginClient.LoginClientState;
import l2server.loginserver.network.clientpackets.*;
import l2server.network.IPacketHandler;
import l2server.network.ReceivablePacket;
import l2server.util.Util;

import java.nio.ByteBuffer;

/**
 * Handler for packets received by Login Server
 *
 * @author KenM
 */
public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{

	/**
	 */
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;

		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();

		switch (state)
		{
			case CONNECTED:
				switch (opcode)
				{
					case 0x07:
						packet = new AuthGameGuard();
						break;
					default:
						debugOpcode(buf, opcode, state);
						break;
				}
				break;
			case AUTHED_GG:
				switch (opcode)
				{
					case 0x00:
						packet = new RequestAuthLogin();
						break;
					case 0x12:
						packet = new RequestAuthLogin2();
						break;
					default:
						debugOpcode(buf, opcode, state);
						break;
				}
				break;
			case AUTHED_LOGIN:
				switch (opcode)
				{
					case 0x02:
						packet = new RequestServerLogin();
						break;
					case 0x05:
						packet = new RequestServerList();
						break;
					default:
						debugOpcode(buf, opcode, state);
						break;
				}
				break;
		}
		return packet;
	}

	private void debugOpcode(ByteBuffer buf, int opcode, LoginClientState state)
	{
		Log.info("Unknown Opcode: " + opcode + " for state: " + state.name());
		String op = "0x" + Integer.toHexString(opcode);

		int size = buf.remaining();
		Log.warning("Unknown Packet: " + op + " on State: " + state.name());
		byte[] array = new byte[size];
		buf.get(array);
		Log.warning(Util.printData(array, size));
	}
}
