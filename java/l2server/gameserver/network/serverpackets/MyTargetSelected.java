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
 * <p>
 * sample  bf 73 5d 30 49 01 00
 * <p>
 * format dh	(objectid, color)
 * <p>
 * color 	-xx -> -9 	red<p>
 * -8  -> -6	light-red<p>
 * -5	-> -3	yellow<p>
 * -2	-> 2	white<p>
 * 3	-> 5	green<p>
 * 6	-> 8	light-blue<p>
 * 9	-> xx	blue<p>
 * <p>
 * usually the color equals the level difference to the selected target
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class MyTargetSelected extends L2GameServerPacket
{
	private int _objectId;
	private int _color;

	/**
	 */
	public MyTargetSelected(int objectId, int color)
	{
		_objectId = objectId;
		_color = color;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_objectId);
		writeH(_color);
		writeD(0x00);
	}
}
