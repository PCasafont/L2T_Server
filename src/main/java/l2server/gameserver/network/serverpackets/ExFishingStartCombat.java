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

import l2server.gameserver.model.actor.L2Character;

/**
 * Format (ch)dddcc
 *
 * @author -Wooden-
 */
public class ExFishingStartCombat extends L2GameServerPacket {
	private L2Character activeChar;
	private int time, hp;
	private int lureType, deceptiveMode, mode;
	
	public ExFishingStartCombat(L2Character character, int time, int hp, int mode, int lureType, int deceptiveMode) {
		activeChar = character;
		this.time = time;
		this.hp = hp;
		this.mode = mode;
		this.lureType = lureType;
		this.deceptiveMode = deceptiveMode;
	}
	
	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl() {
		writeD(activeChar.getObjectId());
		writeD(time);
		writeD(hp);
		writeC(mode); // mode: 0 = resting, 1 = fighting
		writeC(lureType); // 0 = newbie lure, 1 = normal lure, 2 = night lure
		writeC(deceptiveMode); // Fish Deceptive Mode: 0 = no, 1 = yes
	}
}
