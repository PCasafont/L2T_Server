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
 * format   ddddd
 * <p>
 * sample
 * 0000: 39  0b 07 10 48  3e 31 10 48  3a f6 00 00  91 5b 00	9...H>1.H:....[.
 * 0010: 00  4c f1 ff ff									 .L...
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class TargetSelected extends L2GameServerPacket
{
	private int objectId;
	private int targetObjId;
	private int x;
	private int y;
	private int z;

	/**
	 */
	public TargetSelected(int objectId, int targetId, int x, int y, int z)
	{
		this.objectId = objectId;
		targetObjId = targetId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(objectId);
		writeD(targetObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(0x00);
	}
}
