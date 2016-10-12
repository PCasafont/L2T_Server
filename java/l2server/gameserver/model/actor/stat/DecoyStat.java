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

import l2server.gameserver.model.actor.instance.L2DecoyInstance;

public class DecoyStat extends NpcStat
{
	public DecoyStat(L2DecoyInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public final L2DecoyInstance getActiveChar()
	{
		return (L2DecoyInstance) super.getActiveChar();
	}

	@Override
	public int getRunSpeed()
	{
		if (getActiveChar() == null || getActiveChar().getOwner() == null)
		{
			return super.getRunSpeed();
		}

		return getActiveChar().getOwner().getRunSpeed();
	}

	@Override
	public int getPAtkSpd()
	{
		if (getActiveChar() == null || getActiveChar().getOwner() == null)
		{
			return super.getPAtkSpd();
		}

		return getActiveChar().getOwner().getPAtkSpd();
	}

	@Override
	public int getMAtkSpd()
	{
		if (getActiveChar() == null || getActiveChar().getOwner() == null)
		{
			return super.getMAtkSpd();
		}

		return getActiveChar().getOwner().getMAtkSpd();
	}
}
