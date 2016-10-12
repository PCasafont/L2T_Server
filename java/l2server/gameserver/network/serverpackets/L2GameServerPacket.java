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

package l2server.gameserver.network.serverpackets;

import l2server.gameserver.network.L2GameClient;
import l2server.gameserver.network.PacketOpcodes;
import l2server.log.Log;
import l2server.network.SendablePacket;

import java.util.logging.Level;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected int _invisibleCharacter = 0;

	/**
	 * @return True if packet originated from invisible character.
	 */
	public int getInvisibleCharacter()
	{
		return _invisibleCharacter;
	}

	/**
	 * Set "invisible" boolean flag in the packet.
	 * Packets from invisible characters will not be broadcasted to players.
	 *
	 * @param objectId
	 */
	public void setInvisibleCharacter(final int objectId)
	{
		_invisibleCharacter = objectId;
	}

	/**
	 */
	@Override
	protected void write()
	{
		try
		{
			//if (getClient() != null && getClient().getAccountName() != null
			//		&& getClient().getAccountName().equalsIgnoreCase("pere"))
			//	Log.info(getType());

			byte[] opcode = PacketOpcodes.getServerPacketOpcode(getOpCodeClass());
			if (opcode != null)
			{
				writeB(opcode);
			}

			writeImpl();
		}
		catch (Exception e)
		{
			Log.log(Level.SEVERE,
					"Client: " + getClient().toString() + " - Failed writing: " + getType() + " ; " + e.getMessage(),
					e);
		}
	}

	public void runImpl()
	{

	}

	protected abstract void writeImpl();

	protected Class<?> getOpCodeClass()
	{
		return getClass();
	}

	/**
	 * @return A String with this packet name for debugging purposes
	 */
	public final String getType()
	{
		String type = "[S]";
		byte[] opcode = PacketOpcodes.getServerPacketOpcode(getOpCodeClass());
		if (opcode != null)
		{
			type += " " + Integer.toHexString(opcode[0] & 0xff);
			if (opcode.length > 2)
			{
				type += ":" + Integer.toHexString(opcode[1] & 0xff | opcode[2] & 0xff << 8);
			}
			if (opcode.length > 6)
			{
				type += ":" + Integer.toHexString(
						opcode[3] & 0xff | opcode[4] & 0xff << 8 | opcode[5] & 0xff << 16 | opcode[6] & 0xff << 24);
			}
		}

		type += " " + getClass().getSimpleName();
		return type;
	}
}
