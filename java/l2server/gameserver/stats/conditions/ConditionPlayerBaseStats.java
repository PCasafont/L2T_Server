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

package l2server.gameserver.stats.conditions;

import l2server.gameserver.model.actor.L2Character;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerBaseStats.
 *
 * @author mkizub
 */
public class ConditionPlayerBaseStats extends Condition
{

	private final BaseStat stat;
	private final int value;

	/**
	 * Instantiates a new condition player base stats.
	 *
	 * @param player the player
	 * @param stat   the stat
	 * @param value  the value
	 */
	public ConditionPlayerBaseStats(L2Character player, BaseStat stat, int value)
	{
		super();
		this.stat = stat;
		this.value = value;
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
		{
			return false;
		}
		L2PcInstance player = (L2PcInstance) env.player;
		switch (stat)
		{
			case Int:
				return player.getINT() >= value;
			case Str:
				return player.getSTR() >= value;
			case Con:
				return player.getCON() >= value;
			case Dex:
				return player.getDEX() >= value;
			case Men:
				return player.getMEN() >= value;
			case Wit:
				return player.getWIT() >= value;
			case Cha:
				return player.getCHA() >= value;
			case Luc:
				return player.getLUC() >= value;
		}
		return false;
	}
}

enum BaseStat
{
	Int, Str, Con, Dex, Men, Wit, Cha, Luc
}
