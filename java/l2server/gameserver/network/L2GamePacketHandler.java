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

package l2server.gameserver.network;

import l2server.Config;
import l2server.gameserver.network.L2GameClient.GameClientState;
import l2server.gameserver.network.PacketOpcodes.PacketFamily;
import l2server.gameserver.network.clientpackets.L2GameClientPacket;
import l2server.log.Log;
import l2server.network.*;
import l2server.util.Util;

import java.nio.ByteBuffer;

/**
 * Stateful Packet Handler<BR>
 * The Stateful approach prevents the server from handling inconsistent packets, examples:<BR>
 * <li>Clients sends a MoveToLocation packet without having a character attached. (Potential errors handling the packet).</li>
 * <li>Clients sends a RequestAuthLogin being already authed. (Potential exploit).</li>
 * <BR><BR>
 * Note: If for a given exception a packet needs to be handled on more then one state, then it should be added to all these states.
 *
 * @author KenM
 */
public final class L2GamePacketHandler
		implements IPacketHandler<L2GameClient>, IClientFactory<L2GameClient>, IMMOExecutor<L2GameClient>
{

	// implementation
	@Override
	public ReceivablePacket<L2GameClient> handlePacket(ByteBuffer buf, L2GameClient client)
	{
		if (client.dropPacket())
		{
			return null;
		}

		GameClientState state = client.getState();
		PacketFamily family = PacketOpcodes.ClientPacketsFamily;
		L2GameClientPacket msg = null;
		int[] accumOpcodes = new int[5];
		int opcodeCount = 0;
		while (msg == null)
		{
			int opcode;
			switch (family.switchLength)
			{
				case 1:
					opcode = buf.get() & 0xFF;
					break;
				case 2:
					opcode = buf.getShort() & 0xffff;
					break;
				case 4:
				default:
					opcode = buf.getInt();
			}
			accumOpcodes[opcodeCount] = opcode;
			opcodeCount++;

			Object obj = family.children.get(opcode);
			if (obj instanceof PacketFamily)
			{
				family = (PacketFamily) obj;
			}
			else if (obj instanceof Class<?>)
			{
				@SuppressWarnings("unchecked") Class<? extends L2GameClientPacket> packetClass =
						(Class<? extends L2GameClientPacket>) obj;
				try
				{
					msg = packetClass.newInstance();
				}
				catch (Exception e)
				{
					Log.warning("Error while trying to create packet of type: " + packetClass.getCanonicalName());
					e.printStackTrace();
				}
			}
			else
			{
				printDebug(accumOpcodes, opcodeCount, buf, state, client);
				break;
			}
		}

		return msg;
	}

	private void printDebug(int[] opcodes, int opcodeCount, ByteBuffer buf, GameClientState state, L2GameClient client)
	{
		client.onUnknownPacket();
		if (!Config.PACKET_HANDLER_DEBUG)
		{
			return;
		}

		String opcode = "0x" + Integer.toHexString(opcodes[0]);
		for (int i = 1; i < opcodeCount; i++)
		{
			opcode += ":0x" + Integer.toHexString(opcodes[i]);
		}

		int size = buf.remaining();
		Log.warning("Unknown Packet: " + opcode + " on State: " + state.name() + " Client: " + client.toString());
		byte[] array = new byte[size];
		buf.get(array);
		Log.warning(Util.printData(array, size));
	}

	// impl
	@Override
	public L2GameClient create(MMOConnection<L2GameClient> con)
	{
		return new L2GameClient(con);
	}

	@Override
	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		rp.getClient().execute(rp);
	}
}
