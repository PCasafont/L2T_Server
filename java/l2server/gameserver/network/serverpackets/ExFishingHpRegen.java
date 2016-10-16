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
 * Format (ch)dddcccd
 * d: cahacter oid
 * d: time left
 * d: fish hp
 * c:
 * c:
 * c: 00 if fish gets damage 02 if fish regens
 * d:
 *
 * @author -Wooden-
 */
public class ExFishingHpRegen extends L2GameServerPacket
{
	private L2Character activeChar;
	private int time, fishHP, hpMode, anim, goodUse, penalty, hpBarColor;

	public ExFishingHpRegen(L2Character character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
	{
		activeChar = character;
		this.time = time;
		this.fishHP = fishHP;
		hpMode = HPmode;
		goodUse = GoodUse;
		this.anim = anim;
		this.penalty = penalty;
		this.hpBarColor = hpBarColor;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeD(activeChar.getObjectId());
		writeD(time);
		writeD(fishHP);
		writeC(hpMode); // 0 = HP stop, 1 = HP raise
		writeC(goodUse); // 0 = none, 1 = success, 2 = failed
		writeC(anim); // Anim: 0 = none, 1 = reeling, 2 = pumping
		writeD(penalty); // Penalty
		writeC(hpBarColor); // 0 = normal hp bar, 1 = purple hp bar
	}
}
