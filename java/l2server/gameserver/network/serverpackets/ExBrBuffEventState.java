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

/**
 * Eva's Inferno event packet.
 * Format: (ch)dddd // time info params: type (1 - %, 2 - npcId), value (depending on type: for type 1 - % value; for type 2 - 20573-20575), state (0-1), endtime (only when type 2)
 */
public class ExBrBuffEventState extends L2GameServerPacket
{
	private int _type; // 1 - %, 2 - npcId
	private int _value; // depending on type: for type 1 - % value; for type 2 - 20573-20575
	private int _state; // 0-1
	private int _endtime; // only when type 2 as unix time in seconds from 1970

	public ExBrBuffEventState(int type, int value, int state, int endtime)
	{
		_type = type;
		_value = value;
		_state = state;
		_endtime = endtime;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */

	/* (non-Javadoc)
	 * @see l2server.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(_type);
		writeD(_value);
		writeD(_state);
		writeD(_endtime);
	}
}
