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
 * @author Pere
 */
public class ExShowItemAppearanceWindow extends L2GameServerPacket
{
	private int _type;
	private int _stoneId;
	
	public ExShowItemAppearanceWindow(int type, int stoneId)
	{
		_type = type;
		_stoneId = stoneId;
	}
	
	/**
	 * @see l2tserver.util.network.BaseSendablePacket.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x129);
		writeD(_type);
		writeD(0x00); // GoD ???
		writeD(_stoneId);
	}
	
	/**
	 * @see l2tserver.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:12E ExShowItemAppearanceWindow";
	}
}
