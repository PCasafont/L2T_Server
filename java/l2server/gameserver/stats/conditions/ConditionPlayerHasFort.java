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

import l2server.gameserver.model.L2Clan;
import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerHasFort.
 *
 * @author MrPoke
 */
public final class ConditionPlayerHasFort extends Condition
{

	private final int _fort;

	/**
	 * Instantiates a new condition player has fort.
	 *
	 * @param fort the fort
	 */
	public ConditionPlayerHasFort(int fort)
	{
		_fort = fort;
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

		L2Clan clan = ((L2PcInstance) env.player).getClan();
		if (clan == null)
		{
			return _fort == 0;
		}

		// Any fortress
		if (_fort == -1)
		{
			return clan.getHasFort() > 0;
		}

		return clan.getHasFort() == _fort;
	}
}
