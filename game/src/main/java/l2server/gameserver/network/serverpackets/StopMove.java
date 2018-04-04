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

import l2server.gameserver.model.actor.Creature;

/**
 * format   ddddd
 * <p>
 * sample
 * 0000: 59 1a 95 20 48 44 17 02 00 03 f0 fc ff 98 f1 ff	Y.. HD..........
 * 0010: ff c1 1a 00 00									 .....
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class StopMove extends L2GameServerPacket {
	private int objectId;
	private int x;
	private int y;
	private int z;
	private int heading;
	
	public StopMove(Creature cha) {
		this(cha.getObjectId(), cha.getX(), cha.getY(), cha.getZ(), cha.getHeading());
	}
	
	public StopMove(int objectId, int x, int y, int z, int heading) {
		this.objectId = objectId;
		this.x = x;
		this.y = y;
		this.z = z;
		this.heading = heading;
	}
	
	@Override
	protected final void writeImpl() {
		writeD(objectId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
	}
}
