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
package l2tserver.gameserver.network.serverpackets;

/**
 * Format: (ch)cs
 * c: message type (0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5)
 * s: message text (only when type is 0 - gm)
 * 
 * @author janiii
 *
 */
public class ExEventMatchMessage extends L2GameServerPacket
{
	private static final String _S__FE_0F_EXEVENTMATCHMESSAGE = "[S] FE:0f ExEventMatchMessage";
	private int _type;
	private String _message;
	
	/**
	 * Create an event match message.
	 * @param type 0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
	 * @param message message to show, only when type is 0 - gm
	 */
	public ExEventMatchMessage(int type, String message)
	{
		_type = type;
		_message = message;
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x0f);
		writeC(_type);
		writeS(_message);
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_0F_EXEVENTMATCHMESSAGE;
	}
}
