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
 * This class ...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ExAutoSoulShot extends L2GameServerPacket
{
	private int _itemId;
	private int _enable;
	private int _type;

	/**
	 * 0xfe:0x12 ExAutoSoulShot		 (ch)dd
	 */
	public ExAutoSoulShot(int itemId, int enable, int type)
	{
		_itemId = itemId;
		_enable = enable;
		_type = type;
	}

	@Override
	protected final void writeImpl()
	{ // sub id
		writeD(_itemId);
		writeD(_enable);
		writeD(_type);
	}
}
