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

import l2tserver.gameserver.model.L2ItemInstance;

/**
 *
 * @author  KenM
 */
public final class ExRpItemLink extends L2ItemListPacket
{
	private final L2ItemInstance _item;
	
	public ExRpItemLink(L2ItemInstance item)
	{
		_item = item;
	}
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] FE:6C ExRpItemLink";
	}
	
	/**
	 * @see l2tserver.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x6d);

		writeItem(_item);
	}
}
