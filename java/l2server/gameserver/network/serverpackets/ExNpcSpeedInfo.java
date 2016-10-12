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

import l2server.gameserver.model.actor.L2Npc;

/**
 * @author Pere
 */
public final class ExNpcSpeedInfo extends L2GameServerPacket
{
	private L2Npc _npc;

	public ExNpcSpeedInfo(L2Npc npc)
	{
		_npc = npc;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_npc.getObjectId());
		writeH(8); // Size
		writeC(0xc0); // Mask (0x80 run spd multiplier, 0x40 atk spd multiplier, ...)
		writeFl(_npc.getMovementSpeedMultiplier());
		writeFl(_npc.getAttackSpeedMultiplier());
		//writeH((short)_npc.getTemplate().baseRunSpd);
		//writeH((short)_npc.getTemplate().baseWalkSpd);
	}
}
