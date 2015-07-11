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

import l2tserver.Config;

/**
 * Format: (ch) ddd b
 * d: ?
 * d: crest ID
 * d: crest size
 * b: raw data
 * @author -Wooden-
 *
 */
public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private static final String _S__FE_28_EXPLEDGECRESTLARGE = "[S] FE:1b ExPledgeCrestLarge";
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
	 * @see l2tserver.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x1b);

		writeD(Config.SERVER_ID); // server id?
		writeD(0x00); //unk
		writeD(_crestId);
		writeD(_subId); //subId
		writeD(0x10080); //???
		writeD(_data.length);
		
		writeB(_data);
		
	}
	
	/* (non-Javadoc)
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_28_EXPLEDGECRESTLARGE;
	}
	
}