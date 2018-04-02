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
 * 0000: 01  7a 73 10 4c  b2 0b 00 00  a3 fc 00 00  e8 f1 ff	.zs.L...........
 * 0010: ff  bd 0b 00 00  b3 fc 00 00  e8 f1 ff ff			 .............
 * <p>
 * <p>
 * ddddddd
 *
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:57 $
 */
public final class MoveToLocation extends L2GameServerPacket {
	private int charObjId, x, y, z, xDst, yDst, zDst;
	
	public MoveToLocation(Creature cha) {
		charObjId = cha.getObjectId();
		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();
		xDst = cha.getXdestination();
		yDst = cha.getYdestination();
		zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(charObjId);
		
		writeD(xDst);
		writeD(yDst);
		writeD(zDst);
		
		writeD(x);
		writeD(y);
		writeD(z);
	}
}
