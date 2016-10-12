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

import l2server.gameserver.model.actor.instance.L2PcInstance;
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerPledgeClass.
 *
 * @author MrPoke
 */
public final class ConditionPlayerPledgeClass extends Condition
{

	private final int _pledgeClass;

	/**
	 * Instantiates a new condition player pledge class.
	 *
	 * @param pledgeClass the pledge class
	 */
	public ConditionPlayerPledgeClass(int pledgeClass)
	{
		_pledgeClass = pledgeClass;
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

		final L2PcInstance player = (L2PcInstance) env.player;

		if (player.getClan() == null)
		{
			return false;
		}

		if (_pledgeClass == -1)
		{
			return player.isClanLeader();
		}

		//System.out.println(player.getName() + " Pledge = " + player.getPledgeClass());
		return player.getPledgeClass() >= _pledgeClass;
	}
}
