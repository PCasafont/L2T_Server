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

import l2server.Config;

/**
 * Format: (ch) ddd b
 * d: ?
 * d: crest ID
 * d: crest size
 * b: raw data
 *
 * @author -Wooden-
 */
public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private int _crestId;
	private int _subId;
	private byte[] _data;

	public ExPledgeCrestLarge(int crestId, int subId, byte[] data)
	{
		_crestId = crestId;
		_subId = subId;
		_data = data;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(Config.SERVER_ID); // server id?
		writeD(0x00); //unk
		writeD(_crestId);
		writeD(_subId); //subId
		writeD(0x10080); //???
		writeD(_data.length);

		writeB(_data);
	}
}
