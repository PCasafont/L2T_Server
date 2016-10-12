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
 * The Class ConditionPlayerCharges.
 */
public class ConditionPlayerCharges extends Condition
{

	private final int _charges;

	/**
	 * Instantiates a new condition player charges.
	 *
	 * @param charges the charges
	 */
	public ConditionPlayerCharges(int charges)
	{
		_charges = charges;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		//This can be used by monsters but getCharges not exist
		if (env.player instanceof L2PcInstance)
		{
			return ((L2PcInstance) env.player).getCharges() >= _charges;
		}

		return false;
	}
}
