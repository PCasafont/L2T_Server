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

package l2server.gameserver.model.actor.stat;

import l2server.gameserver.model.actor.L2Npc;

public class NpcStat extends CharStat
{
	public NpcStat(L2Npc activeChar)
	{
		super(activeChar);
	}

	@Override
	public byte getLevel()
	{
		return getActiveChar().getTemplate().Level;
	}

	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) super.getActiveChar();
	}

	/*
	@Override
	public int getWalkSpeed(){ return (int) calcStat(Stats.WALK_SPEED, getActiveChar().getTemplate().baseWalkSpd, null, null);}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar() == null)
			return 1;
		if (getActiveChar().isRunning())
			return getRunSpeed() * 1f / getActiveChar().getTemplate().baseRunSpd;
		else
			return getWalkSpeed() * 1f / getActiveChar().getTemplate().baseWalkSpd;
	}
	 */
}
