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
 * format  dddd
 * <p>
 * sample
 * 0000: 3a  69 08 10 48  02 c1 00 00  f7 56 00 00  89 ea ff	:i..H.....V.....
 * 0010: ff  0c b2 d8 61									 ....a
 *
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class TargetUnselected extends L2GameServerPacket {
	private int targetObjId;
	private int x;
	private int y;
	private int z;
	
	/**
	 */
	public TargetUnselected(Creature character) {
		targetObjId = character.getObjectId();
		x = character.getX();
		y = character.getY();
		z = character.getZ();
	}
	
	@Override
	protected final void writeImpl() {
		writeD(targetObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(0x00); //??
	}
}
