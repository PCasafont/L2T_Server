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
 * Format (ch)ddddd
 *
 * @author -Wooden-
 */
public class ExFishingStart extends L2GameServerPacket {
	private Creature activeChar;
	private int x, y, z, fishType;
	private boolean isNightLure;
	
	public ExFishingStart(Creature character, int fishType, int x, int y, int z, boolean isNightLure) {
		activeChar = character;
		this.fishType = fishType;
		this.x = x;
		this.y = y;
		this.z = z;
		this.isNightLure = isNightLure;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(activeChar.getObjectId());
		writeC(fishType); // fish type
		writeD(x); // x position
		writeD(y); // y position
		writeD(z); // z position
		writeC(isNightLure ? 0x01 : 0x00); // night lure
	}
}
