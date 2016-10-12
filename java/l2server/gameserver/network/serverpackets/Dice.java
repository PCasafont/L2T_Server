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
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:40 $
 */
public class Dice extends L2GameServerPacket
{
	private int _charObjId;
	private int _itemId;
	private int _number;
	private int _x;
	private int _y;
	private int _z;

	/**
	 * 0xd4 Dice		 dddddd
	 */
	public Dice(int charObjId, int itemId, int number, int x, int y, int z)
	{
		_charObjId = charObjId;
		_itemId = itemId;
		_number = number;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_charObjId); //object id of player
		writeD(_itemId); //	item id of dice (spade)  4625,4626,4627,4628
		writeD(_number); //number rolled
		writeD(_x); //x
		writeD(_y); //y
		writeD(_z); //z
	}
}
