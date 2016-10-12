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
import l2server.gameserver.stats.Env;

/**
 * The Class ConditionPlayerLandingZone.
 *
 * @author kerberos
 */
public class ConditionPlayerLandingZone extends Condition
{

	private boolean _val;

	/**
	 * Instantiates a new condition player landing zone.
	 *
	 * @param val the val
	 */
	public ConditionPlayerLandingZone(boolean val)
	{
		_val = val;
	}

	/* (non-Javadoc)
	 * @see l2server.gameserver.stats.conditions.Condition#testImpl(l2server.gameserver.stats.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		return env.player.isInsideZone(L2Character.ZONE_LANDING) == _val;
	}
}
