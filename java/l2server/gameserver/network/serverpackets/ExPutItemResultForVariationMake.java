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
 * Format: (ch)ddd
 */
public class ExPutItemResultForVariationMake extends L2GameServerPacket
{

	private int itemObjId;
	private int itemId;

	public ExPutItemResultForVariationMake(int itemObjId, int itemId)
	{
		this.itemObjId = itemObjId;
		this.itemId = itemId;
	}

	/**
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(this.itemObjId);
		writeD(this.itemId);
		writeD(1);
	}
}
